package org.example.basicfhirserver.query.translator.impl;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.example.basicfhirserver.query.resources.patient.PatientSearchCriteria;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

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

    private String token(TokenParam param) {
        return param == null ? null : param.getValue();
    }

    private SearchValue<String> stringMatch(StringParam param) {
        if (param == null || param.getValue() == null) {
            return null;
        }

        if (param.isContains() && param.getValue() != null) {
            return SearchValue.<String>builder()
                    .contains(param.isContains())
                    .value(
                            param.getValue()
                    ).build();
        }

        if (param.isExact() && param.getValue() != null) {
            return SearchValue.<String>builder()
                    .exact(param.isExact())
                    .value(
                            param.getValue()
                    ).build();
        }

        return SearchValue.<String>builder()
                .value(param.getValue())
                .build();
    }

    private <T> SearchValue<T> date(DateParam param, Function<Date, T> mapper) {
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

    private String mapGender(TokenParam gender) {

        if (gender == null)
            return null;

        return switch (gender.getValue().toLowerCase()) {

            case "male" -> "M";

            case "female" -> "F";

            case "other" -> "O";

            default -> "U";
        };
    }
}
