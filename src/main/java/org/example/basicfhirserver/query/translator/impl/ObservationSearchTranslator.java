package org.example.basicfhirserver.query.translator.impl;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchCriteria;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class ObservationSearchTranslator implements SearchTranslator<ObservationSearchQuery, ObservationSearchCriteria> {

    @Override
    public ObservationSearchQuery translate(ObservationSearchCriteria criteria) {
        return ObservationSearchQuery.builder()
                .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
                .category(criteria.getCategory() == null ? null : criteria.getCategory().getValue())
                .codes(
                        criteria.getCodes() == null
                                ? List.of()
                                : criteria.getCodes().getValuesAsQueryTokens()
                                .stream()
                                .map(this::token)
                                .toList()
                )
                .date(convertDate(criteria.getDate(), d -> d == null ? null :
                        d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .lastUpdated(convertDate(criteria.getLastUpdated(), d -> d == null ? null :
                        d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .build();
    }

    private SearchValue<String> token(TokenParam param) {
        if (param == null) {
            return null;
        }

        return SearchValue.<String>builder()
                .system(param.getSystem())
                .value(param.getValue())
                .build();
    }

    private <T> SearchValue<T> convertDate(DateParam param, Function<Date, T> mapper) {
        if (param == null || param.getValue() == null || mapper == null) {
            return null;
        }

        T convertedValue = mapper.apply(param.getValue());

        if (param.getPrefix() == null) {
            return SearchValue.<T>builder()
                    .value(
                            convertedValue
                    ).build();
        }

        return SearchValue.<T>builder()
                .prefix(param.getPrefix())
                .value(
                        convertedValue
                ).build();
    }
}
