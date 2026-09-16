package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;

import java.util.List;
import java.util.UUID;

public interface ClinicalNotesRepository {

    List<ClinicalNotesDBRecord> findClinicalNotesById(UUID uuid);

    List<ClinicalNotesDBRecord> findClinicalNotes(DiagnosticReportSearchQuery diagnosticReportSearchQuery);

}
