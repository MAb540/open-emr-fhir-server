package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;

import java.util.List;
import java.util.UUID;

public interface EncounterService  {

    FormEncounter findById(UUID id);

    List<FormEncounter> find(EncounterSearchQuery encounterSearchQuery);

}
