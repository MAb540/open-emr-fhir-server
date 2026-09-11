package org.example.basicfhirserver.repository.jdbc.diagnosticreport;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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

        return namedParameterJdbcTemplate.query(sql.toString(), params, proceduresListDBRecordRowMapper());
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


    private RowMapper<ProcedureDBRecord> proceduresListDBRecordRowMapper() {
        return (rs, rowNum) -> ProcedureDBRecord.builder()
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

}
