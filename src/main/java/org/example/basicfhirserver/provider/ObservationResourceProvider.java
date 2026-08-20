package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.ObservationSearchTranslator;
import org.example.basicfhirserver.service.ObservationService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ObservationResourceProvider implements IResourceProvider {

    private final ObservationService observationService;
    private final ObservationMapper observationMapper;
    private final ObservationSearchTranslator observationSearchTranslator;

    public ObservationResourceProvider(@Qualifier("ObservationServiceImpl") ObservationService observationService, ObservationMapper observationMapper, ObservationSearchTranslator observationSearchTranslator) {
        this.observationService = observationService;
        this.observationMapper = observationMapper;
        this.observationSearchTranslator = observationSearchTranslator;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Observation.class;
    }

    @Read()
    public Observation getResourceById(@IdParam IdType theId) {
        VitalObservation observation = observationService.findById(UUID.fromString(theId.getIdPart()));

        return observationMapper.toR4(observation);
    }

    @Search()
    public List<Observation> searchObservations(
            @OptionalParam(name = Observation.SP_PATIENT) ReferenceParam patient,
            @OptionalParam(name = Observation.SP_CATEGORY) TokenParam category,
            @OptionalParam(name = Observation.SP_CODE) TokenOrListParam codes,
            @OptionalParam(name = Observation.SP_DATE) DateParam date,
            @OptionalParam(name = Observation.SP_RES_LAST_UPDATED) DateParam lastUpdated) {

        ObservationSearchCriteria criteria = ObservationSearchCriteria.builder().patient(patient).category(category).codes(codes).date(date).lastUpdated(lastUpdated).build();

        var observationSearchQuery = observationSearchTranslator.translate(criteria);

        List<VitalObservation> observations = observationService.find(observationSearchQuery);

        return observations.stream().map(observationMapper::toR4).toList();
    }


}
