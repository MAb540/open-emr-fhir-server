package org.example.basicfhirserver.repository.jdbc.allergy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder()
public class AllergyDBRecord {
    Long id;
    LocalDateTime date;
    String type;
    String title;
    LocalDateTime begdate;
    LocalDateTime enddate;
    LocalDate returndate;
    Integer occurrence;
    Integer classification;
    String referredby;
    String extrainfo;
    String diagnosis;
    Integer activity;
    String comments;
    Long pid;
    String user;
    String groupname;
    Integer outcome;
    String destination;
    Long reinjuryId;
    String injuryPart;
    String injuryType;
    String injuryGrade;
    String reaction;
    Integer externalAllergyid;
    String erxSource;
    String erxUploaded;
    LocalDateTime modifydate;
    String severityAl;
    String externalId;
    String subtype;
    String listOptionId;
    UUID uuid;
    String verification;
    String udi;
    String udiData;

    Long patientId;
    String practitioner;
    String practitionerNpi;
    UUID practitionerUuid;
    String organization;
    UUID organizationUuid;
    UUID puuid;
    UUID patientUuid;
    UUID allergyUuid;
    String reactionTitle;
    String reactionCodes;
    String verificationTitle;
}
