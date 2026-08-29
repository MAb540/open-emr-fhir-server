package org.example.basicfhirserver.repository.jdbc.prescription;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityDBRecord {
    Long id;
    UUID uuid;
    String name;
    String phone;
    String fax;
    String street;
    String city;
    String state;
    String postalCode;
    String countryCode;
    String federalEin;
    String website;
    String email;
    Integer serviceLocation;
    Integer billingLocation;
    Integer acceptsAssignment;
    String posCode;
    String x12SenderId;
    String attn;
    String domainIdentifier;
    String facilityNpi;
    String facilityTaxonomy;
    String taxIdType;
    String color;
    Integer primaryBusinessEntity;
    String facilityCode;
    String extraValidation;
    String mailStreet;
    String mailStreet2;
    String mailCity;
    String mailState;
    String mailZip;
    String oid;
    String iban;
    String info;
    Integer inactive;
}