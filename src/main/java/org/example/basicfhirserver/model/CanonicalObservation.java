package org.example.basicfhirserver.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
public class CanonicalObservation {

    String id;
    String version;
    String status;
    LocalDateTime lastUpdated;

    String patientUuid;
    String encounterUuid;
    String practitionerUuid;

    String profile;
    List<String> categories;

    String code;
    String display;
    String codeSystem;

    LocalDateTime effectiveDateTime;

    ObservationValue value;

    List<ObservationComponent> components;

    @Value
    @Builder
    public static class ObservationValue {
        Double numericValue;
        String unit;
        String unitSystem; // e.g., "http://unitsofmeasure.org" (UCUM)

        String stringValue;
        Map<String, Map<String, String>> codedValues;
    }

    @Value
    @Builder
    public static class ObservationComponent {
        String code;
        String display;
        String codeSystem;
        ObservationValue value;
    }
}
