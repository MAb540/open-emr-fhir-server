package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.springframework.data.domain.Page;

public interface ProcedureRepository {

  List<ProcedureDBRecord> findProcedureById(UUID uuid);

  Page<ProcedureDBRecord> findProcedures(DiagnosticReportSearchQuery diagnosticReportSearchQuery);
}
