package org.example.basicfhirserver.mapper.utils;


public final class FhirCodeSystemConstants {

    public static final String SNOMED_CT = "http://snomed.info/sct";
    public static final String NUCC_PROVIDER = "http://nucc.org/provider-taxonomy";
    public static final String DATA_ABSENT_REASON_EXTENSION = "http://hl7.org/fhir/StructureDefinition/data-absent-reason";
    public static final String DATA_ABSENT_REASON_CODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/data-absent-reason";

    // @see http://hl7.org/fhir/R4/valueset-immunization-status-reason.html
    public static final String IMMUNIZATION_STATUS_REASON = "http://hl7.org/fhir/ValueSet/immunization-status-reason";

    public static final String IMMUNIZATION_OBJECTION_REASON = "http://terminology.hl7.org/CodeSystem/v3-ActReason";
    public static final String UNITS_OF_MEASURE = "http://unitsofmeasure.org";

    public static final String PROVIDER_NPI = "http://hl7.org/fhir/sid/us-npi";

    public static final String HL7_SYSTEM_CAREPLAN_CATEGORY = "http://hl7.org/fhir/us/core/CodeSystem/careplan-category";

    public static final String LOINC = "http://loinc.org";

    public static final String HL7_OBSERVATION_CATEGORY = "http://terminology.hl7.org/CodeSystem/observation-category";

    // @see https://www.hl7.org/fhir/us/core/ValueSet-us-core-documentreference-category.html
    public static final String DOCUMENT_REFERENCE_CATEGORY = "http://hl7.org/fhir/us/core/CodeSystem/us-core-documentreference-category";

    // @see https://terminology.hl7.org/1.0.0//CodeSystem-v3-NullFlavor.html
    public static final String HL7_NULL_FLAVOR = "http://terminology.hl7.org/CodeSystem/v3-NullFlavor";

    // @see http://hl7.org/fhir/R4/valueset-formatcodes.html
    // @see https://profiles.ihe.net/fhir/ihe.formatcode.fhir/background.html
    public static final String IHE_FORMATCODE_CODESYSTEM = "http://ihe.net/fhir/ValueSet/IHE.FormatCode.codesystem";

    public static final String DIAGNOSTIC_SERVICE_SECTION_ID = "http://terminology.hl7.org/CodeSystem/v2-0074";

    // @see http://oid-info.com/get/2.16.840.1.113883.4.7
    public static final String OID_CLINICAL_LABORATORY_IMPROVEMENT_ACT_NUMBER = "urn:oid:2.16.840.1.113883.4.7";

    public static final String HL7_IDENTIFIER_TYPE_TABLE = "http://hl7.org/fhir/v2/0203";

    public static final String HL7_ORGANIZATION_TYPE = "http://terminology.hl7.org/CodeSystem/organization-type";

    // @see http://hl7.org/fhir/R4/valueset-observation-interpretation.html
    public static final String HL7_V3_OBSERVATION_INTERPRETATION = "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation";

    public static final String HL7_ICD10 = "http://hl7.org/fhir/sid/icd-10";

    public static final String HL7_V3_ACT_CODE = "http://terminology.hl7.org/CodeSystem/v3-ActCode";

    public static final String HL7_PARTICIPATION_TYPE = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";

    public static final String RFC_3986 = "urn:ietf:rfc:3986";

    public static final String HL7_DISCHARGE_DISPOSITION = "http://terminology.hl7.org/CodeSystem/discharge-disposition";

    public static final String RXNORM = "http://www.nlm.nih.gov/research/umls/rxnorm";

    // TODO: this is referenced a few places in HL7 as the NDC system, but does not appear to be officially documented
    public static final String NDC = "http://hl7.org/fhir/sid/ndc";

    public static final String HL7_MEDICATION_REQUEST_CATEGORY = "http://terminology.hl7.org/CodeSystem/medicationrequest-category";
    public static final String NCIMETA_NCI_NIH = "http://ncimeta.nci.nih.gov";

