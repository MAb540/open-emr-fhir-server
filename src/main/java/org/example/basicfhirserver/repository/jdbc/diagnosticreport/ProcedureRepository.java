package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;

import java.util.List;
import java.util.UUID;

public interface ProcedureRepository {

    List<ProcedureDBRecord> findProcedureById(UUID uuid);

    List<ProcedureDBRecord> findProcedures(DiagnosticReportSearchQuery diagnosticReportSearchQuery);

}
