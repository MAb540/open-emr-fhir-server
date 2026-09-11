package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

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
public class ClinicalNotesDBRecord {
    Long id;
    UUID uuid;
    Integer activity;
    LocalDateTime date;
    String code;
    String codetext;
    String description;
    String externalId;
    String clinicalNotesType;
    String noteRelatedTo;
    String clinicalNotesCategory;
    LocalDateTime lastUpdated;
    LocalDateTime dateCreated;
    String categoryCode;
    String categoryTitle;
    Long pid;
    UUID puuid;
    Long eid;
    UUID euuid;
    LocalDateTime encounterDate;
    String username;
    String userUuid;
    String npi;
    String physicianType;
}
