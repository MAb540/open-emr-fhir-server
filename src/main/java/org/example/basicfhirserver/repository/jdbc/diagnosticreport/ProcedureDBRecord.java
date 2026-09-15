package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder()
public class ProcedureDBRecord {
    // 1. Parent Order/Procedure Fields
    String procedureName;
    String procedureCode;
    UUID uuid;
    UUID orderUuid;
    Long procedureOrderId;
    Long orderProviderId;
    Integer orderActivity;
    Integer activity;
    String orderDiagnosis;
    Long orderEncounterId;
    Long orderLabId;
    Long orderPatientId;
    Long providerId;
    LocalDateTime dateOrdered;
    LocalDateTime dateCollected;
    String orderStatus;
    String orderPriority;
    String patientInstructions;
    String clinicalHx;
    String procedureOrderType;
    LocalDateTime scheduledDate;
    LocalDateTime scheduledStart;
    LocalDateTime scheduledEnd;
    String performerType;
    String orderIntent;
    Long locationId;
    Integer specimenFasting;

    String diagnoses;
    String standardCode;

    // Attributed References (Flattened / Embedded Structures)
    ProviderInfo provider;
    FacilityInfo location;
    LabMetadataInfo lab;
    PatientReferenceInfo patient;
    EncounterReferenceInfo encounter;

    // 2. The Hierarchical Tree Nesting Target
    List<ReportBlock> reports;

    // --- Embedded Component Definitions ---
    @Value
    @Builder
    public static class ProviderInfo {
        Long id;
        UUID uuid;
        String fname;
        String mname;
        String lname;
        String npi;
    }

    @Value
    @Builder
    public static class FacilityInfo {
        Long id;
        UUID uuid;
        String name;
    }

    @Value
    @Builder
    public static class LabMetadataInfo {
        Long id;
        UUID uuid;
        String name;
        String npi;
        UUID directorUuid;
        String directorNpi;
    }

    @Value
    @Builder
    public static class PatientReferenceInfo {
        Long pid;
        UUID uuid;
    }

    @Value
    @Builder
    public static class EncounterReferenceInfo {
        Long id;
        UUID uuid;
        LocalDateTime date;
    }

    @Value
    @Builder(toBuilder = true)
    public static class ReportBlock {
        Long id;
        UUID uuid;
        LocalDateTime date;
        String notes;
        Integer orderSeq;
        List<ResultBlock> results;
        List<SpecimenBlock> specimens;
    }

    @Value
    @Builder
    public static class ResultBlock {
        Long id;
        UUID uuid;
        String code;
        String text;
        String units;
        String result;
        String range;
        String abnormal;
        String resultAbnormalTitle;
        String resultAbnormalCodes;
        String comments;
        Long documentId;
        String status;
    }

    @Value
    @Builder
    public static class SpecimenBlock {
        UUID uuid;
        String identifier;
        String accession;
        String typeCode;
        String type;
        String methodCode;
        String method;
        String locationCode;
        String location;
        LocalDateTime collectedDate;
        LocalDateTime collectionStart;
        LocalDateTime collectionEnd;
        Double volume;
        String volumeUnit;
        String conditionCode;
        String specimenCondition;
        String comments;
        Integer deleted;
    }
}
