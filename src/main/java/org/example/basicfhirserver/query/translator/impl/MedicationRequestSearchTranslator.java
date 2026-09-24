package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchCriteria;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.example.basicfhirserver.query.translator.utils.TranslatorUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MedicationRequestSearchTranslator implements SearchTranslator<MedicationRequestSearchQuery, MedicationRequestSearchCriteria> {

    @Override
    public MedicationRequestSearchQuery translate(MedicationRequestSearchCriteria criteria) {

        return MedicationRequestSearchQuery.builder()
                .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
                .intent(criteria.getIntent() == null ? List.of() :
                        criteria.getIntent().getValuesAsQueryTokens()
                                .stream().map(TranslatorUtils::tokenWithSystem).toList())
                .status(criteria.getStatus() == null ? null : criteria.getStatus().toString())
                .count(criteria.getCount())
                .offset(criteria.getOffset())
                .build();

    }

}
