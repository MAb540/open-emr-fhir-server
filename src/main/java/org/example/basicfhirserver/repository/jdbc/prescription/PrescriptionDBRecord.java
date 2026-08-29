package org.example.basicfhirserver.repository.jdbc.prescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrescriptionDBRecord {
    UUID uuid;
    String sourceTable;
    String drug;
    String active;
    String intent;
    String category;
    String intentTitle;
    String categoryTitle;
    String categoryText;
    String rxnormDrugcode;
    LocalDateTime dateAdded;
    LocalDateTime dateModified;
    String unit;
    String interval;
    String route;
    String note;
    String status;
    String dosage;
    String drugDosageInstructions;
    LocalDateTime medicationAdherenceDateAsserted;
    String prescriptionDrugSize;
    String quantity;
    String diagnosis;

    UUID puuid;
    UUID euuid;
    UUID pruuid;
    String drugUuid;

    String routeId;
    String routeTitle;
    String routeCodes;

    String unitId;
    String unitTitle;
    String unitCodes;

    String intervalId;
    String intervalTitle;
    String intervalCodes;
    String intervalNotes;

    String medicationAdherence;
    String medicationAdherenceTitle;
    String medicationAdherenceCodes;

    String medicationAdherenceInformationSource;
    String medicationAdherenceInformationSourceTitle;
    String medicationAdherenceInformationSourceCodes;

    String reportingSourceRecordId;
    String reportingSourceUuid;
    String reportingSourceType;
    String reportingSourceAbookType;

    UUID organizationUuid;
    String isPrimaryRecord;
}
