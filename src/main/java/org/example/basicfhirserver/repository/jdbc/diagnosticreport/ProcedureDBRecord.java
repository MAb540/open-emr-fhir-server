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
public class ProcedureDBRecord {

    UUID orderUuid;
    UUID uuid;
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
    LocalDateTime reportDate;
    Long procedureReportId;
    UUID reportUuid;
    String reportNotes;
    Integer procedureOrderSeq;
    Long procedureResultId;
    UUID resultUuid;
    String resultCode;
    String resultText;
    String resultUnits;
    String resultResult;
    String resultRange;
    String resultAbnormal;
    String resultAbnormalTitle;
    String resultAbnormalCodes;
    String resultComments;
    String resultStatus;
    String procedureName;
    String procedureCode;
    String procedureType;
    Integer orderCodeSeq;
    String diagnoses;
    String standardCode;
    Long labId;
    UUID labUuid;
    String labNpi;
    String labName;
    UUID labDirectorUuid;
    String labDirectorNpi;
    UUID puuid;
    Long pid;
    Long patientId;
    Long eid;
    UUID euuid;
    LocalDateTime encounterDate;
    Long docId;
    UUID docUuid;
    UUID providerUuid;
    String providerFname;
    String providerMname;
    String providerLname;
    String providerNpi;
    UUID locationUuid;
    String locationName;
}
