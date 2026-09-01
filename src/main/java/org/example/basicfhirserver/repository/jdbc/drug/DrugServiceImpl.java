package org.example.basicfhirserver.repository.jdbc.drug;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.*;

@Repository
public class DrugServiceImpl implements DrugService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DrugServiceImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<DrugDBRecord> findById(UUID uuid) {

        StringBuilder sql = drugQuery();

        sql.append(" AND drug_table.uuid= :uuid");
        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(sql.toString(), parameters, drugDBRecordRowMapper());

    }


    @Override
    public List<DrugDBRecord> find() {

        StringBuilder sql = drugQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        return namedParameterJdbcTemplate.query(sql.toString(), params, drugDBRecordRowMapper());

    }


    private StringBuilder drugQuery() {
        return new StringBuilder("""
                SELECT
                    drug_table.drug_id,
                    drug_table.uuid,
                    drug_table.name,
                    drug_table.ndc_number,
                    drug_table.form,
                    drug_table.size,
                    drug_table.unit,
                    drug_table.route,
                    drug_table.related_code,
                    drug_table.active,
                    drug_table.drug_code,
                    IF(drug_prescriptions.rxnorm_drugcode!=''
                            ,drug_prescriptions.rxnorm_drugcode
                            ,IF(drug_table.drug_code IS NULL, '', drug_table.drug_code)
                    ) AS 'rxnorm_drugcode',
                    drug_inventory.manufacturer,
                    drug_inventory.lot_number,
                    drug_inventory.expiration,
                    drug_table.drug_last_updated,
                    drug_table.drug_date_created
                    FROM (
                        select
                            drug_id,
                            uuid,
                            name,
                            ndc_number,
                            form,
                            size,
                            unit,
                            route,
                            related_code,
                            active,
                            drug_code,
                            last_updated AS drug_last_updated,
                            date_created AS drug_date_created
                        FROM
                            drugs
                    ) drug_table
                    LEFT JOIN drug_inventory
                        ON drug_table.drug_id = drug_inventory.drug_id
                    LEFT JOIN (
                        select
                            uuid AS prescription_uuid
                            ,rxnorm_drugcode
                            ,drug_id
                            ,patient_id as prescription_patient_id
                        FROM
                        prescriptions
                    ) drug_prescriptions
                        ON drug_prescriptions.drug_id = drug_table.drug_id
                    LEFT JOIN (
                        select uuid AS puuid
                        ,pid
                        FROM patient_data
                    ) patient
                    ON patient.pid = drug_prescriptions.prescription_patient_id WHERE 1=1
                """);
    }


    private RowMapper<DrugDBRecord> drugDBRecordRowMapper() {
        return (rs, rowNum) -> DrugDBRecord.builder()
                .drugId(rs.getLong("drug_id"))
                .uuid(toUuid(rs.getBytes("uuid")))
                .name(rs.getString("name"))
                .ndcNumber(rs.getString("ndc_number"))
                .form(rs.getString("form"))
                .size(rs.getString("size"))
                .unit(rs.getString("unit"))
                .route(rs.getString("route"))
                .relatedCode(rs.getString("related_code"))
                .active(rs.getObject("active") != null ? rs.getInt("active") : null)
                .drugCode(rs.getString("drug_code"))
                .rxnormDrugcode(rs.getString("rxnorm_drugcode"))
                .manufacturer(rs.getString("manufacturer"))
                .lotNumber(rs.getString("lot_number"))
                .expiration(toLocalDateTime(rs.getTimestamp("expiration")))
                .drugLastUpdated(toLocalDateTime(rs.getTimestamp("drug_last_updated")))
                .drugDateCreated(toLocalDateTime(rs.getTimestamp("drug_date_created")))
                .build();
    }

}
