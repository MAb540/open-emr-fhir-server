package org.example.basicfhirserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PractitionerRole {
    private Long roleId;
    private UUID roleUuid;
    private Long providerId;
    private UUID providerUuid;
    private String fullName;
    private Instant lastUpdated;
    private UUID locationUuid;
    private UUID facilityUuid;
    private String facilityName;
    private ContactConfig contactInfo;
    private RoleConfig roleInfo;
    private SpecialtyConfig specialtyInfo;
    private PhysicianTypeConfig physicianTypeInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactConfig {
        private TelecomItem phone;
        private TelecomItem fax;
        private TelecomItem email;
        private TelecomItem url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TelecomItem {
        private String value;
        private String use;
        private String system;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleConfig {
        private String code;
        private String title;
        private Instant lastUpdated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpecialtyConfig{
        private String code;
        private String title;
        private Instant lastUpdated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PhysicianTypeConfig {
        private String codes;
        private String type;
        private String title;
    }
}