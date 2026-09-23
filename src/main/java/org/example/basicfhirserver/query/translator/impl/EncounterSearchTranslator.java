package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.encounter.EncounterSearchCriteria;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.*;

@Component
public class EncounterSearchTranslator implements SearchTranslator<EncounterSearchQuery, EncounterSearchCriteria> {

    @Override
    public EncounterSearchQuery translate(EncounterSearchCriteria criteria) {
        return EncounterSearchQuery.builder()
                .encounterId(criteria.getId() != null ? List.of(token(criteria.getId()) ) : List.of())
                .patientId(criteria.getPatient() == null ? null : List.of(criteria.getPatient().getIdPart()))
                .date(date(criteria.getDate(), d -> d == null ? null :
                        d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .count(criteria.getCount())
                .offset(criteria.getOffset())
                .build();
    }
}
