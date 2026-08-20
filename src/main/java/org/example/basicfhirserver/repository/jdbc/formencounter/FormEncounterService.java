package org.example.basicfhirserver.repository.jdbc.formencounter;

import java.util.List;
import java.util.UUID;

public interface FormEncounterService {

    List<FormEncounterDBRecord> findById(UUID uuid);

    List<FormEncounterDBRecord> find();

}
