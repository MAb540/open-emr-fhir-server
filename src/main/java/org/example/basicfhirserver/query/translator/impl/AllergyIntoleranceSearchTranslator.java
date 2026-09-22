package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchCriteria;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;

@Component
public class AllergyIntoleranceSearchTranslator implements SearchTranslator<AllergyIntoleranceSearchQuery,
        AllergyIntoleranceSearchCriteria> {

    @Override
    public AllergyIntoleranceSearchQuery translate(AllergyIntoleranceSearchCriteria criteria) {
        return AllergyIntoleranceSearchQuery.builder()
                .id(token(criteria.getId()))
                .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
                .count(criteria.getCount())
                .offset(criteria.getOffset())
                .build();
    }

}
