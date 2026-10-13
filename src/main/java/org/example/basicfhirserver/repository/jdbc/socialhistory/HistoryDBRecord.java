package org.example.basicfhirserver.repository.jdbc.socialhistory;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryDBRecord {
    Long id;
    UUID uuid;
    LocalDateTime date;
    String tobacco;
    String alcohol;
    String exercisePatterns;
    String recreationalDrugs;
    Long createdBy;
    Long pid;
    UUID puuid;
    UUID providerUuid;
    String providerNpi;
    String providerUsername;
    String smokingStatusCodes;
    int packsPerDay;
}