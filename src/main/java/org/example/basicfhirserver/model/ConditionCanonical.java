package org.example.basicfhirserver.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionCanonical {
    String id;
    String patientUuid;
    String category;
    String clinicalStatus;
    String verificationStatus;
    String title;
    String diagnosis;
    String comments;
    LocalDateTime onsetDate;
    LocalDateTime begDate;
    LocalDateTime endDate;
    LocalDateTime lastUpdated;

    // Encounter-specific elements (nullable)
    String encounterUuid;
    String creatorUuid;
    String creatorNpi;
    String updatorUuid;
    Integer resolved;

    // Health concern-specific elements (nullable)
    String healthConcernSubtype;
    String healthConcernSubtypeTitle;


}