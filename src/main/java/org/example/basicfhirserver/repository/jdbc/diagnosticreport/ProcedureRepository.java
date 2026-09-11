package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import java.util.List;
import java.util.UUID;

public interface ProcedureRepository {

    List<ProcedureDBRecord> findProcedureById(UUID uuid);

    List<ProcedureDBRecord> findProcedures();

}
