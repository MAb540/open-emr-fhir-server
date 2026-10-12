package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import java.util.List;
import org.example.basicfhirserver.mapper.DiagnosticReportClinicalNotesMapper;
import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.model.DiagnosticReportCanonical;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchCriteria;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.query.translator.impl.DiagnosticReportSearchTranslator;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;
import org.example.basicfhirserver.service.DiagnosticReportService;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DiagnosticReportProviderTest {

  @Mock private DiagnosticReportService diagnosticReportService;
  @Mock private DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper;
  @Mock private DiagnosticReportProcedureMapper diagnosticReportProcedureMapper;
  @Mock private DiagnosticReportSearchTranslator diagnosticReportSearchTranslator;

  @InjectMocks private DiagnosticReportProvider provider;

  @Test
  void searchDiagnosticReport_returnsCombinedNotesAndProcedures() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();
    when(diagnosticReportSearchTranslator.translate(any(DiagnosticReportSearchCriteria.class)))
        .thenReturn(query);

    ClinicalNotesDBRecord noteRecord = mock(ClinicalNotesDBRecord.class);
    Page<ClinicalNotesDBRecord> notesPage = mock(Page.class);
    when(notesPage.getContent()).thenReturn(List.of(noteRecord));
    when(notesPage.getTotalElements()).thenReturn(1L);

    ProcedureDBRecord procedureRecord = mock(ProcedureDBRecord.class);
    Page<ProcedureDBRecord> proceduresPage = mock(Page.class);
    when(proceduresPage.getContent()).thenReturn(List.of(procedureRecord));
    when(proceduresPage.getTotalElements()).thenReturn(1L);

    when(diagnosticReportService.findClinicalNotes(query))
        .thenReturn(new DiagnosticReportCanonical(notesPage, proceduresPage));

    DiagnosticReport noteReport = new DiagnosticReport();
    when(diagnosticReportClinicalNotesMapper.toR4(noteRecord)).thenReturn(noteReport);
    DiagnosticReport procedureReport = new DiagnosticReport();
    when(diagnosticReportProcedureMapper.toR4(procedureRecord)).thenReturn(procedureReport);

    IBundleProvider bundle =
        provider.searchDiagnosticReport(null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(2);
    assertThat(bundle.getResources(0, 10)).containsExactly(noteReport, procedureReport);
    verify(diagnosticReportService).findClinicalNotes(query);
  }

  @Test
  void searchDiagnosticReport_returnsEmptyBundleWhenNoResults() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();
    when(diagnosticReportSearchTranslator.translate(any(DiagnosticReportSearchCriteria.class)))
        .thenReturn(query);

    Page<ClinicalNotesDBRecord> notesPage = mock(Page.class);
    when(notesPage.getContent()).thenReturn(List.of());
    when(notesPage.getTotalElements()).thenReturn(0L);
    Page<ProcedureDBRecord> proceduresPage = mock(Page.class);
    when(proceduresPage.getContent()).thenReturn(List.of());
    when(proceduresPage.getTotalElements()).thenReturn(0L);

    when(diagnosticReportService.findClinicalNotes(query))
        .thenReturn(new DiagnosticReportCanonical(notesPage, proceduresPage));

    IBundleProvider bundle =
        provider.searchDiagnosticReport(null, null, null, null, null, null, null);

    assertThat(bundle.size()).isZero();
    assertThat(bundle.getResources(0, 10)).isEmpty();
  }

  @Test
  void searchDiagnosticReport_buildsCriteriaFromSearchParams() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();
    when(diagnosticReportSearchTranslator.translate(any(DiagnosticReportSearchCriteria.class)))
        .thenReturn(query);

    Page<ClinicalNotesDBRecord> notesPage = mock(Page.class);
    when(notesPage.getContent()).thenReturn(List.of());
    when(notesPage.getTotalElements()).thenReturn(0L);
    Page<ProcedureDBRecord> proceduresPage = mock(Page.class);
    when(proceduresPage.getContent()).thenReturn(List.of());
    when(proceduresPage.getTotalElements()).thenReturn(0L);
    when(diagnosticReportService.findClinicalNotes(query))
        .thenReturn(new DiagnosticReportCanonical(notesPage, proceduresPage));

    TokenParam id = new TokenParam("123");
    ReferenceParam patient = new ReferenceParam("Patient/abc");
    TokenOrListParam codes = new TokenOrListParam();
    provider.searchDiagnosticReport(id, patient, null, null, codes, 10, 5);

    ArgumentCaptor<DiagnosticReportSearchCriteria> captor =
        ArgumentCaptor.forClass(DiagnosticReportSearchCriteria.class);
    verify(diagnosticReportSearchTranslator).translate(captor.capture());
    assertThat(captor.getValue().getId()).isSameAs(id);
    assertThat(captor.getValue().getPatient()).isSameAs(patient);
    assertThat(captor.getValue().getCodes()).isSameAs(codes);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
  }
}
