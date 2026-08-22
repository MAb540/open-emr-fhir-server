package org.example.basicfhirserver.repository.jdbc.user;

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
public class UserDBRecord {
    private Long id;
    private String username;
    private String password;
    private Integer authorized;
    private String info;
    private String source;
    private String fname;
    private String mname;
    private String lname;
    private String federaltaxid;
    private String federaldrugid;
    private String upin;
    private String facility;
    private Integer facilityId;
    private Integer seeAuth;
    private Integer active;
    private String npi;
    private String title;
    private String specialty;
    private String billname;
    private String email;
    private String url;
    private String assistant;
    private String organization;
    private String valedictory;
    private String street;
    private String streetb;
    private String city;
    private String state;
    private String zip;
    private String street2;
    private String streetb2;
    private String city2;
    private String state2;
    private String zip2;
    private String phone;
    private String fax;
    private String phonew1;
    private String phonew2;
    private String phonecell;
    private String notes;
    private String calUi;
    private String taxonomy;
    private Integer calendar;
    private String abookType;
    private String defaultWarehouse;
    private String irnpool;
    private String stateLicenseNumber;
    private String newcropUserRole;
    private String emailDirect;
    private String physicianType;
    private Integer cpoe;
    private String suffix;
    private String mainMenuRole;
    private String wenoProvId;
    private String patientMenuRole;
    private Integer portalUser;
    private Long supervisorId;
    private UUID uuid;
    private String googleSigninEmail;
    private String billingFacility;
    private Integer billingFacilityId;
    private Instant dateCreated;
    private Instant lastUpdated;
    private String countryCode;
    private String countryCode2;
    private String abookTitle;
    private String physicianTitle;
    private String physicianCode;
}