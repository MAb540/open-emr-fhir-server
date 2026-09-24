package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.medication.MedicationSearchCriteria;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

@Component
public class MedicationSearchTranslator implements SearchTranslator<MedicationSearchQuery, MedicationSearchCriteria> {

    @Override
    public MedicationSearchQuery translate(MedicationSearchCriteria criteria) {
        return MedicationSearchQuery.builder()
                .count(criteria.getCount())
                .offset(criteria.getOffset())
                .build();
    }
}
