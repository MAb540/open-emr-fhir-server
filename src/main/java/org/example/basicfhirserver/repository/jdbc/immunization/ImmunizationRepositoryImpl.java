package org.example.basicfhirserver.repository.jdbc.immunization;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.immunization.ImmunizationSearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImmunizationRepositoryImpl implements ImmunizationRepository {

  private static final int DEFAULT_PAGE_SIZE = 5;
  private static final int DEFAULT_PAGE_OFFSET = 0;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public ImmunizationRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public List<ImmunizationDBRecord> findById(UUID uuid) {
    StringBuilder sql = immunizationQuery();

    sql.append(" AND immunizations.uuid = :uuid");

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("uuid", toBytes(uuid));

    return namedParameterJdbcTemplate.query(
        sql.toString(), parameters, immunizationDBRecordRowMapper());
  }

  @Override
  public Page<ImmunizationDBRecord> find(ImmunizationSearchQuery immunizationSearchQuery) {
    StringBuilder sql = immunizationQuery();
    MapSqlParameterSource params = new MapSqlParameterSource();
    addFilter(sql, params, immunizationSearchQuery);

    long total = countTotal(sql, params);

    int offset =
        immunizationSearchQuery.getOffset() != null
            ? immunizationSearchQuery.getOffset()
            : DEFAULT_PAGE_OFFSET;
    int limit =
        immunizationSearchQuery.getCount() != null
            ? immunizationSearchQuery.getCount()
            : DEFAULT_PAGE_SIZE;

    sql.append(" ORDER BY immunizations.administered_date DESC limit :limit offset :offset ");
    params.addValue("limit", limit);
    params.addValue("offset", offset);

    List<ImmunizationDBRecord> records =
        namedParameterJdbcTemplate.query(sql.toString(), params, immunizationDBRecordRowMapper());

    return new PageImpl<>(records, PageRequest.of(offset / limit, limit), total);
  }

  private long countTotal(StringBuilder filteredSql, MapSqlParameterSource params) {
    String countSql = "SELECT COUNT(*) from ( %s ) as query_count";
    Long total =
        namedParameterJdbcTemplate.queryForObject(
            countSql.formatted(filteredSql), params, Long.class);
    return total != null ? total : 0L;
  }

  private StringBuilder immunizationQuery() {
    return new StringBuilder(
        """
                SELECT immunizations.id,
                       immunizations.uuid,
                       patient.puuid,
                       immunizations.administered_date,
                       immunizations.cvx_code,
                       cvx.code_text AS cvx_code_text,
                       immunizations.manufacturer,
                       immunizations.lot_number,
                       immunizations.added_erroneously,
                       immunizations.administered_by_id,
                       immunizations.administered_by,
                       immunizations.education_date,
                       immunizations.note,
                       immunizations.create_date,
                       immunizations.update_date,
                       immunizations.amount_administered,
                       immunizations.amount_administered_unit,
                       immunizations.expiration_date,
                       immunizations.route,
                       immunizations.administration_site,
                       site.title AS site_display,
                       site.notes AS site_code,
                       immunizations.completion_status,
                       immunizations.reason_code,
                       immunizations.refusal_reason,
                       refusal_reasons.refusal_reason_codes,
                       refusal_reasons.refusal_reason_cdc_nip_code,
                       refusal_reasons.refusal_reason_description,
                       providers.provider_uuid,
                       providers.provider_npi,
                       providers.provider_username,
                       IF(
                           IF(
                               immunizations.information_source = 'new_immunization_record' AND
                               IF(immunizations.administered_by IS NOT NULL OR immunizations.administered_by_id IS NOT NULL, TRUE, FALSE),
                               TRUE,
                               FALSE
                           ) OR
                           IF(
                               immunizations.information_source = 'other_provider' OR
                               immunizations.information_source = 'birth_certificate' OR
                               immunizations.information_source = 'school_record' OR
                               immunizations.information_source = 'public_agency',
                               TRUE,
                               FALSE
                           ),
                           TRUE,
                           FALSE
                       ) AS primarySource,
                       enc.euuid,
                       enc.facility_uuid,
                       enc.facility_location_uuid
                FROM immunizations
                LEFT JOIN (
                    SELECT uuid AS puuid,
                           pid
                    FROM patient_data
                ) patient ON immunizations.patient_id = patient.pid
                LEFT JOIN codes AS cvx ON cvx.code = immunizations.cvx_code
                LEFT JOIN list_options AS site ON site.option_id = immunizations.administration_site
                LEFT JOIN (
                    SELECT uuid AS provider_uuid,
                           npi AS provider_npi,
                           username AS provider_username,
                           id AS provider_id
                    FROM users
                ) providers ON immunizations.administered_by_id = providers.provider_id
                LEFT JOIN (
                    SELECT option_id AS refusal_reason_id,
                           notes AS refusal_reason_cdc_nip_code,
                           codes AS refusal_reason_codes,
                           title AS refusal_reason_description
                    FROM list_options
                    WHERE list_id = 'immunization_refusal_reason'
                ) refusal_reasons ON immunizations.refusal_reason = refusal_reasons.refusal_reason_id
                LEFT JOIN (
                    SELECT form_encounter.uuid AS euuid,
                           form_encounter.encounter AS eid,
                           facility.uuid AS facility_uuid,
                           uuid_mapping.uuid AS facility_location_uuid
                    FROM form_encounter
                    LEFT JOIN facility ON form_encounter.facility_id = facility.id
                    LEFT JOIN uuid_mapping ON facility.uuid = uuid_mapping.target_uuid
                    WHERE uuid_mapping.resource = 'Location'
                ) enc ON immunizations.encounter_id = enc.eid
                WHERE 1=1
                """);
  }

  private void addFilter(
      StringBuilder sql,
      MapSqlParameterSource params,
      ImmunizationSearchQuery immunizationSearchQuery) {
    if (immunizationSearchQuery.getId() != null) {
      byte[] binaryUuid = toBytes(UUID.fromString(immunizationSearchQuery.getId()));
      sql.append(" AND immunizations.uuid = :immunizationUuid ");
      params.addValue("immunizationUuid", binaryUuid);
    }

    if (immunizationSearchQuery.getPatientId() != null) {
      byte[] binaryUuid = toBytes(UUID.fromString(immunizationSearchQuery.getPatientId()));
      sql.append(" AND patient.puuid = :patientUuid ");
      params.addValue("patientUuid", binaryUuid);
    }

    addDateFilter(sql, params, immunizationSearchQuery);
    addLastUpdatedFilter(sql, params, immunizationSearchQuery);
  }

  private void addDateFilter(
      StringBuilder sql,
      MapSqlParameterSource params,
      ImmunizationSearchQuery immunizationSearchQuery) {
    if (immunizationSearchQuery.getDate() == null
        || immunizationSearchQuery.getDate().getValue() == null) {
      return;
    }

    LocalDateTime date = immunizationSearchQuery.getDate().getValue();

    if (immunizationSearchQuery.getDate().getPrefix() == null) {
      sql.append(" AND immunizations.administered_date = :date");
      params.addValue("date", date);
      return;
    }

    switch (immunizationSearchQuery.getDate().getPrefix()) {
      case GREATERTHAN, STARTS_AFTER:
        sql.append(" AND immunizations.administered_date > :date");
        break;
      case GREATERTHAN_OR_EQUALS:
        sql.append(" AND immunizations.administered_date >= :date");
        break;
      case LESSTHAN:
      case ENDS_BEFORE:
        sql.append(" AND immunizations.administered_date < :date");
        break;
      case LESSTHAN_OR_EQUALS:
        sql.append(" AND immunizations.administered_date <= :date");
        break;
      case NOT_EQUAL:
        sql.append(" AND immunizations.administered_date <> :date");
        break;
        case EQUAL:
      case APPROXIMATE:
      default:
        sql.append(" AND immunizations.administered_date = :date");
        break;
    }
    params.addValue("date", date);
  }

  private void addLastUpdatedFilter(
      StringBuilder sql,
      MapSqlParameterSource params,
      ImmunizationSearchQuery immunizationSearchQuery) {
    if (immunizationSearchQuery.getLastUpdated() == null
        || immunizationSearchQuery.getLastUpdated().getValue() == null) {
      return;
    }

    LocalDateTime lastUpdated = immunizationSearchQuery.getLastUpdated().getValue();

    if (immunizationSearchQuery.getLastUpdated().getPrefix() == null) {
      sql.append(" AND immunizations.update_date = :lastUpdated");
      params.addValue("lastUpdated", lastUpdated);
      return;
    }

    switch (immunizationSearchQuery.getLastUpdated().getPrefix()) {
      case GREATERTHAN:
        sql.append(" AND immunizations.update_date > :lastUpdated");
        break;
      case GREATERTHAN_OR_EQUALS:
        sql.append(" AND immunizations.update_date >= :lastUpdated");
        break;
      case LESSTHAN:
      case ENDS_BEFORE:
        sql.append(" AND immunizations.update_date < :lastUpdated");
        break;
      case LESSTHAN_OR_EQUALS:
        sql.append(" AND immunizations.update_date <= :lastUpdated");
        break;
      case NOT_EQUAL:
        sql.append(" AND immunizations.update_date <> :lastUpdated");
        break;
      case STARTS_AFTER:
        sql.append(" AND immunizations.update_date > :lastUpdated");
        break;
      case EQUAL:
      case APPROXIMATE:
      default:
        sql.append(" AND immunizations.update_date = :lastUpdated");
        break;
    }
    params.addValue("lastUpdated", lastUpdated);
  }

  private RowMapper<ImmunizationDBRecord> immunizationDBRecordRowMapper() {
    return (rs, rowNum) ->
        ImmunizationDBRecord.builder()
            .id(rs.getLong("id"))
            .uuid(toUuid(rs.getBytes("uuid")))
            .patientPuuid(toUuid(rs.getBytes("puuid")))
            .administeredDate(toLocalDateTime(rs.getTimestamp("administered_date")))
            .cvxCode(rs.getString("cvx_code"))
            .cvxCodeText(rs.getString("cvx_code_text"))
            .manufacturer(rs.getString("manufacturer"))
            .lotNumber(rs.getString("lot_number"))
            .addedErroneously(rs.getBoolean("added_erroneously"))
            .administeredById(
                rs.getObject("administered_by_id") != null
                    ? rs.getLong("administered_by_id")
                    : null)
            .administeredBy(rs.getString("administered_by"))
            .educationDate(toLocalDate(rs.getDate("education_date")))
            .note(rs.getString("note"))
            .createDate(toLocalDateTime(rs.getTimestamp("create_date")))
            .updateDate(toLocalDateTime(rs.getTimestamp("update_date")))
            .amountAdministered(
                rs.getObject("amount_administered") != null
                    ? rs.getInt("amount_administered")
                    : null)
            .amountAdministeredUnit(rs.getString("amount_administered_unit"))
            .expirationDate(toLocalDate(rs.getDate("expiration_date")))
            .route(rs.getString("route"))
            .administrationSite(rs.getString("administration_site"))
            .siteDisplay(rs.getString("site_display"))
            .siteCode(rs.getString("site_code"))
            .completionStatus(rs.getString("completion_status"))
            .reasonCode(rs.getString("reason_code"))
            .refusalReason(rs.getString("refusal_reason"))
            .refusalReasonCodes(rs.getString("refusal_reason_codes"))
            .refusalReasonCdcNipCode(rs.getString("refusal_reason_cdc_nip_code"))
            .refusalReasonDescription(rs.getString("refusal_reason_description"))
            .providerUuid(toUuid(rs.getBytes("provider_uuid")))
            .providerNpi(rs.getString("provider_npi"))
            .providerUsername(rs.getString("provider_username"))
            .primarySource(rs.getBoolean("primarySource"))
            .encounterEuuid(toUuid(rs.getBytes("euuid")))
            .facilityUuid(toUuid(rs.getBytes("facility_uuid")))
            .facilityLocationUuid(toUuid(rs.getBytes("facility_location_uuid")))
            .build();
  }
}
