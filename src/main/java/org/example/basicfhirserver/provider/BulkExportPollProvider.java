package org.example.basicfhirserver.provider;

import static org.example.basicfhirserver.jobs.export.FhirExportServiceConstants.ExportPollEndpoint;
import static org.example.basicfhirserver.jobs.export.FhirExportServiceConstants.ExportPollEndpointWithParams;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.example.basicfhirserver.domain.entities.ExportJobFilesEntity;
import org.example.basicfhirserver.exceptions.BulkExportValidationException;
import org.example.basicfhirserver.jobs.export.ExportService;
import org.example.basicfhirserver.provider.parsers.BulkExportRequestParser;
import org.example.basicfhirserver.provider.parsers.ParsedExportRequest;
import org.example.basicfhirserver.service.ExportJobFilesService;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.StorageProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BulkExportPollProvider {

  private final StorageProvider storageProvider;
  private final ExportJobFilesService exportJobFilesService;
  private final ExportService exportService;
  private final JobScheduler jobScheduler;

  public BulkExportPollProvider(
      StorageProvider storageProvider,
      ExportJobFilesService exportJobFilesService,
      ExportService exportService,
      JobScheduler jobScheduler) {
    this.storageProvider = storageProvider;
    this.exportJobFilesService = exportJobFilesService;
    this.exportService = exportService;
    this.jobScheduler = jobScheduler;
  }

  @Operation(name = "$export", idempotent = true, manualResponse = true)
  public void systemExport(
          RequestDetails theRequestDetails,
          HttpServletResponse theServletResponse
  ) throws Exception {
    try {
      BulkExportRequestParser bulkExportRequestParser =
              new BulkExportRequestParser(exportService.supportedSystemExportResources());
      ParsedExportRequest parsedRequest =
              bulkExportRequestParser.parseAndValidate(theRequestDetails);



      JobId jobId =
              jobScheduler.enqueue(
                      () ->
                              exportService.executeSystemBulkExport(
                                      JobContext.Null,
                                      parsedRequest.resourcesToExport(),
                                      parsedRequest.parsedSince()));

      String serverBaseUrl = theRequestDetails.getFhirServerBase();
      String pollingUrl = serverBaseUrl + ExportPollEndpointWithParams + jobId;

      theServletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
      theServletResponse.setHeader("Content-Location", pollingUrl);
      theServletResponse.getWriter().close();

    } catch (BulkExportValidationException e) {
      theServletResponse.setStatus(e.getStatusCode());
      theServletResponse.setContentType("application/fhir+json;charset=UTF-8");
      String escapedError = e.getMessage().replace("\"", "\\\"");
      theServletResponse
              .getWriter()
              .write(
                      "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\",\"code\":\"value\",\"diagnostics\":\""
                              + escapedError
                              + "\"}]}");
      theServletResponse.getWriter().close();

    } catch (Exception e) {
      theServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      theServletResponse.getWriter().close();
    }


  }

  @Operation(name = "$" + ExportPollEndpoint, idempotent = true, manualResponse = true)
  public void getExportStatus(
      @OperationParam(name = "jobId") StringParam jobId,
      RequestDetails theRequestDetails,
      HttpServletResponse theServletResponse)
      throws IOException {

    if (jobId == null || jobId.getValue() == null) {
      theServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      theServletResponse.getWriter().write("Missing required parameter: jobId");
      return;
    }

    Job job;
    try {
      job = storageProvider.getJobById(UUID.fromString(jobId.getValue()));
    } catch (JobNotFoundException | IllegalArgumentException e) {
      theServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
      theServletResponse.getWriter().write("Job not found for ID: " + jobId.getValue());
      return;
    }

    StateName status = job.getState();

    if (status.equals(StateName.PROCESSING)) {
      theServletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
      theServletResponse.setHeader("X-Progress", "Data extraction in progress");
      theServletResponse.setHeader("Retry-After", "60");
      theServletResponse.getWriter().close();
      return;
    }

    if (status.equals(StateName.FAILED)) {
      String errorMessage = "An unexpected error occurred during data extraction.";
      String escapedMessage = errorMessage.replace("\\", "\\\\").replace("\"", "\\\"");

      theServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      theServletResponse.setContentType("application/fhir+json;charset=UTF-8");

      String operationOutcomeJson =
          """
                    {
                      "resourceType": "OperationOutcome",
                      "issue": [
                        {
                          "severity": "error",
                          "code": "exception",
                          "details": {
                            "text": "Bulk export job execution failed."
                          },
                          "diagnostics": "%s"
                            }
                          ]
                        }
                    """
              .formatted(escapedMessage);
      theServletResponse.getWriter().write(operationOutcomeJson);
      theServletResponse.getWriter().close();
      return;
    }

    if (status.equals(StateName.SUCCEEDED)) {
      List<ExportJobFilesEntity> fileEntities = exportJobFilesService.findById(job.getId());

      if (fileEntities.isEmpty()) {
        theServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        theServletResponse.getWriter().write("Job succeeded but no export files were found.");
        return;
      }
      String serverBaseUrl = theRequestDetails.getFhirServerBase();

      StringJoiner outputArrayJoiner = createOutputArrayJoiner(fileEntities, serverBaseUrl);

      Instant createdAt = job.getCreatedAt();
      String formattedString = createdAt != null ? createdAt.toString() : Instant.now().toString();

      String manifestJson =
          """
                    {
                      "transactionTime": "%s",
                      "request": "%s/Patient/$export",
                      "requiresAccessToken": false,
                      "output": [
                    %s
                      ],
                      "error": []
                    }
                    """
              .formatted(
                  formattedString,
                  serverBaseUrl,
                  outputArrayJoiner.toString().indent(4).stripTrailing());
      theServletResponse.setStatus(HttpServletResponse.SC_OK);
      theServletResponse.setContentType("application/json;charset=UTF-8");
      theServletResponse.getWriter().write(manifestJson);
      theServletResponse.getWriter().close();
    }
  }

  private static @NonNull StringJoiner createOutputArrayJoiner(
      List<ExportJobFilesEntity> fileEntities, String serverBaseUrl) {
    StringJoiner outputArrayJoiner = new StringJoiner(",\n");
    for (ExportJobFilesEntity file : fileEntities) {
      int cutoff = file.getFileId().lastIndexOf('/') + 1;
      String fileId = file.getFileId().substring(cutoff);
      String itemJson =
          """
                        {
                          "type": "%s",
                          "url": "%s/download/ndjson?fileId=%s"
                        }
                    """
              .formatted(file.getResourceType(), serverBaseUrl, fileId)
              .trim();
      outputArrayJoiner.add(itemJson);
    }
    return outputArrayJoiner;
  }

  @Operation(name = "download-ndjson", idempotent = true, manualResponse = true)
  public void downloadFile(
      @OperationParam(name = "fileId") StringParam fileId, HttpServletResponse theServletResponse)
      throws IOException {

    if (fileId == null || fileId.getValue() == null) {
      theServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      theServletResponse.getWriter().write("Missing required parameter: fileId");
      return;
    }

    Path fullFilePath = exportService.getExportFileRootPath().resolve(fileId.getValue());
    List<ExportJobFilesEntity> exportJobFilesEntities =
        exportJobFilesService.findByFileId(fullFilePath.toString());

    if (exportJobFilesEntities.isEmpty()) {
      theServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
      theServletResponse.getWriter().write("Requested file ID not found or has expired.");
      return;
    }

    List<String> fileNames =
        exportJobFilesEntities.stream().map(ExportJobFilesEntity::getFileId).toList();
    String fileName = fileNames.get(0);

    try (FileInputStream fileInputStream = new FileInputStream(fileName);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
        PrintWriter writer = theServletResponse.getWriter()) {

      theServletResponse.setStatus(HttpServletResponse.SC_OK);
      theServletResponse.setContentType("application/fhir+ndjson;charset=UTF-8");
      theServletResponse.setHeader(
          "Content-Disposition", "attachment; filename=\"" + fileId + ".ndjson\"");

      String line;
      while ((line = reader.readLine()) != null) {
        writer.println(line);
      }
      writer.flush();

    } catch (FileNotFoundException e) {
      theServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
      theServletResponse.setContentType("text/plain;charset=UTF-8");
      theServletResponse.getWriter().write("Requested file not found on server disk: " + fileName);
    }
  }
}
