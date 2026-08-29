package org.example.basicfhirserver.repository.jdbc.drug;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder()
public class DrugDBRecord {
    Long drugId;
    UUID uuid;
    String name;
    String ndcNumber;
    String form;
    String size;
    String unit;
    String route;
    String relatedCode;
    Integer active;
    String drugCode;
    String rxnormDrugcode;
    String manufacturer;
    String lotNumber;
    LocalDateTime expiration;
    LocalDateTime drugLastUpdated;
    LocalDateTime drugDateCreated;
}