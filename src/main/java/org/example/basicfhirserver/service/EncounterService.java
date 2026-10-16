package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.service.impl.encounter.EncounterSearchResult;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.data.domain.Page;

public interface EncounterService {

  Encounter findById(UUID uuid);

  Page<Encounter> find(EncounterSearchQuery query);

  EncounterSearchResult search(EncounterSearchQuery encounterSearchQuery);
}
