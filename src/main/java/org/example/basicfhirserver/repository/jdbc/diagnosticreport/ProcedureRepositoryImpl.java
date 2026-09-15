package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toLocalDateTime;
import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toUuid;

@Repository
public class ProcedureRepositoryImpl implements ProcedureRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ProcedureRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<ProcedureDBRecord> findProcedureById(UUID uuid) {
        return List.of();
    }

    @Override
    public List<ProcedureDBRecord> findProcedures() {
        StringBuilder sql = procedureOrderListItemQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        List<RawProcedureRecord> rawProcedureRecords =
                namedParameterJdbcTemplate.query(sql.toString(), params, proceduresListDBRecordRowMapper());


        return hydrateSearchResults(rawProcedureRecords);
    }


    private StringBuilder procedureOrderListItemQuery() {
        return new StringBuilder("""
                SELECT
                    porder.order_uuid
                    ,porder.order_uuid AS uuid
                    ,porder.procedure_order_id
                    ,porder.order_provider_id
                    ,porder.order_activity
                    ,porder.order_activity AS activity
                    ,porder.order_diagnosis
                    ,porder.order_encounter_id
                    ,porder.order_lab_id
                    ,porder.order_patient_id
                    ,porder.provider_id
                    ,porder.date_ordered
                    ,porder.date_collected
                    ,porder.order_status
                    ,porder.order_priority
                    ,porder.patient_instructions
                    ,porder.clinical_hx
                    ,porder.procedure_order_type
                    ,porder.scheduled_date
                    ,porder.scheduled_start
                    ,porder.scheduled_end
                    ,porder.performer_type
                    ,porder.order_intent
                    ,porder.location_id
                    ,porder.specimen_fasting
                
                    ,preport.report_date
                    ,preport.procedure_report_id
                    ,preport.report_uuid
                    ,preport.report_notes
                    ,preport.procedure_order_seq
                
                    ,presult.procedure_result_id
                    ,presult.result_uuid
                    ,presult.result_code
                    ,presult.result_text
                    ,presult.result_units
                    ,presult.result_result
                    ,presult.result_range
                    ,presult.result_abnormal
                    ,presult.result_abnormal_title
                    ,presult.result_abnormal_codes
                    ,presult.result_comments
                    ,presult.result_status
                
                    ,order_codes.procedure_name
                    ,order_codes.procedure_code
                    ,order_codes.procedure_type
                    ,order_codes.procedure_order_seq AS order_code_seq
                    ,order_codes.diagnoses
                
                    ,pcode_types.standard_code
                
                    ,labs.lab_id
                    ,labs.lab_uuid
                    ,labs.lab_npi
                    ,labs.lab_name
                    ,labs.lab_director_uuid
                    ,labs.lab_director_npi
                
                    ,patients.puuid
                    ,patients.pid
                    ,patients.pid AS patient_id
                
                    ,encounters.eid
                    ,encounters.euuid
                    ,encounters.encounter_date
                
                    ,docs.doc_id
                    ,docs.doc_uuid
                
                    ,provider.provider_uuid
                    ,provider.provider_id
                    ,provider.provider_fname
                    ,provider.provider_mname
                    ,provider.provider_lname
                    ,provider.provider_npi
                
                    ,location.location_id
                    ,location.location_uuid
                    ,location.location_name
                FROM (
                    SELECT
                        procedure_order_id
                        ,uuid AS order_uuid
                        ,provider_id AS order_provider_id
                        ,encounter_id AS order_encounter_id
                        ,activity AS order_activity
                        ,order_diagnosis
                        ,order_status
                        ,order_priority
                        ,patient_instructions
                        ,clinical_hx
                        ,lab_id as order_lab_id
                        ,patient_id AS order_patient_id
                        ,provider_id
                        ,date_ordered
                        ,date_collected
                        ,procedure_order_type
                        ,scheduled_date
                        ,scheduled_start
                        ,scheduled_end
                        ,performer_type
                        ,order_intent
                        ,location_id
                        ,specimen_fasting
                    FROM procedure_order
                    WHERE activity = 1
                ) porder
                LEFT JOIN (
                    SELECT
                        procedure_order_id
                        ,procedure_order_seq
                        ,procedure_code
                        ,procedure_name
                        ,procedure_order_title AS procedure_type
                        ,diagnoses
                    FROM procedure_order_code
                ) order_codes ON order_codes.procedure_order_id = porder.procedure_order_id
                LEFT JOIN (
                    SELECT
                        date_report AS report_date
                        ,procedure_report_id
                        ,procedure_order_id
                        ,procedure_order_seq
                        ,uuid AS report_uuid
                        ,report_notes
                    FROM procedure_report
                ) preport ON preport.procedure_order_id = porder.procedure_order_id
                    AND preport.procedure_order_seq = order_codes.procedure_order_seq
                LEFT JOIN (
                    SELECT
                        procedure_result_id
                        ,procedure_report_id
                        ,uuid AS result_uuid
                        ,result AS result_quantity
                        ,result AS result_string
                        ,result AS result_result
                        ,units AS result_units
                        ,result_status
                        ,result_code
                        ,result_text
                        ,result_data_type
                        ,`range` AS result_range
                        ,`abnormal` AS result_abnormal
                        ,`comments` AS result_comments
                        ,`document_id` AS result_document_id
                        ,`lo_abnormal`.`title` AS result_abnormal_title
                        ,`lo_abnormal`.`codes` AS result_abnormal_codes
                    FROM `procedure_result`
                    LEFT JOIN list_options lo_abnormal ON lo_abnormal.option_id = `procedure_result`.`abnormal` AND lo_abnormal.list_id = 'proc_res_abnormal'
                ) presult ON presult.procedure_report_id = preport.procedure_report_id
                LEFT JOIN (
                    SELECT
                        standard_code,
                        procedure_code AS proc_code
                    FROM procedure_type
                ) pcode_types ON order_codes.procedure_code = pcode_types.proc_code
                LEFT JOIN (
                    SELECT
                        encounter AS eid
                        ,uuid AS euuid
                        ,`date` AS encounter_date
                    FROM form_encounter
                ) encounters ON porder.order_encounter_id = encounters.eid
                LEFT JOIN (
                    SELECT
                        ppid AS lab_id
                        ,procedure_providers.uuid AS lab_uuid
                        ,procedure_providers.npi AS lab_npi
                        ,procedure_providers.`name` AS lab_name
                        ,procedure_providers.`active` AS lab_active
                        ,users.uuid AS lab_director_uuid
                        ,users.npi AS lab_director_npi
                    FROM procedure_providers
                    LEFT JOIN users ON users.id = procedure_providers.lab_director
                    WHERE users.npi IS NOT NULL AND users.npi != ''
                ) labs ON labs.lab_id = porder.order_lab_id
                LEFT JOIN (
                    SELECT
                        pid
                        ,uuid AS puuid
                    FROM patient_data
                ) patients ON patients.pid = porder.order_patient_id
                LEFT JOIN (
                    SELECT
                       id AS doc_id
                       ,uuid AS doc_uuid
                    FROM documents
                ) docs ON presult.result_document_id = docs.doc_id
                LEFT JOIN (
                    SELECT
                        users.uuid AS provider_uuid
                        ,users.id AS provider_id
                        ,users.fname AS provider_fname
                        ,users.mname AS provider_mname
                        ,users.lname AS provider_lname
                        ,users.npi AS provider_npi
                    FROM users
                    WHERE npi IS NOT NULL AND npi != ''
                ) provider ON provider.provider_id = porder.provider_id
                LEFT JOIN (
                    SELECT
                        id AS location_id
                        ,uuid AS location_uuid
                        ,name AS location_name
                    FROM facility
                ) location ON location.location_id = porder.location_id WHERE 1=1
                """);

    }


    private RowMapper<RawProcedureRecord> proceduresListDBRecordRowMapper() {
        return (rs, rowNum) -> RawProcedureRecord.builder()
                .orderUuid(toUuid(rs.getBytes("order_uuid")))
                .uuid(toUuid(rs.getBytes("uuid")))
                .procedureOrderId(rs.getObject("procedure_order_id") != null ? rs.getLong("procedure_order_id") : null)
                .orderProviderId(rs.getObject("order_provider_id") != null ? rs.getLong("order_provider_id") : null)
                .orderActivity(rs.getObject("order_activity") != null ? rs.getInt("order_activity") : null)
                .activity(rs.getObject("activity") != null ? rs.getInt("activity") : null)
                .orderDiagnosis(rs.getString("order_diagnosis"))
                .orderEncounterId(rs.getObject("order_encounter_id") != null ? rs.getLong("order_encounter_id") : null)
                .orderLabId(rs.getObject("order_lab_id") != null ? rs.getLong("order_lab_id") : null)
                .orderPatientId(rs.getObject("order_patient_id") != null ? rs.getLong("order_patient_id") : null)
                .providerId(rs.getObject("provider_id") != null ? rs.getLong("provider_id") : null)
                .dateOrdered(toLocalDateTime(rs.getTimestamp("date_ordered")))
                .dateCollected(toLocalDateTime(rs.getTimestamp("date_collected")))
                .orderStatus(rs.getString("order_status"))
                .orderPriority(rs.getString("order_priority"))
                .patientInstructions(rs.getString("patient_instructions"))
                .clinicalHx(rs.getString("clinical_hx"))
                .procedureOrderType(rs.getString("procedure_order_type"))
                .scheduledDate(toLocalDateTime(rs.getTimestamp("scheduled_date")))
                .scheduledStart(toLocalDateTime(rs.getTimestamp("scheduled_start")))
                .scheduledEnd(toLocalDateTime(rs.getTimestamp("scheduled_end")))
                .performerType(rs.getString("performer_type"))
                .orderIntent(rs.getString("order_intent"))
                .locationId(rs.getObject("location_id") != null ? rs.getLong("location_id") : null)
                .specimenFasting(rs.getObject("specimen_fasting") != null ? rs.getInt("specimen_fasting") : null)
                .reportDate(toLocalDateTime(rs.getTimestamp("report_date")))
                .procedureReportId(rs.getObject("procedure_report_id") != null ? rs.getLong("procedure_report_id") : null)
                .reportUuid(toUuid(rs.getBytes("report_uuid")))
                .reportNotes(rs.getString("report_notes"))
                .procedureOrderSeq(rs.getObject("procedure_order_seq") != null ? rs.getInt("procedure_order_seq") : null)
                .procedureResultId(rs.getObject("procedure_result_id") != null ? rs.getLong("procedure_result_id") : null)
                .resultUuid(toUuid(rs.getBytes("result_uuid")))
                .resultCode(rs.getString("result_code"))
                .resultText(rs.getString("result_text"))
                .resultUnits(rs.getString("result_units"))
                .resultResult(rs.getString("result_result"))
                .resultRange(rs.getString("result_range"))
                .resultAbnormal(rs.getString("result_abnormal"))
                .resultAbnormalTitle(rs.getString("result_abnormal_title"))
                .resultAbnormalCodes(rs.getString("result_abnormal_codes"))
                .resultComments(rs.getString("result_comments"))
                .resultStatus(rs.getString("result_status"))
                .procedureName(rs.getString("procedure_name"))
                .procedureCode(rs.getString("procedure_code"))
                .procedureType(rs.getString("procedure_type"))
                .orderCodeSeq(rs.getObject("order_code_seq") != null ? rs.getInt("order_code_seq") : null)
                .diagnoses(rs.getString("diagnoses"))
                .standardCode(rs.getString("standard_code"))
                .labId(rs.getObject("lab_id") != null ? rs.getLong("lab_id") : null)
                .labUuid(toUuid(rs.getBytes("lab_uuid")))
                .labNpi(rs.getString("lab_npi"))
                .labName(rs.getString("lab_name"))
                .labDirectorUuid(toUuid(rs.getBytes("lab_director_uuid")))
                .labDirectorNpi(rs.getString("lab_director_npi"))
                .puuid(toUuid(rs.getBytes("puuid")))
                .pid(rs.getObject("pid") != null ? rs.getLong("pid") : null)
                .patientId(rs.getObject("patient_id") != null ? rs.getLong("patient_id") : null)
                .eid(rs.getObject("eid") != null ? rs.getLong("eid") : null)
                .euuid(toUuid(rs.getBytes("euuid")))
                .encounterDate(toLocalDateTime(rs.getTimestamp("encounter_date")))
                .docId(rs.getObject("doc_id") != null ? rs.getLong("doc_id") : null)
                .docUuid(toUuid(rs.getBytes("doc_uuid")))
                .providerUuid(toUuid(rs.getBytes("provider_uuid")))
                .providerId(rs.getObject("provider_id") != null ? rs.getLong("provider_id") : null)
                .providerFname(rs.getString("provider_fname"))
                .providerMname(rs.getString("provider_mname"))
                .providerLname(rs.getString("provider_lname"))
                .providerNpi(rs.getString("provider_npi"))
                .locationUuid(toUuid(rs.getBytes("location_uuid")))
                .locationName(rs.getString("location_name"))
                .build();
    }

    public List<ProcedureDBRecord> hydrateSearchResults(List<RawProcedureRecord> rawRows) {
        if (rawRows == null || rawRows.isEmpty()) {
            return Collections.emptyList();
        }

        // Tracks the insertion order of unique orders (replicates PHP array sorting)
        List<String> procedureOrderUuuids = new ArrayList<>();

        // Maps to track and reduce duplicate parent structures and report nodes
        Map<String, ProcedureDBRecord> procedureByUuid = new HashMap<>();
        Map<String, ProcedureDBRecord.ReportBlock> reportsByUuid = new HashMap<>();

        // =========================================================================
        // PASS 1: Build the tree structure and collect relational nodes
        // =========================================================================
        for (RawProcedureRecord row : rawRows) {
            // Safe assignment preventing NullPointerException if orderUuid is null
            String procedureUuid = row.getOrderUuid() != null ? row.getOrderUuid().toString() : null;
            if (procedureUuid == null) {
                continue;
            }

            // Create the parent root record container if we haven't seen this order yet
            if (!procedureByUuid.containsKey(procedureUuid)) {
                procedureOrderUuuids.add(procedureUuid);
                ProcedureDBRecord parentRecord = ProcedureDBRecord.builder()
                        .orderUuid(row.getOrderUuid())
                        .uuid(row.getUuid())
                        .procedureOrderId(row.getProcedureOrderId())
                        .orderProviderId(row.getOrderProviderId())
                        .orderActivity(row.getOrderActivity())
                        .activity(row.getActivity())
                        .orderDiagnosis(row.getOrderDiagnosis())
                        .orderEncounterId(row.getOrderEncounterId())
                        .orderLabId(row.getOrderLabId())
                        .orderPatientId(row.getOrderPatientId())
                        .providerId(row.getProviderId())
                        .dateOrdered(row.getDateOrdered())
                        .dateCollected(row.getDateCollected())
                        .orderStatus(row.getOrderStatus())
                        .orderPriority(row.getOrderPriority())
                        .patientInstructions(row.getPatientInstructions())
                        .clinicalHx(row.getClinicalHx())
                        .procedureOrderType(row.getProcedureOrderType())
                        .scheduledDate(row.getScheduledDate())
                        .scheduledStart(row.getScheduledStart())
                        .scheduledEnd(row.getScheduledEnd())
                        .performerType(row.getPerformerType())
                        .orderIntent(row.getOrderIntent())
                        .locationId(row.getLocationId())
                        .specimenFasting(row.getSpecimenFasting())
                        .procedureName(row.getProcedureName())
                        .procedureCode(row.getProcedureCode())
                        .diagnoses(row.getDiagnoses())
                        .standardCode(row.getStandardCode())
                        // Map nested attributed reference block objects
                        .provider(row.getProviderId() != null ? ProcedureDBRecord.ProviderInfo.builder()
                                .id(row.getProviderId()).uuid(row.getProviderUuid())
                                .fname(row.getProviderFname()).mname(row.getProviderMname()).lname(row.getProviderLname())
                                .npi(row.getProviderNpi()).build() : null)
                        .lab(row.getLabId() != null ? ProcedureDBRecord.LabMetadataInfo.builder()
                                .id(row.getLabId()).uuid(row.getLabUuid()).name(row.getLabName()).npi(row.getLabNpi())
                                .directorUuid(row.getLabDirectorUuid()).directorNpi(row.getLabDirectorNpi()).build() : null)
                        .patient(row.getPid() != null ? ProcedureDBRecord.PatientReferenceInfo.builder()
                                .pid(row.getPid()).uuid(row.getPuuid()).build() : null)
                        .encounter(row.getEid() != null ? ProcedureDBRecord.EncounterReferenceInfo.builder()
                                .id(row.getEid()).uuid(row.getEuuid()).date(row.getEncounterDate()).build() : null)
                        .location(row.getLocationId() != null && row.getLocationUuid() != null ? ProcedureDBRecord.FacilityInfo.builder()
                                .id(row.getLocationId()).uuid(row.getLocationUuid()).name(row.getLocationName()).build() : null)
                        .reports(new ArrayList<>())
                        .build();

                procedureByUuid.put(procedureUuid, parentRecord);
            }

            // Fetch the parent pointer to append nested elements
            ProcedureDBRecord currentProcedure = procedureByUuid.get(procedureUuid);

            // Safe assignment preventing NullPointerException if reportUuid is null
            String reportUuid = row.getReportUuid() != null ? row.getReportUuid().toString() : null;

            if (reportUuid != null && !reportUuid.isEmpty()) {
                // If we haven't seen this report yet, create it
                if (!reportsByUuid.containsKey(reportUuid)) {
                    ProcedureDBRecord.ReportBlock newReport = ProcedureDBRecord.ReportBlock.builder()
                            .id(row.getProcedureReportId())
                            .uuid(row.getReportUuid())
                            .date(row.getReportDate())
                            .notes(row.getReportNotes())
                            .orderSeq(row.getProcedureOrderSeq())
                            .results(new ArrayList<>())
                            .specimens(new ArrayList<>())
                            .build();

                    reportsByUuid.put(reportUuid, newReport);

                    // Link this report block straight to the parent procedure's reports list
                    currentProcedure.getReports().add(newReport);
                }

                ProcedureDBRecord.ReportBlock currentReport = reportsByUuid.get(reportUuid);

                // Add individual test result to the report if it exists on this row
                if (row.getProcedureResultId() != null) {
                    ProcedureDBRecord.ResultBlock result = ProcedureDBRecord.ResultBlock.builder()
                            .id(row.getProcedureResultId())
                            .uuid(row.getResultUuid())
                            .code(row.getResultCode())
                            .text(row.getResultText())
                            .units(row.getResultUnits())
                            .result(row.getResultResult())
                            .range(row.getResultRange())
                            .abnormal(row.getResultAbnormal())
                            .comments(row.getResultComments())
                            .build();

                    currentReport.getResults().add(result);
                }
            }
        } // End of PASS 1 Loop

        // =========================================================================
        // PASS 2: Fetch dependent specimens for accumulated report nodes
        // =========================================================================
        String orderIdSql = "SELECT procedure_order_id FROM procedure_report WHERE uuid = :reportUuid";

        String specimenSql = """
                SELECT uuid AS specimen_uuid, specimen_identifier, accession_identifier,
                       specimen_type_code, specimen_type, collection_method_code, collection_method,
                       specimen_location_code, specimen_location, collected_date, collection_date_low,
                       collection_date_high, volume_value, volume_unit, condition_code, specimen_condition,
                       comments AS specimen_comments, deleted
                FROM procedure_specimen
                WHERE procedure_order_id = :orderId AND procedure_order_seq = :orderSeq
                ORDER BY procedure_specimen_id
                """;

        for (Map.Entry<String, ProcedureDBRecord.ReportBlock> entry : reportsByUuid.entrySet()) {
            String reportUuid = entry.getKey();
            ProcedureDBRecord.ReportBlock report = entry.getValue();

            // Only look up specimens if an order sequence is available
            if (report.getOrderSeq() != null) {
                MapSqlParameterSource orderParams = new MapSqlParameterSource("reportUuid", reportUuid);

                // Find the internal procedure_order_id key
                List<Long> orderIdList = namedParameterJdbcTemplate.query(orderIdSql, orderParams,
                        (rs, rowNum) -> rs.getLong("procedure_order_id"));

                if (!orderIdList.isEmpty() && orderIdList.get(0) != null) {
                    Long orderId = orderIdList.get(0);

                    // Set up parameters for the specimen lookup
                    MapSqlParameterSource specimenParams = new MapSqlParameterSource()
                            .addValue("orderId", orderId)
                            .addValue("orderSeq", report.getOrderSeq());

                    // Query and map individual specimen records
                    List<ProcedureDBRecord.SpecimenBlock> specimens = namedParameterJdbcTemplate.query(specimenSql, specimenParams, (rs, rowNum) ->
                            ProcedureDBRecord.SpecimenBlock.builder()
                                    .uuid(toUuid(rs.getBytes("specimen_uuid")))
                                    .identifier(rs.getString("specimen_identifier"))
                                    .accession(rs.getString("accession_identifier"))
                                    .typeCode(rs.getString("specimen_type_code"))
                                    .type(rs.getString("specimen_type"))
                                    .methodCode(rs.getString("collection_method_code"))
                                    .method(rs.getString("collection_method"))
                                    .locationCode(rs.getString("specimen_location_code"))
                                    .location(rs.getString("specimen_location"))
                                    .collectedDate(toLocalDateTime(rs.getTimestamp("collected_date")))
                                    .collectionStart(toLocalDateTime(rs.getTimestamp("collection_date_low")))
                                    .collectionEnd(toLocalDateTime(rs.getTimestamp("collection_date_high")))
                                    .volume(rs.getObject("volume_value") != null ? rs.getDouble("volume_value") : null)
                                    .volumeUnit(rs.getString("volume_unit"))
                                    .conditionCode(rs.getString("condition_code"))
                                    .specimenCondition(rs.getString("specimen_condition"))
                                    .comments(rs.getString("specimen_comments"))
                                    .deleted(rs.getObject("deleted") != null ? rs.getInt("deleted") : null)
                                    .build()
                    );

                    // Add all recovered specimen blocks into this report block's mutable collection
                    if (!specimens.isEmpty()) {
                        report.getSpecimens().addAll(specimens);
                    }
                }
            }
        } // End of PASS 2 Loop

        // Assemble the ordered list of reduced records to match insertion keys
        List<ProcedureDBRecord> finalRecords = new ArrayList<>();
        for (String uuid : procedureOrderUuuids) {
            finalRecords.add(procedureByUuid.get(uuid));
        }

        return finalRecords;
    }


}