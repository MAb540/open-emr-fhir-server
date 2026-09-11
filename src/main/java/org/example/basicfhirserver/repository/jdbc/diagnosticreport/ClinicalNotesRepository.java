package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import java.util.List;
import java.util.UUID;

public interface ClinicalNotesRepository {

    List<ClinicalNotesDBRecord> findClinicalNotesById(UUID uuid);

    List<ClinicalNotesDBRecord> findClinicalNotes();

}
