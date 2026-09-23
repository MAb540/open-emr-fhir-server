package org.example.basicfhirserver.repository.jdbc.condition;

import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.*;

@Repository
public class ConditionRepositoryImpl implements ConditionRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ConditionRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<ConditionProblemListItemDBRecord> findConditionProblemListItemById(UUID uuid) {

        StringBuilder sql = conditionProblemListItemQuery();

        sql.append(" AND l.uuid= :uuid");
        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, conditionProblemListDBRecordRowMapper());
    }

    @Override
    public Page<ConditionProblemListItemDBRecord> findConditionProblemListItem(ConditionSearchQuery conditionSearchQuery) {
        StringBuilder sql = conditionProblemListItemQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();
        addFilterInConditionProblemListItem(sql, params, conditionSearchQuery);

        long total = countTotal(sql, params);

        Integer limit = conditionSearchQuery.getCount();
        int offset = conditionSearchQuery.getOffset() != null ? conditionSearchQuery.getOffset() : 0;

        if (limit == null) {
            return new PageImpl<>(
                    namedParameterJdbcTemplate.query(sql.toString(), params, conditionProblemListDBRecordRowMapper()),
                    Pageable.unpaged(),
                    total);
        }

        sql.append(" ORDER BY l.condition_date DESC limit :limit offset :offset ");
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        List<ConditionProblemListItemDBRecord> records =
                namedParameterJdbcTemplate.query(sql.toString(), params, conditionProblemListDBRecordRowMapper());

        return new PageImpl<>(records, PageRequest.of(offset / limit, limit), total);
    }

    private long countTotal(StringBuilder filteredSql, MapSqlParameterSource params) {
        String countSql = "SELECT COUNT(*) FROM (" + filteredSql + ") cnt";
        Long total = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
        return total != null ? total : 0L;
    }

    private void addFilterInConditionProblemListItem(
            StringBuilder sql,
            MapSqlParameterSource params,
            ConditionSearchQuery conditionSearchQuery
    ) {
        if (conditionSearchQuery.getPatientId() != null) {
            String uuid = conditionSearchQuery.getPatientId();
            byte[] binaryUuid = toBytes(UUID.fromString(uuid));
            sql.append(" AND pd.puuid = :patientUuid ");
            params.addValue("patientUuid", binaryUuid);
        }
    }

    @Override
    public List<ConditionEncounterDiagnosisDBRecord> findConditionEncounterDiagnosisById(UUID uuid) {
        StringBuilder sql = conditionEncounterDiagnosisQuery();

        sql.append(" AND ie.uuid= :uuid");
        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, conditionEncounterDiagnosisDBRecordRowMapper());
    }

    @Override
    public Page<ConditionEncounterDiagnosisDBRecord> findConditionEncounterDiagnosis(ConditionSearchQuery conditionSearchQuery) {

        StringBuilder sql = conditionEncounterDiagnosisQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        long total = countTotal(sql, params);

        Integer limit = conditionSearchQuery.getCount();
        int offset = conditionSearchQuery.getOffset() != null ? conditionSearchQuery.getOffset() : 0;

        if (limit == null) {
            return new PageImpl<>(
                    namedParameterJdbcTemplate.query(sql.toString(), params, conditionEncounterDiagnosisDBRecordRowMapper()),
                    Pageable.unpaged(),
                    total);
        }

        sql.append(" ORDER BY ie.date DESC limit :limit offset :offset ");
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        List<ConditionEncounterDiagnosisDBRecord> records =
                namedParameterJdbcTemplate.query(sql.toString(), params, conditionEncounterDiagnosisDBRecordRowMapper());

        return new PageImpl<>(records, PageRequest.of(offset / limit, limit), total);
    }

    @Override
    public List<ConditionHealthConcernDBRecord> findConditionHealthConcernDiagnosisById(UUID uuid) {
        StringBuilder sql = conditionHealthConcernQuery();

        sql.append(" AND l.uuid= :uuid");
        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, conditionHealthConcernDBRecordRowMapper());
    }

    @Override
    public Page<ConditionHealthConcernDBRecord> findConditionHealthConcernDiagnosis(ConditionSearchQuery conditionSearchQuery) {

        StringBuilder sql = conditionHealthConcernQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        long total = countTotal(sql, params);

        Integer limit = conditionSearchQuery.getCount();
        int offset = conditionSearchQuery.getOffset() != null ? conditionSearchQuery.getOffset() : 0;

        if (limit == null) {
            return new PageImpl<>(
                    namedParameterJdbcTemplate.query(sql.toString(), params, conditionHealthConcernDBRecordRowMapper()),
                    Pageable.unpaged(),
                    total);
        }

        sql.append(" ORDER BY l.date DESC limit :limit offset :offset ");
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        List<ConditionHealthConcernDBRecord> records =
                namedParameterJdbcTemplate.query(sql.toString(), params, conditionHealthConcernDBRecordRowMapper());

        return new PageImpl<>(records, PageRequest.of(offset / limit, limit), total);
    }

    private StringBuilder conditionProblemListItemQuery() {
        return new StringBuilder("""
                SELECT
                    l.id,
                    l.uuid,
                    l.pid,
                    l.condition_date,
                    l.modifydate,
                    l.type,
                    l.title,
                    l.begdate,
                    l.enddate,
                    l.diagnosis,
                    l.activity,
                    l.comments,
                    l.occurrence,
                    l.outcome,
                    l.verification,
                    pd.puuid,
                    l.last_updated_time
                FROM (
                    SELECT
                        id,
                        uuid,
                        pid,
                        date AS condition_date,
                        modifydate,
                        type,
                        title,
                        begdate,
                        enddate,
                        diagnosis,
                        activity,
                        comments,
                        occurrence,
                        outcome,
                        verification,
                        COALESCE(modifydate, date) as last_updated_time
                    FROM
                        lists
                ) l
                INNER JOIN (
                    SELECT
                        uuid AS puuid
                        ,pid AS patient_id
                    FROM patient_data
                ) pd ON l.pid = pd.patient_id
                LEFT JOIN (
                    SELECT
                        list_id
                        ,pid AS issue_encounter_pid
                    FROM issue_encounter
                ) ie ON l.id = ie.list_id AND l.pid = ie.issue_encounter_pid WHERE 1=1
                """);
    }

    private RowMapper<ConditionProblemListItemDBRecord> conditionProblemListDBRecordRowMapper() {
        return (rs, rowNum) -> ConditionProblemListItemDBRecord.builder()
                .id(rs.getLong("id"))
                .uuid(toUuid(rs.getBytes("uuid")))
                .pid(rs.getObject("pid") != null ? rs.getLong("pid") : null)
                .conditionDate(toLocalDateTime(rs.getTimestamp("condition_date")))
                .modifydate(toLocalDateTime(rs.getTimestamp("modifydate")))
                .type(rs.getString("type"))
                .title(rs.getString("title"))
                .begdate(toLocalDateTime(rs.getTimestamp("begdate")))
                .enddate(toLocalDateTime(rs.getTimestamp("enddate")))
                .diagnosis(rs.getString("diagnosis"))
                .activity(rs.getObject("activity") != null ? rs.getInt("activity") : null)
                .comments(rs.getString("comments"))
                .occurrence(rs.getObject("occurrence") != null ? rs.getInt("occurrence") : null)
                .outcome(rs.getObject("outcome") != null ? rs.getInt("outcome") : null)
                .verification(rs.getString("verification"))
                .puuid(toUuid(rs.getBytes("puuid")))
                .lastUpdatedTime(toLocalDateTime(rs.getTimestamp("last_updated_time")))
                .build();
    }

    private StringBuilder conditionEncounterDiagnosisQuery() {
        return new StringBuilder("""
                SELECT
                     l.id,
                     ie.uuid,
                     l.lists_uuid,
                     l.pid,
                     l.modifydate,
                     l.type,
                     l.title,
                     l.begdate,
                     l.enddate,
                     l.diagnosis,
                     l.activity,
                     l.comments,
                     l.occurrence,
                     l.outcome,
                     l.verification,
                     ie.date,
                     ie.encounter_uuid,
                     ie.encounter_id,
                     ie.encounter_date,
                     ie.creator_uuid,
                     ie.creator_npi,
                     ie.updator_uuid,
                     ie.updator_npi,
                     ie.resolved,
                     pd.puuid,
                     l.last_updated_time
                 FROM (
                     SELECT
                         l.id,
                         l.date,
                         l.modifydate,
                         COALESCE(l.modifydate, l.date) as last_updated_time,
                         l.uuid AS lists_uuid,
                         l.pid,
                         l.type,
                         l.title,
                         l.begdate,
                         l.enddate,
                         l.diagnosis,
                         l.activity,
                         l.comments,
                         l.occurrence,
                         l.outcome,
                         l.verification
                     FROM lists l
                 ) l
                 INNER JOIN (
                     SELECT issue_encounter.uuid,
                            issue_encounter.list_id,
                            issue_encounter.pid,
                            issue_encounter.resolved,
                            issue_encounter.created_at AS date,
                            fe.uuid AS encounter_uuid,
                            fe.encounter as encounter_id,
                            fe.date AS encounter_date,
                            creator.creator_uuid,
                            creator.creator_npi,
                            updator.updator_uuid,
                            updator.updator_npi
                     FROM issue_encounter
                     INNER JOIN form_encounter fe USING(encounter,pid)
                     LEFT JOIN (
                         select
                             uuid AS creator_uuid
                              ,npi AS creator_npi
                             , id AS creator_id
                         FROM users
                     ) creator ON issue_encounter.created_by = creator.creator_id
                     LEFT JOIN (
                         select
                             uuid AS updator_uuid
                             ,npi AS updator_npi
                             , id AS updator_id
                         FROM users
                     ) updator ON issue_encounter.updated_by = updator.updator_id
                 ) ie ON l.id = ie.list_id AND l.pid = ie.pid
                 INNER JOIN (
                     SELECT
                         pid AS patient_id
                         ,uuid AS puuid
                     FROM patient_data
                 ) pd ON l.pid = pd.patient_id WHERE 1=1
                """);
    }

    private RowMapper<ConditionEncounterDiagnosisDBRecord> conditionEncounterDiagnosisDBRecordRowMapper() {
        return (rs, rowNum) -> ConditionEncounterDiagnosisDBRecord.builder()
                .id(rs.getLong("id"))
                .uuid(toUuid(rs.getBytes("uuid")))
                .listsUuid(rs.getString("lists_uuid"))
                .pid(rs.getObject("pid") != null ? rs.getLong("pid") : null)
                .modifydate(toLocalDateTime(rs.getTimestamp("modifydate")))
                .type(rs.getString("type"))
                .title(rs.getString("title"))
                .begdate(toLocalDateTime(rs.getTimestamp("begdate")))
                .enddate(toLocalDateTime(rs.getTimestamp("enddate")))
                .diagnosis(rs.getString("diagnosis"))
                .activity(rs.getObject("activity") != null ? rs.getInt("activity") : null)
                .comments(rs.getString("comments"))
                .occurrence(rs.getObject("occurrence") != null ? rs.getInt("occurrence") : null)
                .outcome(rs.getObject("outcome") != null ? rs.getInt("outcome") : null)
                .verification(rs.getString("verification"))
                .date(toLocalDateTime(rs.getTimestamp("date")))
                .encounterUuid(toUuid(rs.getBytes("encounter_uuid")))
                .encounterId(rs.getObject("encounter_id") != null ? rs.getLong("encounter_id") : null)
                .encounterDate(toLocalDateTime(rs.getTimestamp("encounter_date")))
                .creatorUuid(toUuid(rs.getBytes("creator_uuid")))
                .creatorNpi(rs.getString("creator_npi"))
                .updatorUuid(toUuid(rs.getBytes("updator_uuid")))
                .updatorNpi(rs.getString("updator_npi"))
                .resolved(rs.getObject("resolved") != null ? rs.getInt("resolved") : null)
                .puuid(toUuid(rs.getBytes("puuid")))
                .lastUpdatedTime(toLocalDateTime(rs.getTimestamp("last_updated_time")))
                .build();
    }

    private StringBuilder conditionHealthConcernQuery() {
        return new StringBuilder("""
                SELECT
                    l.id,
                    l.uuid,
                    l.pid,
                    l.date AS condition_date,
                    l.modifydate,
                    l.type,
                    l.title,
                    l.begdate,
                    l.enddate,
                    l.diagnosis,
                    l.activity,
                    l.comments,
                    l.occurrence,
                    l.outcome,
                    l.verification,
                    lo_healthconcerns.health_concern_subtype,
                    lo_healthconcerns.health_concern_subtype_title,
                    pd.puuid,
                    l.modifydate AS last_updated_time
                FROM lists l
                INNER JOIN (
                    SELECT
                        uuid AS puuid
                        ,pid AS patient_id
                    FROM patient_data
                ) pd ON l.pid = pd.patient_id
                LEFT JOIN (
                    select
                        option_id AS health_concern_subtype,
                        title AS health_concern_subtype_title
                    FROM list_options
                    WHERE list_id='Observation_Types'
                ) AS lo_healthconcerns ON l.subtype = lo_healthconcerns.health_concern_subtype 
                """);
    }

    private RowMapper<ConditionHealthConcernDBRecord> conditionHealthConcernDBRecordRowMapper() {
        return (rs, rowNum) -> ConditionHealthConcernDBRecord.builder()
                .id(rs.getLong("id"))
                .uuid(toUuid(rs.getBytes("uuid")))
                .pid(rs.getObject("pid") != null ? rs.getLong("pid") : null)
                .conditionDate(toLocalDateTime(rs.getTimestamp("condition_date")))
                .modifydate(toLocalDateTime(rs.getTimestamp("modifydate")))
                .type(rs.getString("type"))
                .title(rs.getString("title"))
                .begdate(toLocalDateTime(rs.getTimestamp("begdate")))
                .enddate(toLocalDateTime(rs.getTimestamp("enddate")))
                .diagnosis(rs.getString("diagnosis"))
                .activity(rs.getObject("activity") != null ? rs.getInt("activity") : null)
                .comments(rs.getString("comments"))
                .occurrence(rs.getObject("occurrence") != null ? rs.getInt("occurrence") : null)
                .outcome(rs.getObject("outcome") != null ? rs.getInt("outcome") : null)
                .verification(rs.getString("verification"))
                .healthConcernSubtype(rs.getString("health_concern_subtype"))
                .healthConcernSubtypeTitle(rs.getString("health_concern_subtype_title"))
                .puuid(toUuid(rs.getBytes("puuid")))
                .lastUpdatedTime(toLocalDateTime(rs.getTimestamp("last_updated_time")))
                .build();
    }

}
