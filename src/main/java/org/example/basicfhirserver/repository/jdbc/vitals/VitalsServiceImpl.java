package org.example.basicfhirserver.repository.jdbc.vitals;

import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.utils.DBUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.*;

@Repository
public class VitalsServiceImpl implements VitalsService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public VitalsServiceImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<VitalsDBRecord> findVitals(
            ObservationSearchQuery searchQuery
    ) {
        StringBuilder sql = vitalsQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        addPatientFilter(
                sql,
                params,
                searchQuery
        );
        addDateFilter(
                sql,
                params,
                searchQuery
        );

        int limit = (searchQuery.getCount() != null) ? searchQuery.getCount() : 5;
        int offset = (searchQuery.getOffset() != null) ? searchQuery.getOffset() : 0;

        sql.append("""
                ORDER BY vitals.date DESC limit :limit offset :offset
                """);

        params.addValue("limit", limit);
        params.addValue("offset", offset);

        return namedParameterJdbcTemplate.query(
                sql.toString(),
                params,
                vitalObservationRowMapper()
        );
    }

    @Override
    public List<VitalsDBRecord> findVitalsById(UUID uuid) {

        StringBuilder sql = vitalsQuery();
        sql.append(" AND vitals.uuid = :uuid");

        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(
                sql.toString(),
                parameters,
                vitalObservationRowMapper()
        );

    }

    @Override
    public List<VitalsUuidMappingDBRecord> findVitalsUuidMappings(List<UUID> vitalsUuid) {

        String sql = """
                SELECT uuid, resource, `table`, target_uuid, resource_path
                FROM uuid_mapping
                WHERE target_uuid IN (:targetUuids)
                """;

        List<byte[]> binaryUuids = vitalsUuid.stream()
                .map(DBUtils::toBytes)
                .toList();

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("targetUuids", binaryUuids);

        return namedParameterJdbcTemplate.query(
                sql,
                parameters,
                vitalObservationUuidRowMapper()
        );
    }

    @Override
    public List<VitalsUuidMappingDBRecord> findVitalsUuidMappingsById(UUID uuid) {

        String sql = """
                SELECT uuid, resource, `table`, target_uuid, resource_path
                FROM uuid_mapping
                WHERE uuid = :uuid
                """;

        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(
                sql,
                parameters,
                vitalObservationUuidRowMapper()
        );
    }

    private StringBuilder vitalsQuery() {

        return new StringBuilder("""
                SELECT
                    patient.uuid AS patient_uuid,
                    encounter.uuid AS encounter_uuid,
                    user.uuid AS practitioner_uuid,
                
                    vitals.uuid AS vitals_uuid,
                    vitals.date AS effective_date_time,
                
                    vitals.weight,
                    vitals.height,
                    vitals.temperature,
                    vitals.BMI AS bmi,
                    vitals.bps,
                    vitals.bpd,
                
                    vitals.last_updated AS lastUpdated
                
                FROM form_vitals vitals
                
                JOIN forms form
                    ON form.form_id = vitals.id
                
                LEFT JOIN patient_data patient
                    ON patient.pid = vitals.pid
                
                LEFT JOIN form_encounter encounter
                    ON encounter.encounter = form.encounter
                    AND encounter.pid = vitals.pid
                
                LEFT JOIN users user
                    ON user.username = vitals.user
                
                WHERE form.formdir = 'vitals'
                """);
    }

    private void addPatientFilter(
            StringBuilder sql,
            MapSqlParameterSource parameters,
            ObservationSearchQuery searchQuery
    ) {
        if (searchQuery.getPatientId() == null) {
            return;
        }
        sql.append("""
                AND patient.uuid IN (:patient_uuid)
                """);

        List<byte[]> binaryUuids = searchQuery.getPatientId().stream()
                .map(idStr -> toBytes(UUID.fromString(idStr)))
                .toList();

        parameters.addValue("patient_uuid",
                binaryUuids
        );
    }

    private void addDateFilter(
            StringBuilder sql,
            MapSqlParameterSource parameters,
            ObservationSearchQuery searchQuery
    ) {
        if (searchQuery.getDate() == null ||
                searchQuery.getDate().getValue() == null) {
            return;
        }

        LocalDateTime date =
                searchQuery.getDate().getValue();

        if (searchQuery.getDate().getPrefix() == null) {
            sql.append(" AND vitals.date = :date ");
            parameters.addValue("date", date);
            return;
        }

        switch (searchQuery.getDate().getPrefix()) {

            case GREATERTHAN:
                sql.append(" AND vitals.date > :date");
                break;

            case GREATERTHAN_OR_EQUALS:
                sql.append(" AND vitals.date >= :date");
                break;

            case LESSTHAN:
                sql.append(" AND vitals.date < :date");
                break;

            case LESSTHAN_OR_EQUALS:
                sql.append(" AND vitals.date <= :date");
                break;

            case NOT_EQUAL:
                sql.append(" AND vitals.date <> :date");
                break;

            case EQUAL:
            case APPROXIMATE:
            case STARTS_AFTER:
            case ENDS_BEFORE:
            default:
                sql.append(" AND vitals.date = :date");
                break;
        }
        parameters.addValue("date", date);
    }

    private RowMapper<VitalsDBRecord> vitalObservationRowMapper() {
        return (rs, rowNum) -> VitalsDBRecord.builder()
                .vitalsUuid(toUuid(rs.getBytes("vitals_uuid")))
                .patientUuid(toUuid(rs.getBytes("patient_uuid")))
                .encounterUuid(toUuid(rs.getBytes("encounter_uuid")))
                .practitionerUuid(toUuid(rs.getBytes("practitioner_uuid")))
                .effectiveDateTime(toLocalDateTime(
                        rs.getTimestamp(
                                "effective_date_time"
                        )
                ))
                .weight(rs.getBigDecimal("weight"))
                .height(rs.getBigDecimal("height"))
                .temperature(rs.getBigDecimal("temperature"))
                .bmi(rs.getBigDecimal("bmi"))
                .systolic(rs.getBigDecimal("bps"))

                .diastolic(rs.getBigDecimal("bpd"))
                .lastUpdated(toLocalDateTime(
                        rs.getTimestamp(
                                "lastUpdated"
                        )
                ))
                .build();
    }

    private RowMapper<VitalsUuidMappingDBRecord> vitalObservationUuidRowMapper() {
        return (rs, rowNum) -> VitalsUuidMappingDBRecord.builder()
                .uuid(toUuid(rs.getBytes("uuid")))
                .resource(rs.getString("resource"))
                .table(rs.getString("table"))
                .targetUuid(toUuid(rs.getBytes("target_uuid")))
                .resourcePath(rs.getString("resource_path"))
                .build();
    }


}