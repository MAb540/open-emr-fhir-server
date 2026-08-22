package org.example.basicfhirserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Practitioner {
    private Long id;
    private UUID uuid;
    private Boolean active;
    private Instant dateCreated;
    private Instant lastUpdated;

    // Name details
    private String firstName;
    private String middleName;
    private String lastName;
    private String prefix; // Maps from title (e.g. Dr.)
    private String suffix;

    // Direct Identifiers
    private String npi;
    private String upin;
    private String stateLicenseNumber;
    private String federalTaxId;
    private String federalDrugId;
    private String taxonomyCode;

    // Structured Contact Lists
    private List<TelecomItem> telecoms;
    private List<AddressItem> addresses;

    // Reference Metadata
    private String physicianType;
    private String physicianTypeTitle;
    private String physicianTypeCode;
    private String abookTitle;

    public String getA() {
        return null;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TelecomItem {
        private String value;
        private String use;    // e.g., work, mobile, home
        private String system; // e.g., phone, fax, email, url
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressItem {
        private String use; // e.g., work, billing
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
