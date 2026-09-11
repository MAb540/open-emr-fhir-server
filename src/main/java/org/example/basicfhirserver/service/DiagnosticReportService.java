package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.DiagnosticReportCanonical;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;

import java.util.List;
import java.util.UUID;

public interface DiagnosticReportService {

    ClinicalNotesDBRecord findClinicalNotesById(UUID uuid);

    DiagnosticReportCanonical findClinicalNotes();

}
