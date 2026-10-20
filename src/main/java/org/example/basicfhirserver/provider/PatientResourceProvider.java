package org.example.basicfhirserver.provider;

import static org.example.basicfhirserver.jobs.export.FhirExportServiceConstants.ExportPollEndpointWithParams;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.exceptions.BulkExportValidationException;
import org.example.basicfhirserver.jobs.export.ExportService;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.parsers.BulkExportRequestParser;
import org.example.basicfhirserver.provider.parsers.ParsedExportRequest;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.patient.PatientSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.PatientSearchTranslator;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.context.JobContext;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_PATIENT,
    supported = {ProfilesConstants.HL7_US_CORE_PATIENT})
public class PatientResourceProvider implements IResourceProvider {

  private final PatientService patientService;
  private final PatientSearchTranslator patientSearchTranslator;
  private final ExportService exportService;
  private final JobScheduler jobScheduler;

  public PatientResourceProvider(
      PatientService patientService,
      PatientSearchTranslator patientSearchTranslator,
      ExportService exportService,
      JobScheduler jobScheduler) {
    this.patientService = patientService;
    this.patientSearchTranslator = patientSearchTranslator;
    this.exportService = exportService;
    this.jobScheduler = jobScheduler;
  }

  @Override
  public Class<Patient> getResourceType() {
    return Patient.class;
  }

  @Read()
  public Patient getResourceById(@IdParam IdType theId) {
    return patientService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchPatients(
      @OptionalParam(name = Patient.SP_RES_ID) TokenParam id,
      @OptionalParam(name = Patient.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
      @OptionalParam(name = Patient.SP_FAMILY) StringParam family,
      @OptionalParam(name = Patient.SP_GIVEN) StringParam given,
      @OptionalParam(name = Patient.SP_NAME) StringParam name,
      @OptionalParam(name = Patient.SP_BIRTHDATE) DateParam birthDate,
      @OptionalParam(name = Patient.SP_DEATH_DATE) DateParam deathDate,
      @IncludeParam(allow = {"Patient:organization", "Patient:general-practitioner"})
          Set<Include> theIncludes,
      @IncludeParam(
              reverse = true,
              allow = {"Observation:patient", "Observation:subject", "Encounter:patient"})
          Set<Include> theRevIncludes,
      @Count Integer count,
      @Offset Integer offset) {
    PatientSearchCriteria criteria =
        PatientSearchCriteria.builder()
            .id(id)
            .lastUpdated(lastUpdated)
            .identifier(identifier)
            .family(family)
            .given(given)
            .name(name)
            .birthdate(birthDate)
            .deathDate(deathDate)
            .includes(theIncludes)
            .revIncludes(theRevIncludes)
            .count(count)
            .offset(offset)
            .build();

    var patientSearchQuery = patientSearchTranslator.translate(criteria);
    SearchResult searchResult = patientService.search(patientSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }

  @Operation(name = "$export", type = Patient.class, idempotent = true, manualResponse = true)
  public void patientExport(
      RequestDetails theRequestDetails, HttpServletResponse theServletResponse) throws Exception {

    try {
      BulkExportRequestParser bulkExportRequestParser =
          new BulkExportRequestParser(exportService.supportedPatientExportResources());
      ParsedExportRequest parsedRequest =
          bulkExportRequestParser.parseAndValidate(theRequestDetails);

      JobId jobId =
          jobScheduler.enqueue(
              () ->
                  exportService.executePatientBulkExport(
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
}
