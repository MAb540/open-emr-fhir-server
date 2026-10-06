package org.example.basicfhirserver.jobs.export;

import ca.uhn.fhir.parser.IParser;
import lombok.extern.slf4j.Slf4j;
import org.example.basicfhirserver.config.FhirContextConfig;
import org.example.basicfhirserver.domain.entities.ExportJobFilesEntity;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.service.ExportJobFilesService;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.PatientService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.context.JobContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.example.basicfhirserver.provider.parsers.BulkExportRequestParser.parseSinceParameter;

@Slf4j
@Service
public class FhirExportService {

    private final PatientService patientService;
    private final LegacyPatientMapper legacyPatientMapper;
    private final ObservationService observationService;
    private final ObservationMapper observationMapper;

    private final FhirContextConfig fhirContextConfig;
    private final ExportJobFilesService exportJobFilesService;

    private static final int BATCH_SIZE = 100;
    private static final int FILE_BUFFER_SIZE = 32768;

    private static final String EXPORT_FILE_PATH = "export-job-files";

    public FhirExportService(LegacyPatientMapper legacyPatientMapper,
                             PatientService patientService,
                             @Qualifier("ObservationServiceImpl") ObservationService observationService,
                             ObservationMapper observationMapper,
                             FhirContextConfig fhirContextConfig,
                             ExportJobFilesService exportJobFilesService) {

        this.legacyPatientMapper = legacyPatientMapper;
        this.patientService = patientService;
        this.observationService = observationService;
        this.observationMapper = observationMapper;
        this.fhirContextConfig = fhirContextConfig;
        this.exportJobFilesService = exportJobFilesService;
    }

    public List<String> supportedExportResources() {
        return List.of("Patient", "Observation", "Condition", "Encounter");
    }

    @Job(name = "FHIR Bulk Data Export for job %0")
    public void executeBulkExport(JobContext jobContext, List<String> supportedResources, String since) {
        UUID jobID = jobContext.getJobId();
        handleExport(jobID, supportedResources, since);
    }

    private void handleExport(UUID jobID, List<String> supportedResources, String since) {
        try {
            createExportFilesDir();
            SearchValue<LocalDateTime> parsedSince = parseSinceParameter(since);

            if (supportedResources.contains("Patient")) {
                ExportPatients(jobID, parsedSince);
            }

            if (supportedResources.contains("Observation")) {
                ExportObservations(jobID, parsedSince);
            }

        } catch (Exception e) {
            log.error("Job {}: Unexpected error during execution", jobID, e);
            throw new RuntimeException("Export task was interrupted", e);
        }
    }

    private void ExportPatients(UUID jobID, SearchValue<LocalDateTime> since) {

        String fileName = jobID + "-patients" + ".ndjson";
        Path filePath = getExportFileRootPath().resolve(fileName);
        IParser fhirJsonParser = fhirContextConfig.fhirContext().newJsonParser().setPrettyPrint(false);

        int offset = 0;
        boolean hasMoreData = true;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()), FILE_BUFFER_SIZE)) {
            while (hasMoreData) {
                PatientSearchQuery query = PatientSearchQuery.builder()
                        .lastUpdated(since)
                        .count(BATCH_SIZE)
                        .offset(offset)
                        .build();
                Page<LegacyPatientEntity> page = patientService.find(query);

                List<LegacyPatientEntity> content = page.getContent();
                if (content.isEmpty()) {
                    log.info("JobID {}: Patient export job content is empty",jobID);
                    break;
                }

                for (LegacyPatientEntity entity : content) {
                    IBaseResource patient = legacyPatientMapper.toR4(entity);
                    String jsonLine = fhirJsonParser.encodeResourceToString(patient);
                    writer.write(jsonLine);
                    writer.newLine();
                }
                log.debug("JobID {}: Patient export job exported {} records (Current offset: {})", jobID, content.size(), offset);

                offset += BATCH_SIZE;
                hasMoreData = page.hasNext();
            }

            log.info("JobID {}: Patient export job NDJSON file written successfully to {}", jobID, filePath);
            ExportJobFilesEntity exportJobFilesEntity = ExportJobFilesEntity.builder()
                    .jobUuid(jobID)
                    .fileId(filePath.toString())
                    .resourceType("Patient")
                    .createdAt(LocalDateTime.now())
                    .build();

            exportJobFilesService.save(exportJobFilesEntity);
            log.info("JobID {}: Patient export job metadata saved successfully.", jobID);

        } catch (IOException e) {
            log.error("JobID {}: Patient export job File writing failed", jobID, e);
            throw new RuntimeException("Patient export task failed due to I/O error", e);
        } catch (RuntimeException e) {
            log.error("JobID {}: Patient export job Failed.", jobID, e);
            throw new RuntimeException(e);
        }
    }

    private void ExportObservations(UUID jobID, SearchValue<LocalDateTime> since) {

        String fileName = jobID + "-observations" + ".ndjson";
        Path filePath = getExportFileRootPath().resolve(fileName);
        IParser fhirJsonParser = fhirContextConfig.fhirContext().newJsonParser().setPrettyPrint(false);

        int offset = 0;
        boolean hasMoreData = true;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()), FILE_BUFFER_SIZE)) {
//            while (hasMoreData) {
            ObservationSearchQuery query = ObservationSearchQuery.builder()
                    .lastUpdated(since)
                    .count(BATCH_SIZE)
                    .offset(offset)
                    .build();

            List<VitalObservation> observations = observationService.find(query);
            if (observations.isEmpty()) {
                log.info("JobID {}: Observation export job records are {}.", jobID, 0);
                return;
            }

            for (VitalObservation entity : observations) {
                IBaseResource patient = observationMapper.toR4(entity);
                String jsonLine = fhirJsonParser.encodeResourceToString(patient);
                writer.write(jsonLine);
                writer.newLine();
            }
            log.debug("JobID {}: Observation export job exported {} records (Current offset: {})", jobID, observations.size(), offset);
            offset += BATCH_SIZE;

//            }
            log.info("JobID {}: Observation export job NDJSON file written successfully to {}", jobID, filePath);
            ExportJobFilesEntity exportJobFilesEntity = ExportJobFilesEntity.builder()
                    .jobUuid(jobID)
                    .fileId(filePath.toString())
                    .resourceType("Observation")
                    .createdAt(LocalDateTime.now())
                    .build();

            exportJobFilesService.save(exportJobFilesEntity);
            log.info("JobID {}: Observation export job metadata saved successfully.", jobID);

        } catch (IOException e) {
            log.error("JobID {}: Observation export job File writing failed", jobID, e);
            throw new RuntimeException("Observation export task failed due to I/O error", e);
        } catch (RuntimeException e) {
            log.error("JobID {}: Observation export job Failed.", jobID, e);
            throw new RuntimeException(e);
        }
    }

    private void createExportFilesDir() {
        try {
            Files.createDirectories(getExportFileRootPath());
        } catch (IOException e) {
            log.error("Failed to create root directory: " + EXPORT_FILE_PATH, e);
            throw new RuntimeException("Initialization failed: Export Files Root directory inaccessible", e);
        }
    }

    public Path getExportFileRootPath() {
        return Paths.get(EXPORT_FILE_PATH);
    }


}
