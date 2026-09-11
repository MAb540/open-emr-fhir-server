package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.ConditionMapper;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.ConditionTranslator;
import org.example.basicfhirserver.service.ConditionService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ConditionResourceProvider implements IResourceProvider {

    private final ConditionService conditionService;
    private final ConditionMapper conditionMapper;
    private final ConditionTranslator conditionTranslator;

    public ConditionResourceProvider(ConditionService conditionService,
                                     ConditionMapper conditionMapper,
                                     ConditionTranslator conditionTranslator) {
        this.conditionService = conditionService;
        this.conditionMapper = conditionMapper;
        this.conditionTranslator = conditionTranslator;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Condition.class;
    }

    @Read()
    public Condition getResourceById(@IdParam IdType theId) {
        ConditionCanonical conditionsCanonical = conditionService.findById(UUID.fromString(theId.getIdPart()));
        return conditionMapper.toR4(conditionsCanonical);
    }

    @Search()
    public List<Condition> searchEncounters(
            @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient,
            @OptionalParam(name = Observation.SP_CATEGORY) TokenParam category
    ) {

        ConditionSearchCriteria criteria = ConditionSearchCriteria.builder()
                .patient(patient)
                .category(category)
                .build();

        var conditionSearchQuery = conditionTranslator.translate(criteria);
        List<ConditionCanonical> conditionsCanonical = conditionService.find(conditionSearchQuery);

        return conditionsCanonical.stream()
                .map(conditionMapper::toR4)
                .toList();
    }
}
