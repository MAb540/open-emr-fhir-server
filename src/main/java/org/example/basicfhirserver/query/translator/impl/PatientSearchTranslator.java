package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.patient.PatientSearchCriteria;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.*;

@Component
public class PatientSearchTranslator implements SearchTranslator<PatientSearchQuery, PatientSearchCriteria> {

    @Override
    public PatientSearchQuery translate(PatientSearchCriteria criteria) {

        return PatientSearchQuery.builder()
                .patientId(token(criteria.getId()))
                .identifier(token(criteria.getIdentifier()))
                .firstName(stringMatch(criteria.getGiven()))
                .lastName(stringMatch(criteria.getFamily()))
                .name(stringMatch(criteria.getName()))
                .birthDate(date(criteria.getBirthdate(), d -> d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()))
                .deathDate(date(criteria.getDeathDate(), d -> d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .count(criteria.getCount())
                .offset(criteria.getOffset())
                .build();
    }

}
