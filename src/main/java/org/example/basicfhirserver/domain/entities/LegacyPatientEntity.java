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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping for the openEMR {@code patient_data} table.
 */
@Entity
@Table(name = "patient_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegacyPatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID uuid;

    @Column(name = "title")
    private String title;

    @Column(name = "language")
    private String language;

    @Column(name = "financial")
    private String financial;

    @Column(name = "fname")
    private String fname;

    @Column(name = "lname")
    private String lname;

    @Column(name = "mname")
    private String mname;

    @Column(name = "DOB")
    private LocalDate dob;

    @Column(name = "street")
    private String street;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "drivers_license")
    private String driversLicense;

    @Column(name = "ss")
    private String ss;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "phone_home")
    private String phoneHome;

    @Column(name = "phone_biz")
    private String phoneBiz;

    @Column(name = "phone_contact")
    private String phoneContact;

    @Column(name = "phone_cell")
    private String phoneCell;

    @Column(name = "pharmacy_id")
    private Integer pharmacyId;

    @Column(name = "status")
    private String status;

    @Column(name = "contact_relationship")
    private String contactRelationship;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "sex")
    private String sex;

    @Column(name = "referrer")
    private String referrer;

    @Column(name = "referrerID")
    private String referrerId;

    @Column(name = "providerID")
    private Integer providerId;

    @Column(name = "ref_providerID")
    private Integer refProviderId;

    @Column(name = "email")
    private String email;

    @Column(name = "email_direct")
    private String emailDirect;

    @Column(name = "ethnoracial")
    private String ethnoracial;

    @Column(name = "race")
    private String race;

    @Column(name = "ethnicity")
    private String ethnicity;

    @Column(name = "religion")
    private String religion;

    @Column(name = "interpreter")
    private String interpreter;

    @Column(name = "interpreter_needed")
    private String interpreterNeeded;

    @Column(name = "migrantseasonal")
    private String migrantseasonal;

    @Column(name = "family_size")
    private String familySize;

    @Column(name = "monthly_income")
    private String monthlyIncome;

    @Column(name = "billing_note")
    private String billingNote;

    @Column(name = "homeless")
    private String homeless;

    @Column(name = "financial_review")
    private LocalDateTime financialReview;

    @Column(name = "pubpid")
    private String pubpid;

    @Column(name = "pid")
    private Long pid;

    @Column(name = "genericname1")
    private String genericname1;

    @Column(name = "genericval1")
    private String genericval1;

    @Column(name = "genericname2")
    private String genericname2;

    @Column(name = "genericval2")
    private String genericval2;

    @Column(name = "hipaa_mail")
    private String hipaaMail;

    @Column(name = "hipaa_voice")
    private String hipaaVoice;

    @Column(name = "hipaa_notice")
    private String hipaaNotice;

    @Column(name = "hipaa_message")
    private String hipaaMessage;

    @Column(name = "hipaa_allowsms")
    private String hipaaAllowSms;

    @Column(name = "hipaa_allowemail")
    private String hipaaAllowEmail;

    @Column(name = "squad")
    private String squad;

    @Column(name = "fitness")
    private Integer fitness;

    @Column(name = "referral_source")
    private String referralSource;

    @Column(name = "usertext1")
    private String usertext1;

    @Column(name = "usertext2")
    private String usertext2;

    @Column(name = "usertext3")
    private String usertext3;

    @Column(name = "usertext4")
    private String usertext4;

    @Column(name = "usertext5")
    private String usertext5;

    @Column(name = "usertext6")
    private String usertext6;

    @Column(name = "usertext7")
    private String usertext7;

    @Column(name = "usertext8")
    private String usertext8;

    @Column(name = "userlist1")
    private String userlist1;

    @Column(name = "userlist2")
    private String userlist2;

    @Column(name = "userlist3")
    private String userlist3;

    @Column(name = "userlist4")
    private String userlist4;

    @Column(name = "userlist5")
    private String userlist5;

    @Column(name = "userlist6")
    private String userlist6;

    @Column(name = "userlist7")
    private String userlist7;

    @Column(name = "pricelevel")
    private String pricelevel;

    @Column(name = "regdate")
    private LocalDateTime regdate;

    @Column(name = "contrastart")
    private LocalDate contrastart;

    @Column(name = "completed_ad")
    private String completedAd;

    @Column(name = "ad_reviewed")
    private LocalDateTime adReviewed;

    @Column(name = "advance_directive_user_authenticator")
    private Long advanceDirectiveUserAuthenticator;

    @Column(name = "vfc")
    private String vfc;

    @Column(name = "mothersname")
    private String mothersname;

    @Column(name = "guardiansname")
    private String guardiansname;

    @Column(name = "allow_imm_reg_use")
    private String allowImmRegUse;

    @Column(name = "allow_imm_info_share")
    private String allowImmInfoShare;

    @Column(name = "allow_health_info_ex")
    private String allowHealthInfoEx;

    @Column(name = "allow_patient_portal")
    private String allowPatientPortal;

    @Column(name = "deceased_date")
    private LocalDateTime deceasedDate;

    @Column(name = "deceased_reason")
    private String deceasedReason;

    @Column(name = "soap_import_status")
    private Integer soapImportStatus;

    @Column(name = "cmsportal_login")
    private String cmsportalLogin;

    @Column(name = "care_team_provider")
    private String careTeamProvider;

    @Column(name = "care_team_facility")
    private String careTeamFacility;

    @Column(name = "care_team_status")
    private String careTeamStatus;

    @Column(name = "county")
    private String county;

    @Column(name = "industry")
    private String industry;

    @Column(name = "imm_reg_status")
    private String immRegStatus;

    @Column(name = "imm_reg_stat_effdate")
    private String immRegStatEffdate;

    @Column(name = "publicity_code")
    private String publicityCode;

    @Column(name = "publ_code_eff_date")
    private String publCodeEffDate;

    @Column(name = "protect_indicator")
    private String protectIndicator;

    @Column(name = "prot_indi_effdate")
    private String protIndiEffdate;

    @Column(name = "guardianrelationship")
    private String guardianrelationship;

    @Column(name = "guardiansex")
    private String guardiansex;

    @Column(name = "guardianaddress")
    private String guardianaddress;

    @Column(name = "guardiancity")
    private String guardiancity;

    @Column(name = "guardianstate")
    private String guardianstate;

    @Column(name = "guardianpostalcode")
    private String guardianpostalcode;

    @Column(name = "guardiancountry")
    private String guardiancountry;

    @Column(name = "guardianphone")
    private String guardianphone;

    @Column(name = "guardianworkphone")
    private String guardianworkphone;

    @Column(name = "guardianemail")
    private String guardianemail;

    @Column(name = "sexual_orientation")
    private String sexualOrientation;

    @Column(name = "gender_identity")
    private String genderIdentity;

    @Column(name = "birth_fname")
    private String birthFname;

    @Column(name = "birth_lname")
    private String birthLname;

    @Column(name = "birth_mname")
    private String birthMname;

    @Column(name = "dupscore")
    private Integer dupscore;

    @Column(name = "name_history")
    private String nameHistory;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "street_line_2")
    private String streetLine2;

    @Column(name = "patient_groups")
    private String patientGroups;

    @Column(name = "prevent_portal_apps")
    private String preventPortalApps;

    @Column(name = "provider_since_date")
    private String providerSinceDate;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "preferred_name")
    private String preferredName;

    @Column(name = "nationality_country")
    private String nationalityCountry;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "tribal_affiliations")
    private String tribalAffiliations;

    @Column(name = "sex_identified")
    private String sexIdentified;

    @Column(name = "pronoun")
    private String pronoun;
}
