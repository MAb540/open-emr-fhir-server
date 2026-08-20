package org.example.basicfhirserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalObservation {

    String id;

    String patientId;

    String encounterId;

    LocalDateTime effectiveDateTime;

    BigDecimal value;

    String unit;

    String code;

    String display;

    String status;

    String profile;

    String practitionerId;

    LocalDateTime lastUpdated;

    String version;

    Map<String, VitalObservationComponent> components;


    public record VitalObservationComponent(
            BigDecimal value
//            String unit,
//            String code,
//            String display
    ) {}
}

