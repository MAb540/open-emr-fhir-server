package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.FormEncounter;

import java.util.List;
import java.util.UUID;

public interface EncounterService  {

    FormEncounter findById(UUID id);

    List<FormEncounter> find();

}
