package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.hl7.fhir.r4.model.DiagnosticReport;

public interface DiagnosticReportClinicalNotesMapper extends ResourceMapper <DiagnosticReport, ClinicalNotesDBRecord> {
}
