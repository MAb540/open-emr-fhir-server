package org.example.basicfhirserver.repository.jdbc.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleDBRecord {
    private Long id;
    private byte[] uuid;
    private String userName;
    private Long providerId;
    private byte[] providerUuid;
    private Instant providerLastUpdated;
    private byte[] locationUuid;
    private String workPhone;
    private String workPhoneUse;
    private String workPhoneSystem;
    private String fax;
    private String faxUse;
    private String faxSystem;
    private String email;
    private String emailUse;
    private String emailSystem;
    private String url;
    private String urlUse;
    private String urlSystem;
    private byte[] facilityUuid;
    private String facilityName;
    private String roleCode;
    private String roleTitle;
    private Instant roleLastUpdated;
    private String specialtyCode;
    private String specialtyTitle;
    private Instant specialtyLastUpdated;
    private String physicianTypeCodes;
    private String physicianType;
    private String physicianTypeTitle;
}