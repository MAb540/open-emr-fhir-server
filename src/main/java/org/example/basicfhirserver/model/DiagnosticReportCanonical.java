package org.example.basicfhirserver.model;

import lombok.Value;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;

import java.util.List;

@Value
public class DiagnosticReportCanonical {
    List<ClinicalNotesDBRecord> notes;
    List<ProcedureDBRecord> procedures;
}
