package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.springframework.data.domain.Page;

public interface EncounterService {

  FormEncounter findById(UUID uuid);

  Page<FormEncounter> find(EncounterSearchQuery encounterSearchQuery);
}
