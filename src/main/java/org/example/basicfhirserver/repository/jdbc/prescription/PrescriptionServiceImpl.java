package org.example.basicfhirserver.repository.jdbc.prescription;

import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toBytes;
import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toUuid;

@Repository
public class PrescriptionServiceImpl implements PrescriptionService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public PrescriptionServiceImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    @Override
    public List<PrescriptionDBRecord> findById(UUID uuid) {

        StringBuilder sql = prescriptionQuery();

        sql.append(" AND combined_prescriptions.uuid= :uuid");
        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, prescriptionDBRecordRowMapper());
    }

    @Override
    public List<PrescriptionDBRecord> find(MedicationRequestSearchQuery medicationRequestSearchQuery) {

        StringBuilder sql = prescriptionQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();
        addFilter(sql, params, medicationRequestSearchQuery);

        return namedParameterJdbcTemplate.query(sql.toString(), params, prescriptionDBRecordRowMapper());
    }


    @Override
    public List<FacilityDBRecord> findFacility() {

        StringBuilder sql = facilityQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        sql.append("""
                AND primary_business_entity = :primary_business_entity
                """);
        params.addValue("primary_business_entity", 1);

        return namedParameterJdbcTemplate.query(sql.toString(), params, facilityDBRecordRowMapper());

    }


    private StringBuilder prescriptionQuery() {
        return new StringBuilder("""
                SELECT
                    combined_prescriptions.uuid
                    ,combined_prescriptions.source_table
                    ,combined_prescriptions.drug
                    ,combined_prescriptions.active
                    ,combined_prescriptions.intent
                    ,combined_prescriptions.category
                    ,combined_prescriptions.intent_title
                    ,combined_prescriptions.category_title
                    ,'Community' AS category_text
                    ,combined_prescriptions.rxnorm_drugcode
                    ,combined_prescriptions.date_added
                    ,combined_prescriptions.unit
                    ,combined_prescriptions.`interval`
                    ,combined_prescriptions.route
                    ,combined_prescriptions.note
                    ,combined_prescriptions.status
                    ,combined_prescriptions.dosage
                    ,combined_prescriptions.drug_dosage_instructions
                    ,combined_prescriptions.date_added
                    ,combined_prescriptions.date_modified
                    ,combined_prescriptions.medication_adherence_date_asserted
                    ,combined_prescriptions.prescription_drug_size
                    ,combined_prescriptions.quantity
                    ,combined_prescriptions.diagnosis
                    ,combined_prescriptions.is_primary_record
                    ,patient.puuid
                    ,encounter.euuid
                    ,practitioner.pruuid
                    ,drug_uuid
                
                    ,routes_list.route_id
                    ,routes_list.route_title
                    ,routes_list.route_codes
                
                    ,units_list.unit_id
                    ,units_list.unit_title
                    ,units_list.unit_codes
                
                    ,intervals_list.interval_id
                    ,intervals_list.interval_title
                    ,intervals_list.interval_codes
                    ,intervals_list.interval_notes
                
                    ,combined_prescriptions.medication_adherence
                    ,med_adherence.medication_adherence_title
                    ,med_adherence.medication_adherence_codes
                
                    ,combined_prescriptions.medication_adherence_information_source
                    ,med_adherence_source.medication_adherence_information_source_title
                    ,med_adherence_source.medication_adherence_information_source_codes
                    ,combined_prescriptions.reporting_source_record_id
                    ,reporting_source.reporting_source_uuid
                    ,reporting_source.reporting_source_type
                    ,reporting_source.reporting_source_abook_type
                    FROM (
                          SELECT
                                 prescriptions.uuid
                                ,'prescriptions' AS 'source_table'
                                ,prescriptions.drug
                                ,prescriptions.active
                                ,prescriptions.end_date
                                ,COALESCE(prescriptions.request_intent, 'order') AS intent
                                ,COALESCE(prescriptions.request_intent_title, 'Order') AS intent_title
                                ,COALESCE(prescriptions.usage_category, 'community') AS category
                                ,COALESCE(prescriptions.usage_category_title, 'Home/Community') as category_title
                                ,IF(prescriptions.rxnorm_drugcode!=''
                                    ,prescriptions.rxnorm_drugcode
                                    ,IF(drugs.drug_code IS NULL, '', drugs.drug_code)
                                ) AS 'rxnorm_drugcode'
                                ,date_added
                                ,date_modified
                                ,COALESCE(prescriptions.unit,drugs.unit) AS unit
                                ,prescriptions.`interval`
                                ,COALESCE(prescriptions.`route`,drugs.`route`) AS 'route'
                                ,prescriptions.size AS prescription_drug_size
                                ,prescriptions.`note`
                                ,patient_id
                                ,encounter
                                ,provider_id
                                ,drugs.uuid AS drug_uuid
                                ,prescriptions.drug_dosage_instructions
                                ,prescriptions.quantity
                                ,meds.medication_adherence_date_asserted
                                ,meds.medication_adherence
                                ,meds.medication_adherence_information_source
                                ,CASE
                                    WHEN prescriptions.end_date IS NOT NULL AND prescriptions.active = '1' THEN 'completed'
                                    WHEN prescriptions.active = '1' THEN 'active'
                                    ELSE 'stopped'
                                END as 'status'
                                ,prescriptions.dosage
                                ,diagnosis
                                ,meds.is_primary_record
                                ,meds.reporting_source_record_id
                        FROM
                            prescriptions
                        LEFT JOIN
                            drugs ON prescriptions.drug_id = drugs.drug_id
                        LEFT JOIN (
                            SELECT
                                id AS meds_id,
                                medication_adherence_information_source,
                                medication_adherence,
                                medication_adherence_date_asserted,
                                prescription_id AS meds_prescription_id,
                                is_primary_record,
                                reporting_source_record_id
                            FROM lists_medication
                        ) meds ON prescriptions.id = meds.meds_prescription_id
                        UNION
                        SELECT
                            lists.uuid
                            ,'lists' AS 'source_table'
                            ,lists.title AS drug
                            ,activity AS active
                            ,lists.enddate AS end_date
                            ,IF(lists_medication.request_intent IS NULL, 'plan', lists_medication.request_intent) AS intent
                            ,IF(lists_medication.request_intent_title IS NULL, 'Plan', lists_medication.request_intent_title) AS intent_title
                            ,lists_medication.usage_category AS category
                            ,lists_medication.usage_category_title AS category_title
                            -- we don't have rxnorm codes for free text meds
                            ,NULL AS rxnorm_drugcode
                            ,`date` AS date_added
                            ,`modifydate` AS date_modified
                            ,NULL as unit
                            ,NULL as 'interval'
                            ,NULL as `route`
                            ,NULL as `prescription_drug_size`
                            ,lists.comments as 'note'
                            ,pid AS patient_id
                            ,issues_encounter.issues_encounter_encounter as encounter
                            ,users.id AS provider_id
                            ,NULL as drug_uuid
                            ,lists_medication.drug_dosage_instructions
                            ,NULL as quantity
                            ,lists_medication.medication_adherence_date_asserted
                            ,lists_medication.medication_adherence
                            ,lists_medication.medication_adherence_information_source
                            ,CASE
                                    WHEN lists.enddate IS NOT NULL AND lists.activity = 1 THEN 'completed'
                                    WHEN lists.activity = 1 THEN 'active'
                                    ELSE 'stopped'
                            END as 'status'
                            ,NULL as dosage
                            ,diagnosis
                            ,is_primary_record
                            ,reporting_source_record_id
                        FROM
                            lists
                        LEFT JOIN
                                users ON users.username = lists.user
                        LEFT JOIN
                            lists_medication ON lists_medication.list_id = lists.id
                        LEFT JOIN
                        (
                           select
                                  pid AS issues_encounter_pid
                                , list_id AS issues_encounter_list_id
                                -- lists have a 0..* relationship with issue_encounters which is a problem as FHIR treats medications as a 0.1
                                -- we take the very first encounter that the issue was tied to.
                                , min(encounter) AS issues_encounter_encounter FROM issue_encounter GROUP BY pid,list_id
                        ) issues_encounter ON lists.pid = issues_encounter.issues_encounter_pid AND lists.id = issues_encounter.issues_encounter_list_id
                        WHERE
                            type = 'medication'
                            AND lists_medication.prescription_id IS NULL
                    ) combined_prescriptions
                    LEFT JOIN
                    (
                      SELECT
                        option_id AS route_id
                        ,title AS route_title
                        ,codes AS route_codes
                      FROM list_options
                      WHERE list_id='drug_route'
                    ) routes_list ON routes_list.route_id = combined_prescriptions.route
                    LEFT JOIN
                    (
                      SELECT
                        option_id AS interval_id
                        ,title AS interval_title
                        ,codes AS interval_codes
                        ,notes AS interval_notes
                      FROM list_options
                      WHERE list_id='drug_interval'
                    ) intervals_list ON intervals_list.interval_id = combined_prescriptions.interval
                    LEFT JOIN
                    (
                      SELECT
                        option_id AS unit_id
                        ,title AS unit_title
                        ,codes AS unit_codes
                      FROM list_options
                      WHERE list_id='drug_units'
                    ) units_list ON units_list.unit_id = combined_prescriptions.unit
                    LEFT JOIN
                    (
                      SELECT
                        option_id AS medication_adherence_id
                        ,title AS medication_adherence_title
                        ,codes AS medication_adherence_codes
                      FROM list_options
                      WHERE list_id='medication_adherence'
                    ) med_adherence ON med_adherence.medication_adherence_id = combined_prescriptions.medication_adherence
                    LEFT JOIN
                    (
                      SELECT
                        option_id AS medication_adherence_information_source_id
                        ,title AS medication_adherence_information_source_title
                        ,codes AS medication_adherence_information_source_codes
                      FROM list_options
                      WHERE list_id='medication_adherence'
                    ) med_adherence_source ON med_adherence_source.medication_adherence_information_source_id = combined_prescriptions.medication_adherence_information_source
                    LEFT JOIN (
                        select uuid AS puuid
                        ,pid
                        FROM patient_data
                    ) patient
                    ON patient.pid = combined_prescriptions.patient_id
                    LEFT JOIN (
                        SELECT
                            encounter,
                            uuid AS euuid
                        FROM form_encounter
                    ) encounter
                    ON encounter.encounter = combined_prescriptions.encounter
                    LEFT JOIN (
                        SELECT
                               id AS practitioner_id
                               ,uuid AS pruuid
                        FROM users
                        WHERE users.npi IS NOT NULL AND users.npi != ''
                    ) practitioner
                    ON practitioner.practitioner_id = combined_prescriptions.provider_id
                    LEFT JOIN (
                        SELECT
                        uuid AS reporting_source_uuid
                        ,'user' AS reporting_source_type
                        ,id AS reporting_source_user_id
                        ,abook_type AS reporting_source_abook_type
                        FROM users
                        WHERE npi IS NOT NULL AND npi != ''
                    ) reporting_source ON reporting_source.reporting_source_user_id = combined_prescriptions.reporting_source_record_id
                    WHERE 1 = 1
                """);
    }


    private void addFilter(
            StringBuilder sql,
            MapSqlParameterSource params,
            MedicationRequestSearchQuery medicationRequestSearchQuery
    ) {

        if (medicationRequestSearchQuery.getPatientId() != null) {
            String uuid = medicationRequestSearchQuery.getPatientId();
            byte[] binaryUuid = toBytes(UUID.fromString(uuid));
            sql.append(" AND patient.puuid = :patientUuid ");
            params.addValue("patientUuid", binaryUuid);
        }

        if (medicationRequestSearchQuery.getIntent() != null && !medicationRequestSearchQuery.getIntent().isEmpty()) {
            Set<String> requestedCodes = medicationRequestSearchQuery.getIntent()
                    .stream()
                    .map(SearchValue::getValue).collect(Collectors.toSet());
            sql.append(" AND combined_prescriptions.intent IN (:intents) ");
            params.addValue("intents", requestedCodes);
        }

        if (medicationRequestSearchQuery.getStatus() != null) {
            String status = medicationRequestSearchQuery.getStatus();
            sql.append(" AND combined_prescriptions.status = :status ");
            params.addValue("status", status.toLowerCase());
        }

    }


    private StringBuilder facilityQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                SELECT FAC.id,
                       FAC.uuid,
                       FAC.name,
                       FAC.phone,
                       FAC.fax,
                       FAC.street,
                       FAC.city,
                       FAC.state,
                       FAC.postal_code,
                       FAC.country_code,
                       FAC.federal_ein,
                       FAC.website,
                       FAC.email,
                       FAC.service_location,
                       FAC.billing_location,
                       FAC.accepts_assignment,
                       FAC.pos_code,
                       FAC.x12_sender_id,
                       FAC.attn,
                       FAC.domain_identifier,
                       FAC.facility_npi,
                       FAC.facility_taxonomy,
                       FAC.tax_id_type,
                       FAC.color,
                       FAC.primary_business_entity,
                       FAC.facility_code,
                       FAC.extra_validation,
                       FAC.mail_street,
                       FAC.mail_street2,
                       FAC.mail_city,
                       FAC.mail_state,
                       FAC.mail_zip,
                       FAC.oid,
                       FAC.iban,
                       FAC.info,
                       FAC.inactive
                FROM facility FAC
                WHERE 1=1 
                """);

        return sql;
    }

    private RowMapper<PrescriptionDBRecord> prescriptionDBRecordRowMapper() {
        return (rs, rowNum) -> PrescriptionDBRecord.builder()
                .uuid(toUuid(rs.getBytes("uuid")))
                .sourceTable(rs.getString("source_table"))
                .drug(rs.getString("drug"))
                .active(rs.getString("active"))
                .intent(rs.getString("intent"))
                .category(rs.getString("category"))
                .intentTitle(rs.getString("intent_title"))
                .categoryTitle(rs.getString("category_title"))
                .categoryText(rs.getString("category_text"))
                .rxnormDrugcode(rs.getString("rxnorm_drugcode"))
                .dateAdded(getLocalDateTime(rs, "date_added"))
                .dateModified(getLocalDateTime(rs, "date_modified"))
                .unit(rs.getString("unit"))
                .interval(rs.getString("interval"))
                .route(rs.getString("route"))
                .note(rs.getString("note"))
                .status(rs.getString("status"))
                .dosage(rs.getString("dosage"))
                .drugDosageInstructions(rs.getString("drug_dosage_instructions"))
                .medicationAdherenceDateAsserted(getLocalDateTime(rs, "medication_adherence_date_asserted"))
                .prescriptionDrugSize(rs.getString("prescription_drug_size"))
                .quantity(rs.getString("quantity"))
                .diagnosis(rs.getString("diagnosis"))

                .puuid(toUuid(rs.getBytes("puuid")))
                .euuid(toUuid(rs.getBytes("euuid")))
                .pruuid(toUuid(rs.getBytes("pruuid")))
                .drugUuid(rs.getString("drug_uuid"))

                .routeId(rs.getString("route_id"))
                .routeTitle(rs.getString("route_title"))
                .routeCodes(rs.getString("route_codes"))

                .unitId(rs.getString("unit_id"))
                .unitTitle(rs.getString("unit_title"))
                .unitCodes(rs.getString("unit_codes"))

                .intervalId(rs.getString("interval_id"))
                .intervalTitle(rs.getString("interval_title"))
                .intervalCodes(rs.getString("interval_codes"))
                .intervalNotes(rs.getString("interval_notes"))

                .medicationAdherence(rs.getString("medication_adherence"))
                .medicationAdherenceTitle(rs.getString("medication_adherence_title"))
                .medicationAdherenceCodes(rs.getString("medication_adherence_codes"))
                .medicationAdherenceInformationSource(rs.getString("medication_adherence_information_source"))
                .medicationAdherenceInformationSourceTitle(rs.getString("medication_adherence_information_source_title"))
                .medicationAdherenceInformationSourceCodes(rs.getString("medication_adherence_information_source_codes"))

                .reportingSourceRecordId(rs.getString("reporting_source_record_id"))
                .reportingSourceUuid(rs.getString("reporting_source_uuid"))
                .reportingSourceType(rs.getString("reporting_source_type"))
                .reportingSourceAbookType(rs.getString("reporting_source_abook_type"))
                .isPrimaryRecord(rs.getString("is_primary_record"))
                .build();
    }

    public RowMapper<FacilityDBRecord> facilityDBRecordRowMapper() {
        return (rs, rowNum) -> FacilityDBRecord.builder()
                .id(rs.getLong("id"))
                .uuid(toUuid(rs.getBytes("uuid")))
                .name(rs.getString("name"))
                .phone(rs.getString("phone"))
                .fax(rs.getString("fax"))
                .street(rs.getString("street"))
                .city(rs.getString("city"))
                .state(rs.getString("state"))
                .postalCode(rs.getString("postal_code"))
                .countryCode(rs.getString("country_code"))
                .federalEin(rs.getString("federal_ein"))
                .website(rs.getString("website"))
                .email(rs.getString("email"))
                .serviceLocation(rs.getObject("service_location") != null ? rs.getInt("service_location") : null)
                .billingLocation(rs.getObject("billing_location") != null ? rs.getInt("billing_location") : null)
                .acceptsAssignment(rs.getObject("accepts_assignment") != null ? rs.getInt("accepts_assignment") : null)
                .posCode(rs.getString("pos_code"))
                .x12SenderId(rs.getString("x12_sender_id"))
                .attn(rs.getString("attn"))
                .domainIdentifier(rs.getString("domain_identifier"))
                .facilityNpi(rs.getString("facility_npi"))
                .facilityTaxonomy(rs.getString("facility_taxonomy"))
                .taxIdType(rs.getString("tax_id_type"))
                .color(rs.getString("color"))
                .primaryBusinessEntity(rs.getObject("primary_business_entity") != null ? rs.getInt("primary_business_entity") : null)
                .facilityCode(rs.getString("facility_code"))
                .extraValidation(rs.getString("extra_validation"))
                .mailStreet(rs.getString("mail_street"))
                .mailStreet2(rs.getString("mail_street2"))
                .mailCity(rs.getString("mail_city"))
                .mailState(rs.getString("mail_state"))
                .mailZip(rs.getString("mail_zip"))
                .oid(rs.getString("oid"))
                .iban(rs.getString("iban"))
                .info(rs.getString("info"))
                .inactive(rs.getObject("inactive") != null ? rs.getInt("inactive") : null)
                .build();
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }


}
