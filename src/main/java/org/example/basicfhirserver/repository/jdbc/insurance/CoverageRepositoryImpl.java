package org.example.basicfhirserver.repository.jdbc.insurance;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.*;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.coverage.CoverageSearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CoverageRepositoryImpl implements CoverageRepository {

  private static final int DEFAULT_PAGE_SIZE = 5;
  private static final int DEFAULT_PAGE_OFFSET = 0;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public CoverageRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public List<CoverageDBRecord> findById(UUID uuid) {
    StringBuilder sql = coverageQuery();

    sql.append(" AND insurance_data.uuid = :uuid");

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("uuid", toBytes(uuid));

    return namedParameterJdbcTemplate.query(
        sql.toString(), parameters, coverageDBRecordRowMapper());
  }

  @Override
  public Page<CoverageDBRecord> find(CoverageSearchQuery coverageSearchQuery) {
    StringBuilder sql = coverageQuery();
    MapSqlParameterSource params = new MapSqlParameterSource();
    addFilter(sql, params, coverageSearchQuery);

    long total = countTotal(sql, params);

    int offset =
        coverageSearchQuery.getOffset() != null
            ? coverageSearchQuery.getOffset()
            : DEFAULT_PAGE_OFFSET;
    int limit =
        coverageSearchQuery.getCount() != null ? coverageSearchQuery.getCount() : DEFAULT_PAGE_SIZE;

    sql.append(
        """
                ORDER BY `patient_data_pid` ASC,
                         `type` ASC,
                         (`date_end` IS NULL OR `date_end` > NOW()) DESC,
                         (`date_end` IS NOT NULL AND `date_end` > NOW()) DESC,
                         `date` DESC,
                         `date_end` DESC,
                         `policy_number` ASC
                limit :limit offset :offset
                """);
    params.addValue("limit", limit);
    params.addValue("offset", offset);

    List<CoverageDBRecord> records =
        namedParameterJdbcTemplate.query(sql.toString(), params, coverageDBRecordRowMapper());

    return new PageImpl<>(records, PageRequest.of(offset / limit, limit), total);
  }

  private long countTotal(StringBuilder filteredSql, MapSqlParameterSource params) {
    String countSql = "SELECT COUNT(*) from ( %s ) as query_count";
    Long total =
        namedParameterJdbcTemplate.queryForObject(
            countSql.formatted(filteredSql), params, Long.class);
    return total != null ? total : 0L;
  }

  private StringBuilder coverageQuery() {
    return new StringBuilder(
        """
                SELECT `insurance_data`.*,
                       `puuid`,
                       `insureruuid`
                FROM `insurance_data`
                LEFT JOIN (
                        SELECT
                            `uuid` AS `insureruuid`,
                            `id` AS `insurerid`,
                            `ins_type_code`
                        FROM `insurance_companies`
                ) `insurance_company_data` ON `insurance_data`.`provider` = `insurance_company_data`.`insurerid`
                LEFT JOIN (
                        SELECT
                            `pid` AS `patient_data_pid`,
                            `uuid` AS `puuid`
                        FROM `patient_data`
                ) `patient_data` ON `insurance_data`.`pid` = `patient_data`.`patient_data_pid`
                WHERE 1=1
                """);
  }

  private void addFilter(
      StringBuilder sql, MapSqlParameterSource params, CoverageSearchQuery coverageSearchQuery) {
    if (coverageSearchQuery.getId() != null) {
      byte[] binaryUuid = toBytes(UUID.fromString(coverageSearchQuery.getId()));
      sql.append(" AND insurance_data.uuid = :coverageUuid ");
      params.addValue("coverageUuid", binaryUuid);
    }

    if (coverageSearchQuery.getPatientId() != null) {
      byte[] binaryUuid = toBytes(UUID.fromString(coverageSearchQuery.getPatientId()));
      sql.append(" AND patient_data.puuid = :patientUuid ");
      params.addValue("patientUuid", binaryUuid);
    }
  }

  private RowMapper<CoverageDBRecord> coverageDBRecordRowMapper() {
    return (rs, rowNum) ->
        CoverageDBRecord.builder()
            .id(rs.getLong("id"))
            .type(rs.getString("type"))
            .provider(rs.getObject("provider") != null ? rs.getLong("provider") : null)
            .planName(rs.getString("plan_name"))
            .policyNumber(rs.getString("policy_number"))
            .policyType(rs.getString("policy_type"))
            .groupNumber(rs.getString("group_number"))
            .subscriberLname(rs.getString("subscriber_lname"))
            .subscriberFname(rs.getString("subscriber_fname"))
            .subscriberMname(rs.getString("subscriber_mname"))
            .subscriberDob(toLocalDateTime(rs.getTimestamp("subscriber_dob")))
            .subscriberSsn(rs.getString("subscriber_ssn"))
            .subscriberSex(rs.getString("subscriber_sex"))
            .subscriberStreet(rs.getString("subscriber_street"))
            .subscriberCity(rs.getString("subscriber_city"))
            .subscriberState(rs.getString("subscriber_state"))
            .subscriberPostalCode(rs.getString("subscriber_postal_code"))
            .subscriberCountry(rs.getString("subscriber_country"))
            .subscriberPhone(rs.getString("subscriber_phone"))
            .subscriberRelationship(rs.getString("subscriber_relationship"))
            .subscriberEmployer(rs.getString("subscriber_employer"))
            .subscriberEmployerStreet(rs.getString("subscriber_employer_street"))
            .subscriberEmployerCity(rs.getString("subscriber_employer_city"))
            .subscriberEmployerState(rs.getString("subscriber_employer_state"))
            .subscriberEmployerPostalCode(rs.getString("subscriber_employer_postal_code"))
            .subscriberEmployerCountry(rs.getString("subscriber_employer_country"))
            .subscriberEmployerPhone(rs.getString("subscriber_employer_phone"))
            .date(toLocalDateTime(rs.getTimestamp("date")))
            .dateEnd(toLocalDateTime(rs.getTimestamp("date_end")))
            .pid(rs.getObject("pid") != null ? rs.getLong("pid") : null)
            .acceptAssignment(rs.getString("accept_assignment"))
            .subNo(rs.getObject("sub_no") != null ? rs.getInt("sub_no") : null)
            .copay(rs.getBigDecimal("copay"))
            .uuid(toUuid(rs.getBytes("uuid")))
            .puuid(toUuid(rs.getBytes("puuid")))
            .insureruuid(toUuid(rs.getBytes("insureruuid")))
            .insTypeCode(rs.getObject("ins_type_code") != null ? rs.getInt("ins_type_code") : null)
            .build();
  }
}
