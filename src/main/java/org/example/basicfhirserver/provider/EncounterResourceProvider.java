package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.service.EncounterService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EncounterResourceProvider implements IResourceProvider {

    private final EncounterService encounterService;
    private final EncounterMapper encounterMapper;

    public EncounterResourceProvider(
            EncounterService encounterService,
            EncounterMapper encounterMapper
    ) {
        this.encounterService = encounterService;
        this.encounterMapper = encounterMapper;
    }


    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Encounter.class;
    }


    @Read()
    public Encounter getResourceById(@IdParam IdType theId) {
        FormEncounter formEncounter = encounterService.findById(UUID.fromString(theId.getIdPart()));
        return encounterMapper.toR4(formEncounter);
    }

    @Search()
    public List<Encounter> searchEncounters() {
        return encounterService.find().stream()
                .map(encounterMapper::toR4)
                .toList();
    }


}