    public static final String OID_RACE_AND_ETHNICITY = "urn:oid:2.16.840.1.113883.6.238";

    public static final String HL7_US_CORE_RACE = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race";

    public static final String LANGUAGE_BCP_47 = "urn:ietf:bcp:47";

    /**
     * Required for US Core CareTeam Role.  Requires UMLS subscription to view valueset
     * available here: https://vsac.nlm.nih.gov/valueset/2.16.840.1.113762.1.4.1099.27/expansion/Latest
     */
    public static final String CARE_TEAM_MEMBER_FUNCTION_SNOMEDCT = "2.16.840.1.113762.1.4.1099.27";

    /**
     * Required for Structured Data Collection (SDC) Task implementations
     *
     * @see https://build.fhir.org/ig/HL7/sdc/ValueSet-task-code.html
     */
    public static final String HL7_SDC_TASK_TEMP = "https://build.fhir.org/ig/HL7/sdc/CodeSystem-temp.html";

    public static final String HL7_SDC_TASK_SERVICE_REQUEST = "http://hl7.org/fhir/CodeSystem/task-code";

    /**
     * CPT4 code sets are owned by the American Medical Association (AMA)
     */
    public static final String AMA_CPT = "http://www.ama-assn.org/go/cpt";

    public static final String HL7_CATEGORY_OBSERVATION = "http://terminology.hl7.org/CodeSystem/observation-category";

    public static final String HL7_US_CORE_CATEGORY_OBSERVATION = "http://hl7.org/fhir/us/core/CodeSystem/us-core-category";

    public static final String INDUSTRY_NAICS_DETAIL_ODH = "urn:oid:2.16.840.1.114222.4.11.7900";

    public static final String OCCUPATION_ODH = "urn:oid:2.16.840.1.114222.4.11.7901";

    public static final String SPECIMEN_IDENTIFIER = "http://terminology.hl7.org/CodeSystem/v2-0203";

    // HL7 v2 table 0487: Specimen Type
    public static final String SPECIMEN_TYPE = "http://terminology.hl7.org/CodeSystem/v2-0487";

    // HL7 v2 table 0488: Specimen Collection Method
    public static final String SPECIMEN_COLLECTION_METHOD = "http://terminology.hl7.org/CodeSystem/v2-0488";

    // HL7 v2 table 0493: Specimen Condition
    public static final String SPECIMEN_CONDITION = "http://terminology.hl7.org/CodeSystem/v2-0493";

    // HL7 v2 table 0371: Additive/Preservative
    public static final String SPECIMEN_ADDITIVE_PRESERVATIVE = "http://terminology.hl7.org/CodeSystem/v2-0371";

    // HL7 v2 table 0070: Specimen Source Codes (legacy)
    public static final String SPECIMEN_SOURCE = "http://terminology.hl7.org/CodeSystem/v2-0070";

    // FHIR core: Specimen Status (availability)
    public static final String SPECIMEN_STATUS = "http://hl7.org/fhir/specimen-status";

    public static final String HL7_CONDITION_CATEGORY = "http://terminology.hl7.org/CodeSystem/condition-category";

    public static final String HL7_CONDITION_CATEGORY_3_1_1 = " http://hl7.org/fhir/us/core/CodeSystem/condition-category";

    // NCI 2025-04 release of NCI Thesaurus
    public static final String NCI_THESAURUS = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";

    public static final String HL7_TIMING_ABBREVIATION = "http://terminology.hl7.org/CodeSystem/v3-GTSAbbreviation";

    public static final String HL7_ROLE_CODE = "http://terminology.hl7.org/CodeSystem/role-code";

    public static final String HSOC = "https://www.cdc.gov/nhsn/cdaportal/terminology/codesystem/hsloc.html";
    public static final String CMS_PLACE_OF_SERVICE = " https://www.cms.gov/Medicare/Coding/place-of-service-codes/Place_of_Service_Code_Set";
}
