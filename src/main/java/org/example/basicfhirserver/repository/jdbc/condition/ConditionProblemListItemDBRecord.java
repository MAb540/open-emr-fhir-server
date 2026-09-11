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
public class ConditionProblemListItemDBRecord {
    Long id;
    UUID uuid;
    Long pid;
    LocalDateTime conditionDate;
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
    UUID puuid;
    LocalDateTime lastUpdatedTime;
}
