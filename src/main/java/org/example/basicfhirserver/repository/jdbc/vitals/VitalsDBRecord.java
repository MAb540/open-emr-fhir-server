package org.example.basicfhirserver.repository.jdbc.vitals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalsDBRecord {
    UUID vitalsUuid;
    UUID patientUuid;
    UUID encounterUuid;
    UUID practitionerUuid;

    UUID observationUuid;
    LocalDateTime effectiveDateTime;

    BigDecimal weight;
    BigDecimal height;
    BigDecimal temperature;
    BigDecimal bmi;

    BigDecimal systolic;
    BigDecimal diastolic;

    String resourcePath;

    LocalDateTime lastUpdated;
}
