package org.example.basicfhirserver.repository.jdbc.facility;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityDBRecord {
  private Long id;
  private UUID uuid;
  private String name;
  private String phone;
  private String fax;
  private String street;
  private String city;
  private String state;
  private String postalCode;
  private String countryCode;
  private String federalEin;
  private String website;
  private String email;
  private Integer serviceLocation;
  private Integer billingLocation;
  private Integer acceptsAssignment;
  private String posCode;
  private String x12SenderId;
  private String attn;
  private String domainIdentifier;
  private String facilityNpi;
  private String facilityTaxonomy;
  private String taxIdType;
  private String color;
  private Integer primaryBusinessEntity;
  private String facilityCode;
  private String extraValidation;
  private String mailStreet;
  private String mailStreet2;
  private String mailCity;
  private String mailState;
  private String mailZip;
  private String oid;
  private String iban;
  private String info;
  private Integer inactive;
  private LocalDateTime lastUpdated;
}
