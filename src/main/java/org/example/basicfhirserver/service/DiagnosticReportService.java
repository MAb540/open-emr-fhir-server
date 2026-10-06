package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.model.DiagnosticReportCanonical;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;

public interface DiagnosticReportService {

  ClinicalNotesDBRecord findClinicalNotesById(UUID uuid);

  DiagnosticReportCanonical findClinicalNotes(
      DiagnosticReportSearchQuery diagnosticReportSearchQuery);
}
