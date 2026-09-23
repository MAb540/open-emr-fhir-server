package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface EncounterService  {

    FormEncounter findById(UUID uuid);

    Page<FormEncounter> find(EncounterSearchQuery encounterSearchQuery);

}
