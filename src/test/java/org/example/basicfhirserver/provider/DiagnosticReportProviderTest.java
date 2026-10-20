package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import java.util.List;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchCriteria;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.query.translator.impl.DiagnosticReportSearchTranslator;
import org.example.basicfhirserver.service.DiagnosticReportService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DiagnosticReportProviderTest {

  @Mock private DiagnosticReportService diagnosticReportService;
  @Mock private DiagnosticReportSearchTranslator diagnosticReportSearchTranslator;

  @InjectMocks private DiagnosticReportProvider provider;

  @Test
  void searchDiagnosticReport_returnsCombinedNotesAndProcedures() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();
    when(diagnosticReportSearchTranslator.translate(any(DiagnosticReportSearchCriteria.class)))
        .thenReturn(query);

    DiagnosticReport noteReport = new DiagnosticReport();
    DiagnosticReport procedureReport = new DiagnosticReport();
    when(diagnosticReportService.search(query))
        .thenReturn(new SearchResult(List.of(noteReport, procedureReport), List.of(), 2, 0));

    IBundleProvider bundle =
        provider.searchDiagnosticReport(null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(2);
    assertThat(bundle.getResources(0, 10)).containsExactly(noteReport, procedureReport);
    verify(diagnosticReportService).search(query);
  }

  @Test
  void searchDiagnosticReport_returnsEmptyBundleWhenNoResults() {
    DiagnosticReportSearchQuery query = DiagnosticReportSearchQuery.builder().build();
    when(diagnosticReportSearchTranslator.translate(any(DiagnosticReportSearchCriteria.class)))
        .thenReturn(query);

    when(diagnosticReportService.search(query))
        .thenReturn(new SearchResult(List.of(), List.of(), 0, 0));

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
    when(diagnosticReportService.search(query))
        .thenReturn(new SearchResult(List.of(), List.of(), 0, 0));

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
