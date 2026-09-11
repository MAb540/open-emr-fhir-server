package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

@Component
public class ConditionTranslator implements SearchTranslator<ConditionSearchQuery, ConditionSearchCriteria> {

    @Override
    public ConditionSearchQuery translate(ConditionSearchCriteria criteria) {
        return ConditionSearchQuery.builder()
                .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
                .category(criteria.getCategory() == null ? null : criteria.getCategory().getValue())
                .build();
    }
}
