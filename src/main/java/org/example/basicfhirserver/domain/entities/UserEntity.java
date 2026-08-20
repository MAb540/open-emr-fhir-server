package org.example.basicfhirserver.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping for the openEMR {@code users} table.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "authorized")
    private Integer authorized;

    @Column(name = "info")
    private String info;

    @Column(name = "source")
    private Integer source;

    @Column(name = "fname")
    private String fname;

    @Column(name = "mname")
    private String mname;

    @Column(name = "lname")
    private String lname;

    @Column(name = "federaltaxid")
    private String federaltaxid;

    @Column(name = "federaldrugid")
    private String federaldrugid;

    @Column(name = "upin")
    private String upin;

    @Column(name = "facility")
    private String facility;

    @Column(name = "facility_id")
    private Integer facilityId;

    @Column(name = "see_auth")
    private Integer seeAuth;

    @Column(name = "active")
    private Integer active;

    @Column(name = "npi")
    private String npi;

    @Column(name = "title")
    private String title;

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "billname")
    private String billname;

    @Column(name = "email")
    private String email;

    @Column(name = "url")
    private String url;

    @Column(name = "assistant")
    private String assistant;

    @Column(name = "organization")
    private String organization;

    @Column(name = "valedictory")
    private String valedictory;

    @Column(name = "street")
    private String street;

    @Column(name = "streetb")
    private String streetb;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip")
    private String zip;

    @Column(name = "street2")
    private String street2;

    @Column(name = "streetb2")
    private String streetb2;

    @Column(name = "city2")
    private String city2;

    @Column(name = "state2")
    private String state2;

    @Column(name = "zip2")
    private String zip2;

    @Column(name = "phone")
    private String phone;

    @Column(name = "fax")
    private String fax;

    @Column(name = "phonew1")
    private String phonew1;

    @Column(name = "phonew2")
    private String phonew2;

    @Column(name = "phonecell")
    private String phonecell;

    @Column(name = "notes")
    private String notes;

    @Column(name = "cal_ui")
    private Integer calUi;

    @Column(name = "taxonomy")
    private String taxonomy;

    @Column(name = "calendar")
    private Integer calendar;

    @Column(name = "abook_type")
    private String abookType;

    @Column(name = "default_warehouse")
    private String defaultWarehouse;

    @Column(name = "irnpool")
    private String irnpool;

    @Column(name = "state_license_number")
    private String stateLicenseNumber;

    @Column(name = "newcrop_user_role")
    private String newcropUserRole;

    @Column(name = "email_direct")
    private String emailDirect;

    @Column(name = "physician_type")
    private String physicianType;

    @Column(name = "cpoe")
    private Integer cpoe;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "main_menu_role")
    private String mainMenuRole;

    @Column(name = "weno_prov_id")
    private String wenoProvId;

    @Column(name = "patient_menu_role")
    private String patientMenuRole;

    @Column(name = "portal_user")
    private Integer portalUser;

    @Column(name = "supervisor_id")
    private Integer supervisorId;

    @Column(name = "uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID uuid;

    @Column(name = "google_signin_email")
    private String googleSigninEmail;

    @Column(name = "billing_facility")
    private String billingFacility;

    @Column(name = "billing_facility_id")
    private Integer billingFacilityId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "country_code2")
    private String countryCode2;
}
