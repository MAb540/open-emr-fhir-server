package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;
import org.hl7.fhir.r4.model.DiagnosticReport;

public interface DiagnosticReportProcedureMapper extends ResourceMapper<DiagnosticReport, ProcedureDBRecord>{
}
