package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.EncounterSearchTranslator;
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
    private final EncounterSearchTranslator encounterSearchTranslator;

    public EncounterResourceProvider(
            EncounterService encounterService,
            EncounterMapper encounterMapper,
            EncounterSearchTranslator encounterSearchTranslator
    ) {
        this.encounterService = encounterService;
        this.encounterMapper = encounterMapper;
        this.encounterSearchTranslator = encounterSearchTranslator;
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
    public List<Encounter> searchEncounters(
            @OptionalParam(name = Encounter.SP_RES_ID) TokenParam id,
            @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient,
            @OptionalParam(name = Encounter.SP_DATE) DateParam date
    ) {

        EncounterSearchCriteria criteria = EncounterSearchCriteria.builder()
                .id(id)
                .patient(patient)
                .date(date)
                .build();

        var encounterSearchQuery = encounterSearchTranslator.translate(criteria);
        List<FormEncounter> formEncounters = encounterService.find(encounterSearchQuery);

        return formEncounters.stream()
                .map(encounterMapper::toR4)
                .toList();
    }


}
