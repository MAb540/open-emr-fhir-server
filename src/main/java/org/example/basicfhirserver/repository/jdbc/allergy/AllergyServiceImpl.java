package org.example.basicfhirserver.repository.jdbc.allergy;

import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.*;

@Repository
public class AllergyServiceImpl implements AllergyService {


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AllergyServiceImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<AllergyDBRecord> findById(UUID uuid) {

        StringBuilder sql = allergyQuery();

        sql.append(" AND allergy_ids.allergy_uuid= :uuid");
        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, allergyDBRecordRowMapper());
    }

    @Override
    public List<AllergyDBRecord> find(AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery) {

        StringBuilder sql = allergyQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();
        addFilter(sql, params, allergyIntoleranceSearchQuery);

        int limit = (allergyIntoleranceSearchQuery.getCount() != null) ? allergyIntoleranceSearchQuery.getCount() : 5;
        int offset = (allergyIntoleranceSearchQuery.getOffset() != null) ? allergyIntoleranceSearchQuery.getOffset() : 0;

        sql.append("""
                ORDER BY lists.date DESC limit :limit offset :offset
                """);

        params.addValue("limit", limit);
        params.addValue("offset", offset);

        return namedParameterJdbcTemplate.query(sql.toString(), params, allergyDBRecordRowMapper());

    }


    private StringBuilder allergyQuery() {
        return new StringBuilder("""
                    SELECT lists.*,
                            lists.pid AS patient_id,
                            lists.title,
                            lists.comments,
                            practitioners.uuid as practitioner,
                            practitioners.practitioner_npi,
                            practitioners.practitioner_uuid,
                            organizations.uuid as organization,
                            organizations.organization_uuid,
                            patient.puuid,
                            patient.patient_uuid,
                            allergy_ids.allergy_uuid,
                            reaction.title as reaction_title,
                            reaction.codes AS reaction_codes,
                            verification.title as verification_title
                        FROM (
                                SELECT lists.*, lists.pid AS patient_id FROM lists
                            ) lists
                            INNER JOIN (
                                SELECT lists.uuid AS allergy_uuid FROM lists
                            ) allergy_ids ON lists.uuid = allergy_ids.allergy_uuid
                            LEFT JOIN list_options as reaction ON (reaction.option_id = lists.reaction and reaction.list_id = 'reaction')
                            LEFT JOIN list_options as verification ON verification.option_id = lists.verification
                                and verification.list_id = 'allergyintolerance-verification'
                            RIGHT JOIN (
                                SELECT
                                    patient_data.uuid AS puuid
                                    ,patient_data.pid
                                    ,patient_data.uuid AS patient_uuid
                                FROM patient_data
                            ) patient ON patient.pid = lists.pid
                            LEFT JOIN (
                                select
                                users.uuid
                                ,users.uuid AS practitioner_uuid
                                ,users.npi AS practitioner_npi
                                ,users.username
                                ,users.facility AS organization
                                FROM users
                            ) practitioners ON practitioners.username = lists.user
                            LEFT JOIN (
                                select
                                facility.uuid
                                ,facility.uuid AS organization_uuid
                                ,facility.name
                                FROM facility
                            ) organizations ON organizations.name = practitioners.organization WHERE 1=1
                """);
    }

    private void addFilter(
            StringBuilder sql,
            MapSqlParameterSource params,
            AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery
    ) {

        if (allergyIntoleranceSearchQuery.getId() != null) {
            String uuid = allergyIntoleranceSearchQuery.getId();
            byte[] binaryUuid = toBytes(UUID.fromString(uuid));
            sql.append(" AND allergy_ids.allergy_uuid = :allergyUuid ");
            params.addValue("allergyUuid", binaryUuid);
        }

        if (allergyIntoleranceSearchQuery.getPatientId() != null) {
            String uuid = allergyIntoleranceSearchQuery.getPatientId();
            byte[] binaryUuid = toBytes(UUID.fromString(uuid));
            sql.append(" AND patient.puuid = :patientUuid ");
            params.addValue("patientUuid", binaryUuid);
        }

    }

    private RowMapper<AllergyDBRecord> allergyDBRecordRowMapper() {
        return (rs, rowNum) -> AllergyDBRecord.builder()
                .id(rs.getLong("id"))
                .date(toLocalDateTime(rs.getTimestamp("date")))
                .type(rs.getString("type"))
                .title(rs.getString("title"))
                .begdate(toLocalDateTime(rs.getTimestamp("begdate")))
                .enddate(toLocalDateTime(rs.getTimestamp("enddate")))
                .returndate(toLocalDate(rs.getDate("returndate")))
                .occurrence(rs.getObject("occurrence") != null ? rs.getInt("occurrence") : null)
                .classification(rs.getObject("classification") != null ? rs.getInt("classification") : null)
                .referredby(rs.getString("referredby"))
                .extrainfo(rs.getString("extrainfo"))
                .diagnosis(rs.getString("diagnosis"))
                .activity(rs.getObject("activity") != null ? rs.getInt("activity") : null)
                .comments(rs.getString("comments"))
                .pid(rs.getObject("pid") != null ? rs.getLong("pid") : null)
                .user(rs.getString("user"))
                .groupname(rs.getString("groupname"))
                .outcome(rs.getInt("outcome"))
                .destination(rs.getString("destination"))
                .reinjuryId(rs.getLong("reinjury_id"))
                .injuryPart(rs.getString("injury_part"))
                .injuryType(rs.getString("injury_type"))
                .injuryGrade(rs.getString("injury_grade"))
                .reaction(rs.getString("reaction"))
                .externalAllergyid(rs.getObject("external_allergyid") != null ? rs.getInt("external_allergyid") : null)
                .erxSource(rs.getString("erx_source"))
                .erxUploaded(rs.getString("erx_uploaded"))
                .modifydate(toLocalDateTime(rs.getTimestamp("modifydate")))
                .severityAl(rs.getString("severity_al"))
                .externalId(rs.getString("external_id"))
                .subtype(rs.getString("subtype"))
                .listOptionId(rs.getString("list_option_id"))
                .uuid(toUuid(rs.getBytes("uuid")))
                .verification(rs.getString("verification"))
                .udi(rs.getString("udi"))
                .udiData(rs.getString("udi_data"))

                .patientId(rs.getObject("patient_id") != null ? rs.getLong("patient_id") : null)
                .practitioner(rs.getString("practitioner"))
                .practitionerNpi(rs.getString("practitioner_npi"))
                .practitionerUuid(toUuid(rs.getBytes("practitioner_uuid")))
                .organization(rs.getString("organization"))
                .organizationUuid(toUuid(rs.getBytes("organization_uuid")))
                .puuid(toUuid(rs.getBytes("puuid")))
                .patientUuid(toUuid(rs.getBytes("patient_uuid")))
                .allergyUuid(toUuid(rs.getBytes("allergy_uuid")))
                .reactionTitle(rs.getString("reaction_title"))
                .reactionCodes(rs.getString("reaction_codes"))
                .verificationTitle(rs.getString("verification_title"))
                .build();

    }

}
