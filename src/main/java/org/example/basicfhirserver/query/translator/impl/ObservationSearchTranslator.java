package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.observation.ObservationSearchCriteria;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.example.basicfhirserver.query.translator.utils.TranslatorUtils;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.date;

@Component
public class ObservationSearchTranslator implements SearchTranslator<ObservationSearchQuery, ObservationSearchCriteria> {

    @Override
    public ObservationSearchQuery translate(ObservationSearchCriteria criteria) {
        return ObservationSearchQuery.builder()
                .patientId(criteria.getPatient() == null ? null : List.of(criteria.getPatient().getIdPart()))
                .category(criteria.getCategory() == null ? null : criteria.getCategory().getValue())
                .codes(
                        criteria.getCodes() == null
                                ? List.of()
                                : criteria.getCodes().getValuesAsQueryTokens()
                                .stream()
                                .map(TranslatorUtils::tokenWithSystem)
                                .toList()
                )
                .date(date(criteria.getDate(), d -> d == null ? null :
                        d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .lastUpdated(date(criteria.getLastUpdated(), d -> d == null ? null :
                        d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .count(criteria.getCount())
                .offset(criteria.getOffset())
                .build();
    }


}
