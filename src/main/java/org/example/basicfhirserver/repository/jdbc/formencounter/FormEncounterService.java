package org.example.basicfhirserver.repository.jdbc.formencounter;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.springframework.data.domain.Page;

public interface FormEncounterService {

  List<FormEncounterDBRecord> findById(UUID uuid);

  Page<FormEncounterDBRecord> find(EncounterSearchQuery encounterSearchQuery);
}
