package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.encounter.EncounterSearchCriteria;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.*;

@Component
public class EncounterSearchTranslator implements SearchTranslator<EncounterSearchQuery, EncounterSearchCriteria> {

    @Override
    public EncounterSearchQuery translate(EncounterSearchCriteria criteria) {
        return EncounterSearchQuery.builder()
                .encounterId(token(criteria.getId()))
                .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
                .date(date(criteria.getDate(), d -> d == null ? null :
                        d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .build();
    }
}
