package org.example.basicfhirserver.repository.jdbc.formencounter;

import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.*;

@Repository
public class FormEncounterServiceImpl implements FormEncounterService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public FormEncounterServiceImpl(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<FormEncounterDBRecord> findById(UUID uuid) {
        StringBuilder sql = formEncounterQuery();

        sql.append(" AND fe.euuid = :uuid");
        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(sql.toString(),
                parameters,
                formEncounterRowMapper());
    }

    @Override
    public List<FormEncounterDBRecord> find(EncounterSearchQuery encounterSearchQuery) {
        StringBuilder sql = formEncounterQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        addFilter(sql, params, encounterSearchQuery);

        return namedParameterJdbcTemplate.query(sql.toString(),
                params,
                formEncounterRowMapper());
    }

    private StringBuilder formEncounterQuery() {
        return new StringBuilder("""
                SELECT fe.eid,
                    fe.euuid,
                    fe.encounter_date AS date, -- Changed to read the unescaped subquery alias
                    fe.reason,
                    fe.onset_date,
                    fe.sensitivity,
                    fe.billing_note,
                    fe.pc_catid,
                    fe.last_level_billed,
                    fe.last_level_closed,
                    fe.last_stmt_date,
                    fe.stmt_count,
                    fe.supervisor_id,
                    fe.invoice_refno,
                    fe.referral_source,
                    fe.billing_facility,
                    fe.external_id,
                    fe.last_update,
                    fe.pos_code,
                    fe.class_code,
                    class.notes as class_title,
                    opc.pc_catname,
                
                    patient.pid,
                    patient.puuid,
                    facilities.facility_id,
                    facilities.facility_uuid,
                    facilities.facility_name,
                    facilities.facility_location_uuid,
                
                    fa.billing_facility_id,
                    fa.billing_facility_uuid,
                    fa.billing_facility_name,
                    fa.billing_location_uuid,
                
                    fe.provider_id,
                    fe.referring_provider_id,
                    fe.ordering_provider_id,
                    providers.provider_uuid,
                    providers.provider_username,
                    referrers.referrer_uuid,
                    referrers.referrer_username,
                    fe.discharge_disposition,
                    discharge_list.discharge_disposition_text
                
                    FROM (
                        select
                            encounter as eid,
                            uuid as euuid,
                            date as encounter_date, -- Removed backticks and explicitly aliased here
                            reason,
                            onset_date,
                            sensitivity,
                            billing_note,
                            pc_catid,
                            last_level_billed,
                            last_level_closed,
                            last_stmt_date,
                            stmt_count,
                            provider_id,
                            supervisor_id,
                            invoice_refno,
                            referral_source,
                            billing_facility,
                            external_id,
                            pos_code,
                            class_code,
                            facility_id,
                            discharge_disposition,
                            pid as encounter_pid,
                            referring_provider_id,
                            ordering_provider_id,
                            last_update
                        FROM form_encounter
                    ) fe
                    LEFT JOIN openemr_postcalendar_categories as opc
                    ON opc.pc_catid = fe.pc_catid
                    LEFT JOIN list_options as class ON class.option_id = fe.class_code
                    LEFT JOIN (
                        select
                             facility.id AS billing_facility_id
                             ,facility.uuid AS billing_facility_uuid
                             ,facility.`name` AS billing_facility_name
                             ,locations.uuid AS billing_location_uuid
                        from facility
                        LEFT JOIN uuid_mapping AS locations
                            ON locations.target_uuid = facility.uuid AND locations.resource='Location'
                    ) fa ON fa.billing_facility_id = fe.billing_facility
                    LEFT JOIN (
                        select
                               pid
                              ,uuid AS puuid
                        FROM patient_data
                    ) patient ON fe.encounter_pid = patient.pid
                    LEFT JOIN (
                        select
                             id AS provider_provider_id
                             ,uuid AS provider_uuid
                             ,`username` AS provider_username
                         FROM users
                         WHERE
                             npi IS NOT NULL and npi != ''
                    ) providers ON fe.provider_id = providers.provider_provider_id
                    LEFT JOIN (
                        select
                             id AS referring_provider_id
                             ,uuid AS referrer_uuid
                             ,`username` AS referrer_username
                         FROM users
                         WHERE
                             npi IS NOT NULL and npi != ''
                    ) referrers ON fe.referring_provider_id = referrers.referring_provider_id
                    LEFT JOIN (
                        select
                             facility.id AS facility_id
                             ,facility.uuid AS facility_uuid
                             ,facility.`name` AS facility_name
                             ,`locations`.`uuid` AS facility_location_uuid
                        from facility
                        LEFT JOIN uuid_mapping AS locations
                            ON locations.target_uuid = facility.uuid AND locations.resource='Location'
                    ) facilities ON facilities.facility_id = fe.facility_id
                    LEFT JOIN (
                        select option_id AS discharge_option_id
                        ,title AS discharge_disposition_text
                        FROM list_options
                        WHERE list_id = 'discharge-disposition'
                    ) discharge_list ON fe.discharge_disposition = discharge_list.discharge_option_id WHERE 1=1 
                """);
    }

    private void addFilter(
            StringBuilder sql,
            MapSqlParameterSource params,
            EncounterSearchQuery encounterSearchQuery
    ) {
        if (encounterSearchQuery.getEncounterId() != null) {
            String uuid = encounterSearchQuery.getEncounterId();
            byte[] binaryUuid = toBytes(UUID.fromString(uuid));
            sql.append("""
                    AND (fe.euuid = :encounterUuid)
                    """);
            params.addValue("encounterUuid", binaryUuid);
        }

        if (encounterSearchQuery.getPatientId() != null) {
            List<byte[]> binaryUuids = encounterSearchQuery.getPatientId().stream()
                    .map(idStr -> toBytes(UUID.fromString(idStr)))
                    .toList();
            sql.append(" AND (patient.puuid IN (:patientUuid) ) ");
            params.addValue("patientUuid", binaryUuids);
        }
        addDateFilter(sql, params, encounterSearchQuery);
    }

    private void addDateFilter(
            StringBuilder sql,
            MapSqlParameterSource params,
            EncounterSearchQuery encounterSearchQuery
    ) {
        if (encounterSearchQuery.getDate() == null ||
                encounterSearchQuery.getDate().getValue() == null) {
            return;
        }

        LocalDateTime date =
                encounterSearchQuery.getDate().getValue();

        if (encounterSearchQuery.getDate().getPrefix() == null) {
            sql.append(" AND fe.encounter_date = :date");
            params.addValue("date", date);
            return;
        }

        switch (encounterSearchQuery.getDate().getPrefix()) {
            case GREATERTHAN:
                sql.append(" AND fe.encounter_date > :encounterDate");
                break;
            case GREATERTHAN_OR_EQUALS:
                sql.append(" AND fe.encounter_date >= :encounterDate");
                break;
            case LESSTHAN:
            case ENDS_BEFORE: // Handled logically
                sql.append(" AND fe.encounter_date < :encounterDate");
                break;
            case LESSTHAN_OR_EQUALS:
                sql.append(" AND fe.encounter_date <= :encounterDate");
                break;
            case NOT_EQUAL:
                sql.append(" AND fe.encounter_date <> :encounterDate");
                break;
            case STARTS_AFTER: // Handled logically
                sql.append(" AND fe.encounter_date > :encounterDate");
                break;
            case EQUAL:
            case APPROXIMATE:
            default:
                sql.append(" AND fe.encounter_date = :encounterDate");
                break;
        }
        params.addValue("encounterDate", date);
    }

    private RowMapper<FormEncounterDBRecord> formEncounterRowMapper() {
        return (rs, rowNum) -> FormEncounterDBRecord.builder()
                .eid(rs.getObject("eid", Long.class))
                .euuid(toUuid(rs.getBytes("euuid")))
                .date(toLocalDateTime(rs.getTimestamp("date")))
                .reason(rs.getString("reason"))
                .onsetDate(toLocalDate(rs.getDate("onset_date")))
                .sensitivity(rs.getString("sensitivity"))
                .billingNote(rs.getString("billing_note"))
                .pcCatid(rs.getObject("pc_catid", Long.class))
                .lastLevelBilled(rs.getObject("last_level_billed", Integer.class))
                .lastLevelClosed(rs.getObject("last_level_closed", Integer.class))
                .lastStmtDate(toLocalDate(rs.getDate("last_stmt_date")))
                .stmtCount(rs.getObject("stmt_count", Integer.class))
                .supervisorId(rs.getObject("supervisor_id", Long.class))
                .invoiceRefno(rs.getString("invoice_refno"))
                .referralSource(rs.getString("referral_source"))
                .billingFacility(rs.getObject("billing_facility", Long.class))
                .externalId(rs.getString("external_id"))
                .lastUpdate(toLocalDateTime(rs.getTimestamp("last_update")))
                .posCode(rs.getString("pos_code"))
                .classCode(rs.getString("class_code"))

                .classTitle(rs.getString("class_title"))
                .pcCatname(rs.getString("pc_catname"))

                .pid(rs.getObject("pid", Long.class))
                .puuid(toUuid(rs.getBytes("puuid")))

                .facilityId(rs.getObject("facility_id", Long.class))
                .facilityUuid(toUuid(rs.getBytes("facility_uuid")))
                .facilityName(rs.getString("facility_name"))
                .facilityLocationUuid(toUuid(rs.getBytes("facility_location_uuid")))

                .billingFacilityId(rs.getObject("billing_facility_id", Long.class))
                .billingFacilityUuid(toUuid(rs.getBytes("billing_facility_uuid")))
                .billingFacilityName(rs.getString("billing_facility_name"))
                .billingLocationUuid(toUuid(rs.getBytes("billing_location_uuid")))

                .providerId(rs.getObject("provider_id", Long.class))
                .referringProviderId(rs.getObject("referring_provider_id", Long.class))
                .orderingProviderId(rs.getObject("ordering_provider_id", Long.class))

                .providerUuid(toUuid(rs.getBytes("provider_uuid")))
                .providerUsername(rs.getString("provider_username"))
                .referrerUuid(toUuid(rs.getBytes("referrer_uuid")))
                .referrerUsername(rs.getString("referrer_username"))

                .dischargeDisposition(rs.getString("discharge_disposition"))
                .dischargeDispositionText(rs.getString("discharge_disposition_text"))
                .build();
    }

}
