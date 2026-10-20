package org.example.basicfhirserver.service.impl.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.DiagnosticReportClinicalNotesMapper;
import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.mapper.impl.DiagnosticReportProcedureMapperImpl;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesRepository;
import org.example.basicfhirserver.repository.jdbc.procedure.ProcedureDBRecord;
import org.example.basicfhirserver.repository.jdbc.procedure.ProcedureRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DiagnosticReportServiceImplTest {

  @Mock private ClinicalNotesRepository clinicalNotesRepository;
  @Mock private ProcedureRepository procedureRepository;

  // The clinical notes mapper is still an unimplemented stub (it returns null), so it stays
  // mocked. The procedure mapper is a pure function over a DB record - there is no I/O to mock,
  // so the real implementation is used and the assertions target the returned FHIR resource.
  @Mock private DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper;

  private final DiagnosticReportProcedureMapper diagnosticReportProcedureMapper =
      new DiagnosticReportProcedureMapperImpl();

  private DiagnosticReportServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new DiagnosticReportServiceImpl(
            clinicalNotesRepository,
            procedureRepository,
            diagnosticReportClinicalNotesMapper,
            diagnosticReportProcedureMapper);
  }

  private static final UUID ORDER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID PATIENT_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
  private static final UUID ENCOUNTER_UUID =
      UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
  private static final UUID PRACTITIONER_UUID =
      UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
  private static final UUID LAB_UUID = UUID.fromString("523e4567-e89b-12d3-a456-426614174004");
  private static final UUID RESULT_UUID = UUID.fromString("623e4567-e89b-12d3-a456-426614174005");
  private static final LocalDateTime PROCEDURE_DATE = LocalDateTime.of(2026, 1, 20, 11, 15);

  private static ProcedureDBRecord procedureRecord() {
    return ProcedureDBRecord.builder()
        .orderUuid(ORDER_UUID)
        .procedureName("Complete blood count")
        .standardCode("58410-2")
        .orderStatus("completed")
        .provider(ProcedureDBRecord.ProviderInfo.builder().id(1L).uuid(PRACTITIONER_UUID).build())
        .patient(
            ProcedureDBRecord.PatientReferenceInfo.builder().pid(42L).uuid(PATIENT_UUID).build())
        .encounter(
            ProcedureDBRecord.EncounterReferenceInfo.builder()
                .id(7L)
                .uuid(ENCOUNTER_UUID)
                .date(PROCEDURE_DATE)
                .build())
        .lab(
            ProcedureDBRecord.LabMetadataInfo.builder()
                .id(3L)
                .uuid(LAB_UUID)
                .name("Central Lab")
                .directorUuid(PRACTITIONER_UUID)
                .build())
        .reports(
            List.of(
                ProcedureDBRecord.ReportBlock.builder()
                    .id(9L)
                    .uuid(RESULT_UUID)
                    .date(PROCEDURE_DATE)
                    .results(
                        List.of(
                            ProcedureDBRecord.ResultBlock.builder()
                                .id(11L)
                                .uuid(RESULT_UUID)
                                .text("Haemoglobin")
                                .build()))
                    .build()))
        .build();
  }

  private static ClinicalNotesDBRecord notesRecord() {
    return ClinicalNotesDBRecord.builder().id(1L).uuid(ORDER_UUID).code("NOTE-1").build();
  }

  @Test
  void search_returnsClinicalNotesAndProcedureReports() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();

    ClinicalNotesDBRecord notesRecord = notesRecord();
    Page<ClinicalNotesDBRecord> notesPage = new PageImpl<>(List.of(notesRecord));

    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    ProcedureDBRecord procedureRecord = procedureRecord();
    Page<ProcedureDBRecord> proceduresPage = new PageImpl<>(List.of(procedureRecord));

    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    DiagnosticReport noteReport = new DiagnosticReport();
    noteReport.setId(notesRecord.getUuid().toString());
    when(diagnosticReportClinicalNotesMapper.toR4(notesRecord)).thenReturn(noteReport);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    DiagnosticReport note = (DiagnosticReport) result.primaryResources().get(0);
    assertThat(note.getId()).isEqualTo(notesRecord.getUuid().toString());

    DiagnosticReport procedure = (DiagnosticReport) result.primaryResources().get(1);
    assertThat(procedure.getId()).isEqualTo(ORDER_UUID.toString());
    assertThat(procedure.getStatus()).isEqualTo(DiagnosticReport.DiagnosticReportStatus.FINAL);
    assertThat(procedure.getCode().getCodingFirstRep().getCode()).isEqualTo("58410-2");
    assertThat(procedure.getCode().getCodingFirstRep().getDisplay())
        .isEqualTo("Complete blood count");
    assertThat(procedure.getSubject().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(procedure.getEncounter().getReference()).isEqualTo("Encounter/" + ENCOUNTER_UUID);
    assertThat(procedure.getCategory().get(0).getCodingFirstRep().getCode())
        .isEqualTo("LAB_CATEGORY");
    assertThat(procedure.getBasedOn().get(0).getReference())
        .isEqualTo("ServiceRequest/" + ORDER_UUID);
    assertThat(procedure.getResult().get(0).getReference()).isEqualTo("Observation/" + RESULT_UUID);
    assertThat(procedure.getResultsInterpreter().get(0).getReference())
        .isEqualTo("Practitioner/" + PRACTITIONER_UUID);
  }

  @Test
  void search_returnsOnlyProcedureReportsWhenNoClinicalNotes() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();

    Page<ClinicalNotesDBRecord> notesPage = new PageImpl<>(List.of());

    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    Page<ProcedureDBRecord> proceduresPage = new PageImpl<>(List.of(procedureRecord()));

    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1);

    DiagnosticReport procedure = (DiagnosticReport) result.primaryResources().get(0);
    assertThat(procedure.getId()).isEqualTo(ORDER_UUID.toString());
    assertThat(procedure.getStatus()).isEqualTo(DiagnosticReport.DiagnosticReportStatus.FINAL);
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();

    Page<ClinicalNotesDBRecord> notesPage = new PageImpl<>(List.of());

    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    Page<ProcedureDBRecord> proceduresPage = new PageImpl<>(List.of());

    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_sumsTotalsFromNotesAndProcedures() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();

    ClinicalNotesDBRecord notesRecord = notesRecord();
    Page<ClinicalNotesDBRecord> notesPage =
        new PageImpl<>(List.of(notesRecord), PageRequest.of(0, 1), 3L);

    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    Page<ProcedureDBRecord> proceduresPage =
        new PageImpl<>(List.of(procedureRecord()), PageRequest.of(0, 1), 2L);

    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    DiagnosticReport noteReport = new DiagnosticReport();
    when(diagnosticReportClinicalNotesMapper.toR4(notesRecord)).thenReturn(noteReport);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.totalElements()).isEqualTo(5);
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    DiagnosticReportSearchQuery query =
        DiagnosticReportSearchQuery.builder().offset(10).count(5).build();

    Page<ClinicalNotesDBRecord> notesPage = new PageImpl<>(List.of());

    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    Page<ProcedureDBRecord> proceduresPage = new PageImpl<>(List.of());

    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}
