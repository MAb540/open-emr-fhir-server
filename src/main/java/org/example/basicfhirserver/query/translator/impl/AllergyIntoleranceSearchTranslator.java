package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchCriteria;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

@Component
public class AllergyIntoleranceSearchTranslator implements SearchTranslator<AllergyIntoleranceSearchQuery,
        AllergyIntoleranceSearchCriteria> {

    @Override
    public AllergyIntoleranceSearchQuery translate(AllergyIntoleranceSearchCriteria criteria) {
        return AllergyIntoleranceSearchQuery.builder()
                .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
                .build();
    }

}
