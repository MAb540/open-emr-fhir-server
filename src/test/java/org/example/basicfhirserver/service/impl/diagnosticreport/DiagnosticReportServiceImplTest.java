package org.example.basicfhirserver.service.impl.diagnosticreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.basicfhirserver.mapper.DiagnosticReportClinicalNotesMapper;
import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesRepository;
import org.example.basicfhirserver.repository.jdbc.procedure.ProcedureDBRecord;
import org.example.basicfhirserver.repository.jdbc.procedure.ProcedureRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DiagnosticReportServiceImplTest {

  @Mock private ClinicalNotesRepository clinicalNotesRepository;
  @Mock private ProcedureRepository procedureRepository;
  @Mock private DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper;
  @Mock private DiagnosticReportProcedureMapper diagnosticReportProcedureMapper;

  @InjectMocks private DiagnosticReportServiceImpl service;

  @Test
  void search_combinesClinicalNotesAndProcedures() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();

    ClinicalNotesDBRecord noteRecord = mock(ClinicalNotesDBRecord.class);
    Page<ClinicalNotesDBRecord> notesPage = mock(Page.class);
    when(notesPage.getContent()).thenReturn(List.of(noteRecord));
    when(notesPage.getTotalElements()).thenReturn(1L);
    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    ProcedureDBRecord procedureRecord = mock(ProcedureDBRecord.class);
    Page<ProcedureDBRecord> proceduresPage = mock(Page.class);
    when(proceduresPage.getContent()).thenReturn(List.of(procedureRecord));
    when(proceduresPage.getTotalElements()).thenReturn(1L);
    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    DiagnosticReport noteReport = new DiagnosticReport();
    when(diagnosticReportClinicalNotesMapper.toR4(noteRecord)).thenReturn(noteReport);
    DiagnosticReport procedureReport = new DiagnosticReport();
    when(diagnosticReportProcedureMapper.toR4(procedureRecord)).thenReturn(procedureReport);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(noteReport, procedureReport);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();
    verify(clinicalNotesRepository).findClinicalNotes(query);
    verify(procedureRepository).findProcedures(query);
  }

  @Test
  void search_returnsOnlyNotesWhenNoProcedures() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();

    ClinicalNotesDBRecord noteRecord = mock(ClinicalNotesDBRecord.class);
    Page<ClinicalNotesDBRecord> notesPage = mock(Page.class);
    when(notesPage.getContent()).thenReturn(List.of(noteRecord));
    when(notesPage.getTotalElements()).thenReturn(1L);
    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    Page<ProcedureDBRecord> proceduresPage = mock(Page.class);
    when(proceduresPage.getContent()).thenReturn(List.of());
    when(proceduresPage.getTotalElements()).thenReturn(0L);
    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    DiagnosticReport noteReport = new DiagnosticReport();
    when(diagnosticReportClinicalNotesMapper.toR4(noteRecord)).thenReturn(noteReport);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(noteReport);
    assertThat(result.totalElements()).isEqualTo(1);
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();

    Page<ClinicalNotesDBRecord> notesPage = mock(Page.class);
    when(notesPage.getContent()).thenReturn(List.of());
    when(notesPage.getTotalElements()).thenReturn(0L);
    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    Page<ProcedureDBRecord> proceduresPage = mock(Page.class);
    when(proceduresPage.getContent()).thenReturn(List.of());
    when(proceduresPage.getTotalElements()).thenReturn(0L);
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

    ClinicalNotesDBRecord noteRecord = mock(ClinicalNotesDBRecord.class);
    Page<ClinicalNotesDBRecord> notesPage = mock(Page.class);
    when(notesPage.getContent()).thenReturn(List.of(noteRecord));
    when(notesPage.getTotalElements()).thenReturn(3L);
    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    ProcedureDBRecord procedureRecord = mock(ProcedureDBRecord.class);
    Page<ProcedureDBRecord> proceduresPage = mock(Page.class);
    when(proceduresPage.getContent()).thenReturn(List.of(procedureRecord));
    when(proceduresPage.getTotalElements()).thenReturn(2L);
    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    DiagnosticReport noteReport = new DiagnosticReport();
    when(diagnosticReportClinicalNotesMapper.toR4(noteRecord)).thenReturn(noteReport);
    DiagnosticReport procedureReport = new DiagnosticReport();
    when(diagnosticReportProcedureMapper.toR4(procedureRecord)).thenReturn(procedureReport);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(noteReport, procedureReport);
    assertThat(result.totalElements()).isEqualTo(5);
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    DiagnosticReportSearchQuery query =
        DiagnosticReportSearchQuery.builder().offset(10).count(5).build();

    Page<ClinicalNotesDBRecord> notesPage = mock(Page.class);
    when(notesPage.getContent()).thenReturn(List.of());
    when(notesPage.getTotalElements()).thenReturn(0L);
    when(clinicalNotesRepository.findClinicalNotes(query)).thenReturn(notesPage);

    Page<ProcedureDBRecord> proceduresPage = mock(Page.class);
    when(proceduresPage.getContent()).thenReturn(List.of());
    when(proceduresPage.getTotalElements()).thenReturn(0L);
    when(procedureRepository.findProcedures(query)).thenReturn(proceduresPage);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
    verify(clinicalNotesRepository).findClinicalNotes(query);
    verify(procedureRepository).findProcedures(query);
  }
}
