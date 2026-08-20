package org.example.basicfhirserver.repository.jdbc.formencounter;

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
@Builder
public class FormEncounterDBRecord {
    private Long eid;
    private UUID euuid;
    private LocalDateTime date;
    private String reason;
    private LocalDate onsetDate;
    private String sensitivity;
    private String billingNote;
    private Long pcCatid;
    private Integer lastLevelBilled;
    private Integer lastLevelClosed;
    private LocalDate lastStmtDate;
    private Integer stmtCount;
    private Long supervisorId;
    private String invoiceRefno;
    private String referralSource;
    private Long billingFacility;
    private String externalId;
    private LocalDateTime lastUpdate;
    private String posCode;
    private String classCode;

    private String classTitle;
    private String pcCatname;

    private Long pid;
    private UUID puuid;

    private Long facilityId;
    private UUID facilityUuid;
    private String facilityName;
    private UUID facilityLocationUuid;

    private Long billingFacilityId;
    private UUID billingFacilityUuid;
    private String billingFacilityName;
    private UUID billingLocationUuid;

    private Long providerId;
    private Long referringProviderId;
    private Long orderingProviderId;

    private UUID providerUuid;
    private String providerUsername;
    private UUID referrerUuid;
    private String referrerUsername;

    private String dischargeDisposition;
    private String dischargeDispositionText;

}