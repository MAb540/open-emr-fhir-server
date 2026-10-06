package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.springframework.data.domain.Page;

public interface ClinicalNotesRepository {

  List<ClinicalNotesDBRecord> findClinicalNotesById(UUID uuid);

  Page<ClinicalNotesDBRecord> findClinicalNotes(
      DiagnosticReportSearchQuery diagnosticReportSearchQuery);
}
