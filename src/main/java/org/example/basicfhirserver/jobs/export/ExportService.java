package org.example.basicfhirserver.jobs.export;

import static org.example.basicfhirserver.provider.parsers.BulkExportRequestParser.parseSinceParameter;

import ca.uhn.fhir.parser.IParser;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.example.basicfhirserver.config.FhirContextConfig;
import org.example.basicfhirserver.domain.entities.ExportJobFilesEntity;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.*;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.model.Practitioner;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.service.*;
import org.example.basicfhirserver.service.assembler.condition.ConditionAssembler;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.context.JobContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExportService {

  private final PatientService patientService;
  private final LegacyPatientMapper legacyPatientMapper;
  private final ObservationService observationService;
  private final ObservationMapper observationMapper;
  private final EncounterService encounterService;
  private final EncounterMapper encounterMapper;
  private final ConditionService conditionService;
  private final ConditionMapper conditionMapper;
  private final PractitionerService practitionerService;
  private final PractitionerMapper practitionerMapper;

  private final FhirContextConfig fhirContextConfig;
  private final ExportJobFilesService exportJobFilesService;

  private static final int BATCH_SIZE = 100;
  private static final int FILE_BUFFER_SIZE = 32768;

  private static final String EXPORT_FILE_PATH = "export-job-files";

  public ExportService(
          LegacyPatientMapper legacyPatientMapper,
          PatientService patientService,
          @Qualifier("ObservationServiceImpl") ObservationService observationService,
          ObservationMapper observationMapper,
          EncounterService encounterService,
          EncounterMapper encounterMapper,
          ConditionService conditionService,
          ConditionMapper conditionMapper, PractitionerService practitionerService, PractitionerMapper practitionerMapper,
          FhirContextConfig fhirContextConfig,
          ExportJobFilesService exportJobFilesService) {
    this.legacyPatientMapper = legacyPatientMapper;
    this.patientService = patientService;
    this.observationService = observationService;
    this.observationMapper = observationMapper;
    this.encounterService = encounterService;
    this.encounterMapper = encounterMapper;
    this.conditionMapper = conditionMapper;
    this.conditionService = conditionService;
    this.practitionerService = practitionerService;
    this.practitionerMapper = practitionerMapper;
    this.fhirContextConfig = fhirContextConfig;
    this.exportJobFilesService = exportJobFilesService;
  }

  public List<String> supportedPatientExportResources() {
    return Stream.of(PatientExportSupportedResources.values())
            .map(PatientExportSupportedResources::getValue)
            .toList();
  }

  public List<String> supportedSystemExportResources() {
    return Stream.of(SystemExportSupportedResources.values())
            .map(SystemExportSupportedResources::getValue)
            .toList();
  }

  @Job(name = "Patient Bulk Data Export Task")
  public void executePatientBulkExport(
      JobContext jobContext, List<String> supportedResources, String since) {
    UUID jobID = jobContext.getJobId();
    handlePatientExport(jobID, supportedResources, since);
  }

  @Job(name = "System Bulk Data Export Task")
  public void executeSystemBulkExport(
          JobContext jobContext, List<String> supportedResources, String since) {
    UUID jobID = jobContext.getJobId();
    handleSystemExport(jobID, supportedResources, since);
  }

  private void handlePatientExport(UUID jobID, List<String> supportedResources, String since) {
    try {
      createExportFilesDir();
      SearchValue<LocalDateTime> parsedSince = parseSinceParameter(since);

      if (supportedResources.contains(PatientExportSupportedResources.PATIENT.getValue())) {
        ExportPatients(jobID, parsedSince);
      }

      if (supportedResources.contains(PatientExportSupportedResources.OBSERVATION.getValue())) {
        ExportObservations(jobID, parsedSince);
      }

      if (supportedResources.contains(PatientExportSupportedResources.ENCOUNTER.getValue())) {
        ExportEncounters(jobID, parsedSince);
      }

      if (supportedResources.contains(PatientExportSupportedResources.CONDITION.getValue())) {
        ExportCondition(jobID, parsedSince);
      }

    } catch (Exception e) {
      log.error("Job {}: Unexpected error during execution", jobID, e);
      throw new RuntimeException("Export task was interrupted", e);
    }
  }

  private void handleSystemExport(UUID jobID, List<String> supportedResources, String since) {
    try {
      createExportFilesDir();
      SearchValue<LocalDateTime> parsedSince = parseSinceParameter(since);

      if (supportedResources.contains(SystemExportSupportedResources.PRACTITIONER.getValue())) {
        ExportPractitioner(jobID, parsedSince);
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

    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(filePath.toFile()), FILE_BUFFER_SIZE)) {
      while (hasMoreData) {
        PatientSearchQuery query =
            PatientSearchQuery.builder()
                .lastUpdated(since)
                .count(BATCH_SIZE)
                .offset(offset)
                .build();

        Page<LegacyPatientEntity> page = patientService.find(query);

        List<LegacyPatientEntity> content = page.getContent();
        if (content.isEmpty()) {
          log.info("JobID {}: Patient export job content is empty", jobID);
          break;
        }
        for (LegacyPatientEntity entity : content) {
          IBaseResource patient = legacyPatientMapper.toR4(entity);
          String jsonLine = fhirJsonParser.encodeResourceToString(patient);
          writer.write(jsonLine);
          writer.newLine();
        }
        log.debug(
            "JobID {}: Patient export job exported {} records (Current offset: {})",
            jobID,
            content.size(),
            offset);

        offset += BATCH_SIZE;
        hasMoreData = page.hasNext();
      }

      log.info(
          "JobID {}: Patient export job NDJSON file written successfully to {}", jobID, filePath);
      ExportJobFilesEntity exportJobFilesEntity =
          ExportJobFilesEntity.builder()
              .jobUuid(jobID)
              .fileId(filePath.toString())
              .resourceType(PatientExportSupportedResources.PATIENT.getValue())
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

    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(filePath.toFile()), FILE_BUFFER_SIZE)) {
      //            while (hasMoreData) {
      ObservationSearchQuery query =
          ObservationSearchQuery.builder()
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
      log.debug(
          "JobID {}: Observation export job exported {} records (Current offset: {})",
          jobID,
          observations.size(),
          offset);
      offset += BATCH_SIZE;

      //            }
      log.info(
          "JobID {}: Observation export job NDJSON file written successfully to {}",
          jobID,
          filePath);
      ExportJobFilesEntity exportJobFilesEntity =
          ExportJobFilesEntity.builder()
              .jobUuid(jobID)
              .fileId(filePath.toString())
              .resourceType(PatientExportSupportedResources.OBSERVATION.getValue())
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

  private void ExportEncounters(UUID jobID, SearchValue<LocalDateTime> since) {

    String fileName = jobID + "-encounters" + ".ndjson";
    Path filePath = getExportFileRootPath().resolve(fileName);
    IParser fhirJsonParser = fhirContextConfig.fhirContext().newJsonParser().setPrettyPrint(false);

    int offset = 0;
    boolean hasMoreData = true;

    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(filePath.toFile()), FILE_BUFFER_SIZE)) {
      while (hasMoreData) {
        EncounterSearchQuery query =
            EncounterSearchQuery.builder()
                .lastUpdated(since)
                .count(BATCH_SIZE)
                .offset(offset)
                .build();

        Page<FormEncounter> page = encounterService.find(query);
        List<FormEncounter> content = page.getContent();

        if (page.isEmpty()) {
          log.info("JobID {}: Encounter export job content is empty", jobID);
          break;
        }

        for (FormEncounter entity : content) {
          IBaseResource patient = encounterMapper.toR4(entity);
          String jsonLine = fhirJsonParser.encodeResourceToString(patient);
          writer.write(jsonLine);
          writer.newLine();
        }
        log.debug(
            "JobID {}: Encounter export job exported {} records (Current offset: {})",
            jobID,
            content.size(),
            offset);

        offset += BATCH_SIZE;
        hasMoreData = page.hasNext();
      }

      log.info(
          "JobID {}: Encounter export job NDJSON file written successfully to {}", jobID, filePath);
      ExportJobFilesEntity exportJobFilesEntity =
          ExportJobFilesEntity.builder()
              .jobUuid(jobID)
              .fileId(filePath.toString())
              .resourceType(PatientExportSupportedResources.ENCOUNTER.getValue())
              .createdAt(LocalDateTime.now())
              .build();

      exportJobFilesService.save(exportJobFilesEntity);
      log.info("JobID {}: Encounter export job metadata saved successfully.", jobID);

    } catch (IOException e) {
      log.error("JobID {}: Encounter export job File writing failed", jobID, e);
      throw new RuntimeException("Encounter export task failed due to I/O error", e);
    } catch (RuntimeException e) {
      log.error("JobID {}: Encounter export job Failed.", jobID, e);
      throw new RuntimeException(e);
    }
  }

  private void ExportCondition(UUID jobID, SearchValue<LocalDateTime> since) {

    String fileName = jobID + "-condition" + ".ndjson";
    Path filePath = getExportFileRootPath().resolve(fileName);
    IParser fhirJsonParser = fhirContextConfig.fhirContext().newJsonParser().setPrettyPrint(false);

    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(filePath.toFile()), FILE_BUFFER_SIZE)) {
      List<String> conditionCategories =
          List.of(
              ConditionAssembler.CATEGORY_PROBLEM_LIST,
              ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS,
              ConditionAssembler.CATEGORY_HEALTH_CONCERNS);

      for (String condition : conditionCategories) {
        exportConditionCategory(jobID, writer, condition, fhirJsonParser);
      }

      log.info(
          "JobID {}: Condition export job NDJSON file written successfully to {}", jobID, filePath);
      ExportJobFilesEntity exportJobFilesEntity =
          ExportJobFilesEntity.builder()
              .jobUuid(jobID)
              .fileId(filePath.toString())
              .resourceType(PatientExportSupportedResources.CONDITION.getValue())
              .createdAt(LocalDateTime.now())
              .build();

      exportJobFilesService.save(exportJobFilesEntity);
      log.info("JobID {}: Condition export job metadata saved successfully.", jobID);

    } catch (IOException e) {
      log.error("JobID {}: Condition export job File writing failed", jobID, e);
      throw new RuntimeException("Encounter export task failed due to I/O error", e);
    } catch (RuntimeException e) {
      log.error("JobID {}: Condition export job Failed.", jobID, e);
      throw new RuntimeException(e);
    }
  }

  private void ExportPractitioner(UUID jobID, SearchValue<LocalDateTime> since) {

    String fileName = jobID + "-practitioner" + ".ndjson";
    Path filePath = getExportFileRootPath().resolve(fileName);
    IParser fhirJsonParser = fhirContextConfig.fhirContext().newJsonParser().setPrettyPrint(false);

    int offset = 0;
    boolean hasMoreData = true;

    try (BufferedWriter writer =
                 new BufferedWriter(new FileWriter(filePath.toFile()), FILE_BUFFER_SIZE)) {
      while (hasMoreData) {
        PractitionerSearchQuery query =
                PractitionerSearchQuery.builder()
                        .count(BATCH_SIZE)
                        .offset(offset)
                        .build();

        Page<Practitioner> page = practitionerService.find(query);

        List<Practitioner> content = page.getContent();
        if (content.isEmpty()) {
          log.info("JobID {}: Practitioner export job content is empty", jobID);
          break;
        }
        for (Practitioner entity : content) {
          IBaseResource patient = practitionerMapper.toR4(entity);
          String jsonLine = fhirJsonParser.encodeResourceToString(patient);
          writer.write(jsonLine);
          writer.newLine();
        }
        log.debug(
                "JobID {}: Practitioner export job exported {} records (Current offset: {})",
                jobID,
                content.size(),
                offset);

        offset += BATCH_SIZE;
        hasMoreData = page.hasNext();
      }

      log.info(
              "JobID {}: Practitioner export job NDJSON file written successfully to {}", jobID, filePath);
      ExportJobFilesEntity exportJobFilesEntity =
              ExportJobFilesEntity.builder()
                      .jobUuid(jobID)
                      .fileId(filePath.toString())
                      .resourceType(SystemExportSupportedResources.PRACTITIONER.getValue())
                      .createdAt(LocalDateTime.now())
                      .build();

      exportJobFilesService.save(exportJobFilesEntity);
      log.info("JobID {}: Practitioner export job metadata saved successfully.", jobID);

    } catch (IOException e) {
      log.error("JobID {}: Practitioner export job File writing failed", jobID, e);
      throw new RuntimeException("Patient export task failed due to I/O error", e);
    } catch (RuntimeException e) {
      log.error("JobID {}: Practitioner export job Failed.", jobID, e);
      throw new RuntimeException(e);
    }
  }

  private void exportConditionCategory(
      UUID jobID, BufferedWriter writer, String category, IParser fhirJsonParser)
      throws IOException {
    int offset = 0;
    boolean hasMoreData = true;

    while (hasMoreData) {
      ConditionSearchQuery query =
          ConditionSearchQuery.builder()
              .category(category)
              .count(BATCH_SIZE)
              .offset(offset)
              .build();

      Page<ConditionCanonical> page = conditionService.find(query);
      List<ConditionCanonical> content = page.getContent();

      if (page.isEmpty()) {
        log.info("JobID {}: Condition export job content is empty", jobID);
        break;
      }

      for (ConditionCanonical entity : content) {
        IBaseResource condition = conditionMapper.toR4(entity);
        String jsonLine = fhirJsonParser.encodeResourceToString(condition);
        writer.write(jsonLine);
        writer.newLine();
      }
      log.debug(
          "JobID {}: Condition export job exported {} records (Current offset: {})",
          jobID,
          content.size(),
          offset);

      offset += BATCH_SIZE;
      hasMoreData = page.hasNext();
    }
  }

  private void createExportFilesDir() {
    try {
      Files.createDirectories(getExportFileRootPath());
    } catch (IOException e) {
      log.error("Failed to create root directory: " + EXPORT_FILE_PATH, e);
      throw new RuntimeException(
          "Initialization failed: Export Files Root directory inaccessible", e);
    }
  }

  public Path getExportFileRootPath() {
    return Paths.get(EXPORT_FILE_PATH);
  }
}
