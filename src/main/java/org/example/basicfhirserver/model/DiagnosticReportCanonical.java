package org.example.basicfhirserver.model;

import lombok.Value;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;
import org.springframework.data.domain.Page;

import java.util.List;

@Value
public class DiagnosticReportCanonical {
    Page<ClinicalNotesDBRecord> notes;
    Page<ProcedureDBRecord> procedures;
}
