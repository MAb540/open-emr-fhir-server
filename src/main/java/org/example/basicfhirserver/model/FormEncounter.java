package org.example.basicfhirserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormEncounter {
    private Long id;
    private UUID uuid;
    private LocalDateTime date;
    private String reason;
    private LocalDate onsetDate;
    private String sensitivity;
    private String billingNote;
    private BillingStatus billingStatus;
    private PatientReference patient;
    private FacilityInfo serviceFacility;
    private FacilityInfo billingFacility;
    private ProviderTeam providers;
    private DischargeDetails discharge;
    private String externalId;
    private LocalDateTime lastUpdate;
    private EncounterClass encounterClass;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingStatus {
        private Long categoryId;
        private String categoryName;
        private Integer lastLevelBilled;
        private Integer lastLevelClosed;
        private LocalDate lastStatementDate;
        private Integer statementCount;
        private String invoiceReferenceNo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientReference {
        private Long id;
        private UUID uuid;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityInfo {
        private Long id;
        private UUID uuid;
        private String name;
        private UUID locationUuid;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderReference {
        private Long id;
        private UUID uuid;
        private String username;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderTeam {
        private ProviderReference primary;
        private ProviderReference referring;
        private Long orderingProviderId;
        private Long supervisorId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DischargeDetails {
        private String dispositionCode;
        private String dispositionText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncounterClass {
        private String code;
        private String title;
    }

}

