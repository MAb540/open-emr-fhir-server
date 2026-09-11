package org.example.basicfhirserver.repository.jdbc.condition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder()
public class ConditionEncounterDiagnosisDBRecord {
    Long id;
    UUID uuid;
    String listsUuid;
    Long pid;
    LocalDateTime modifydate;
    String type;
    String title;
    LocalDateTime begdate;
    LocalDateTime enddate;
    String diagnosis;
    Integer activity;
    String comments;
    Integer occurrence;
    Integer outcome;
    String verification;
    LocalDateTime date;
    UUID encounterUuid;
    Long encounterId;
    LocalDateTime encounterDate;
    UUID creatorUuid;
    String creatorNpi;
    UUID updatorUuid;
    String updatorNpi;
    Integer resolved;
    UUID puuid;
    LocalDateTime lastUpdatedTime;
}
