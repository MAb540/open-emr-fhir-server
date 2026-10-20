package org.example.basicfhirserver.service;

import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;

public interface DiagnosticReportService {

  SearchResult search(DiagnosticReportSearchQuery diagnosticReportSearchQuery);
}
