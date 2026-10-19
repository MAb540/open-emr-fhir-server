package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Encounter;

public interface EncounterService {

  Encounter findById(UUID uuid);

  SearchResult search(EncounterSearchQuery encounterSearchQuery);
}
