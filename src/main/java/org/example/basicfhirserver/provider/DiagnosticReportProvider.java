package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.DiagnosticReportSearchTranslator;
import org.example.basicfhirserver.service.DiagnosticReportService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

@Component
public class DiagnosticReportProvider implements IResourceProvider {

  private final DiagnosticReportService diagnosticReportService;
  private final DiagnosticReportSearchTranslator diagnosticReportSearchTranslator;

  public DiagnosticReportProvider(
      DiagnosticReportService diagnosticReportService,
      DiagnosticReportSearchTranslator diagnosticReportSearchTranslator) {
    this.diagnosticReportService = diagnosticReportService;
    this.diagnosticReportSearchTranslator = diagnosticReportSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return DiagnosticReport.class;
  }

  @Search()
  public IBundleProvider searchDiagnosticReport(
      @OptionalParam(name = DiagnosticReport.SP_RES_ID) TokenParam id,
      @OptionalParam(name = DiagnosticReport.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = Observation.SP_DATE) DateParam date,
      @OptionalParam(name = DiagnosticReport.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @OptionalParam(name = Observation.SP_CODE) TokenOrListParam codes,
      @Count Integer count,
      @Offset Integer offset) {
    DiagnosticReportSearchCriteria criteria =
        DiagnosticReportSearchCriteria.builder()
            .id(id)
            .patient(patient)
            .date(date)
            .lastUpdated(lastUpdated)
            .codes(codes)
            .count(count)
            .offset(offset)
            .build();

    var diagnosticReportSearchQuery = diagnosticReportSearchTranslator.translate(criteria);
    SearchResult searchResult = diagnosticReportService.search(diagnosticReportSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
