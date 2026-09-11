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
public class ClinicalNotesRepositoryImpl implements ClinicalNotesRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ClinicalNotesRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<ClinicalNotesDBRecord> findClinicalNotesById(UUID uuid) {
        return List.of();
    }

    @Override
    public List<ClinicalNotesDBRecord> findClinicalNotes() {
        StringBuilder sql = clinicalNotesListItemQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        return namedParameterJdbcTemplate.query(sql.toString(), params, clinicalNotesListDBRecordRowMapper());
    }

    private StringBuilder clinicalNotesListItemQuery() {
        return new StringBuilder("""
                SELECT
                    notes.id
                    ,notes.uuid AS uuid
                    ,notes.activity
                    ,notes.date
                    ,notes.code
                    ,notes.codetext
                    ,notes.description
                    ,notes.external_id
                    ,notes.clinical_notes_type
                    ,notes.note_related_to
                    ,notes.clinical_notes_category
                    ,notes.last_updated
                    ,forms.date_created
                    ,lo_category.category_code
                    ,lo_category.category_title
                    ,patients.pid
                    ,patients.puuid
                    ,encounters.eid
                    ,encounters.euuid
                    ,encounters.encounter_date
                    ,users.username
                    ,users.user_uuid
                    ,users.npi
                    ,users.physician_type
                FROM
                    (
                        select
                            id
                            ,uuid
                            ,activity
                            ,`date`
                            ,`code`
                            ,codetext
                            ,`description`
                            ,`pid` AS notes_pid
                            ,external_id
                            ,clinical_notes_type
                            ,note_related_to
                            ,clinical_notes_category
                            ,form_id
                            ,last_updated
                            ,user
                     FROM
                        form_clinical_notes
                 ) notes
                JOIN (
                    SELECT
                        id
                        ,form_id
                        ,encounter
                        ,pid AS form_pid
                        ,`date` AS date_created
                    FROM
                        forms
                    WHERE formdir = 'clinical_notes'
                ) forms ON forms.form_id = notes.form_id AND forms.form_pid = notes.notes_pid
                LEFT JOIN (
                    select
                        encounter AS eid
                        ,uuid AS euuid
                        ,`date` AS encounter_date
                        ,`pid` AS encounter_pid
                    FROM
                        form_encounter
                ) encounters ON encounters.eid = forms.encounter AND forms.form_pid = encounters.encounter_pid
                LEFT JOIN
                (
                    SELECT
                        uuid AS puuid
                        ,pid
                        FROM patient_data
                ) patients ON forms.form_pid = patients.pid
                LEFT JOIN
                (
                    SELECT
                        uuid AS user_uuid
                        ,username
                        ,id AS uid
                        ,npi
                        ,physician_type
                        FROM
                            users
                ) users ON notes.`user` = users.username
                LEFT JOIN
                (
                    SELECT
                        notes AS category_code
                        ,title AS category_title
                        ,option_id
                    FROM
                        list_options
                    WHERE
                        list_id = 'Clinical_Note_Category'
                            ) lo_category ON notes.clinical_notes_category = lo_category.option_id WHERE 1=1
                """);
    }


    private RowMapper<ClinicalNotesDBRecord> clinicalNotesListDBRecordRowMapper() {
        return (rs, rowNum) -> ClinicalNotesDBRecord.builder()
                .id(rs.getLong("id"))
                .uuid(toUuid(rs.getBytes("uuid")))
                .activity(rs.getObject("activity") != null ? rs.getInt("activity") : null)
                .date(toLocalDateTime(rs.getTimestamp("date")))
                .code(rs.getString("code"))
                .codetext(rs.getString("codetext"))
                .description(rs.getString("description"))
                .externalId(rs.getString("external_id"))
                .clinicalNotesType(rs.getString("clinical_notes_type"))
                .noteRelatedTo(rs.getString("note_related_to"))
                .clinicalNotesCategory(rs.getString("clinical_notes_category"))
                .lastUpdated(toLocalDateTime(rs.getTimestamp("last_updated")))
                .dateCreated(toLocalDateTime(rs.getTimestamp("date_created")))
                .categoryCode(rs.getString("category_code"))
                .categoryTitle(rs.getString("category_title"))
                .pid(rs.getObject("pid") != null ? rs.getLong("pid") : null)
                .puuid(toUuid(rs.getBytes("puuid")))
                .eid(rs.getObject("eid") != null ? rs.getLong("eid") : null)
                .euuid(toUuid(rs.getBytes("euuid")))
                .encounterDate(toLocalDateTime(rs.getTimestamp("encounter_date")))
                .username(rs.getString("username"))
                .userUuid(rs.getString("user_uuid"))
                .npi(rs.getString("npi"))
                .physicianType(rs.getString("physician_type"))
                .build();
    }


}
