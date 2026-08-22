package org.example.basicfhirserver.query.translator.utils;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.example.basicfhirserver.query.resources.SearchValue;

import java.util.Date;
import java.util.function.Function;

public class TranslatorUtils {

    public static String token(TokenParam param) {
        return param == null ? null : param.getValue();
    }

    public static SearchValue<String> tokenWithSystem(TokenParam param) {
        if (param == null) {
            return null;
        }

        return SearchValue.<String>builder()
                .system(param.getSystem())
                .value(param.getValue())
                .build();
    }

    public static SearchValue<String> stringMatch(StringParam param) {
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

    public static <T> SearchValue<T> date(DateParam param, Function<Date, T> mapper) {
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

    private static String mapGender(TokenParam gender) {

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
