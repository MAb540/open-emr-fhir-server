package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ClinicalNotesRepository {

    List<ClinicalNotesDBRecord> findClinicalNotesById(UUID uuid);

    Page<ClinicalNotesDBRecord> findClinicalNotes(DiagnosticReportSearchQuery diagnosticReportSearchQuery);

}
