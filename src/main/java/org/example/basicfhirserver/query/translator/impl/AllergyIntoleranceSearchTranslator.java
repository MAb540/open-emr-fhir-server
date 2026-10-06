package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchCriteria;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.date;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;

@Component
public class AllergyIntoleranceSearchTranslator implements SearchTranslator<AllergyIntoleranceSearchQuery,
        AllergyIntoleranceSearchCriteria> {

    @Override
    public AllergyIntoleranceSearchQuery translate(AllergyIntoleranceSearchCriteria criteria) {
        return AllergyIntoleranceSearchQuery.builder()
                .id(token(criteria.getId()))
                .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
                .lastUpdated(date(criteria.getLastUpdated(), d -> d == null ? null :
                        d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .count(criteria.getCount())
                .offset(criteria.getOffset())
                .build();
    }

}
