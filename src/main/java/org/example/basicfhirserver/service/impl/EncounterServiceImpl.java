package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterDBRecord;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterService;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.assembler.formencounter.FormEncounterAssembler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EncounterServiceImpl implements EncounterService {

    private final FormEncounterAssembler formEncounterAssembler;
    private final FormEncounterService formEncounterService;

    public EncounterServiceImpl(
            FormEncounterService formEncounterService,
            FormEncounterAssembler formEncounterAssembler
    ) {
        this.formEncounterService = formEncounterService;
        this.formEncounterAssembler = formEncounterAssembler;
    }


    @Override
    public FormEncounter findById(UUID uuid) {
        List<FormEncounterDBRecord> formEncountersDBRecords = formEncounterService.findById(uuid);
        if(formEncountersDBRecords.isEmpty()){
            throw new ResourceNotFoundException("Encounter with given ID " + uuid + " not found.");
        }

        return formEncounterAssembler.toCanonical(formEncountersDBRecords.get(0));
    }

    @Override
    public List<FormEncounter> find(EncounterSearchQuery encounterSearchQuery) {
        List<FormEncounterDBRecord> formEncountersDBRecords = formEncounterService.find(encounterSearchQuery);
        return formEncountersDBRecords.stream().map(formEncounterAssembler::toCanonical).toList();
    }
}
