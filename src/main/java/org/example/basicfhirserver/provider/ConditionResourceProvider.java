package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.ConditionMapper;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.ConditionTranslator;
import org.example.basicfhirserver.service.ConditionService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.data.domain.Page;
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
    public IBundleProvider searchEncounters(
            @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient,
            @OptionalParam(name = Observation.SP_CATEGORY) TokenParam category,
            @Count Integer count,
            @Offset Integer offset
    ) {
        ConditionSearchCriteria criteria = ConditionSearchCriteria.builder()
                .patient(patient)
                .category(category)
                .count(count)
                .offset(offset)
                .build();

        var conditionSearchQuery = conditionTranslator.translate(criteria);
        Page<ConditionCanonical> conditionsCanonical = conditionService.find(conditionSearchQuery);

        List<IBaseResource> primaryConditions = conditionsCanonical.getContent().stream()
                .<IBaseResource>map(conditionMapper::toR4)
                .toList();

        int currentOffset = offset != null ? offset : 0;
        int currentPageSize = conditionsCanonical.getContent().size();

        return new BundleProvider(
                primaryConditions,
                List.of(),
                Math.toIntExact(conditionsCanonical.getTotalElements()),
                currentOffset,
                currentPageSize
        );
    }
}
