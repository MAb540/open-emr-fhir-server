package org.example.basicfhirserver.repository.jdbc.facility;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toBytes;
import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toLocalDateTime;
import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toUuid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FacilityServiceImpl implements FacilityService {

  private static final int DEFAULT_PAGE_SIZE = 5;
  private static final int DEFAULT_PAGE_OFFSET = 0;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public FacilityServiceImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public List<FacilityDBRecord> findById(UUID uuid) {
    StringBuilder sql = facilityQuery();

    sql.append(" AND FAC.uuid = :uuid");
    byte[] binaryUuid = toBytes(uuid);

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("uuid", binaryUuid);

    return namedParameterJdbcTemplate.query(sql.toString(), parameters, facilityDBRecordRowMapper());
  }

  @Override
  public Page<FacilityDBRecord> find(OrganizationSearchQuery organizationSearchQuery) {
    StringBuilder sql = facilityQuery();
    MapSqlParameterSource params = new MapSqlParameterSource();
    addFilter(sql, params, organizationSearchQuery);

    long total = countTotal(sql, params);

    int offset =
        organizationSearchQuery.getOffset() != null
            ? organizationSearchQuery.getOffset()
            : DEFAULT_PAGE_OFFSET;
    int limit =
        organizationSearchQuery.getCount() != null
            ? organizationSearchQuery.getCount()
            : DEFAULT_PAGE_SIZE;

    sql.append(" ORDER BY FAC.id DESC limit :limit offset :offset ");
    params.addValue("limit", limit);
    params.addValue("offset", offset);

    List<FacilityDBRecord> records =
        namedParameterJdbcTemplate.query(sql.toString(), params, facilityDBRecordRowMapper());

    return new PageImpl<>(records, PageRequest.of(offset / limit, limit), total);
  }

  private long countTotal(StringBuilder filteredSql, MapSqlParameterSource params) {
    String countSql = "SELECT COUNT(*) from ( %s ) as query_count";
    Long total =
        namedParameterJdbcTemplate.queryForObject(
            countSql.formatted(filteredSql), params, Long.class);
    return total != null ? total : 0L;
  }

  private StringBuilder facilityQuery() {
    return new StringBuilder(
        """
                SELECT FAC.id,
                       FAC.uuid,
                       FAC.name,
                       FAC.phone,
                       FAC.fax,
                       FAC.street,
                       FAC.city,
                       FAC.state,
                       FAC.postal_code,
                       FAC.country_code,
                       FAC.federal_ein,
                       FAC.website,
                       FAC.email,
                       FAC.service_location,
                       FAC.billing_location,
                       FAC.accepts_assignment,
                       FAC.pos_code,
                       FAC.x12_sender_id,
                       FAC.attn,
                       FAC.domain_identifier,
                       FAC.facility_npi,
                       FAC.facility_taxonomy,
                       FAC.tax_id_type,
                       FAC.color,
                       FAC.primary_business_entity,
                       FAC.facility_code,
                       FAC.extra_validation,
                       FAC.mail_street,
                       FAC.mail_street2,
                       FAC.mail_city,
                       FAC.mail_state,
                       FAC.mail_zip,
                       FAC.oid,
                       FAC.iban,
                       FAC.info,
                       FAC.inactive,
                       FAC.last_updated
                FROM facility FAC
                WHERE 1=1
                """);
  }

  private void addFilter(
      StringBuilder sql, MapSqlParameterSource params, OrganizationSearchQuery organizationSearchQuery) {
    if (organizationSearchQuery.getOrganizationId() != null
        && !organizationSearchQuery.getOrganizationId().isEmpty()) {
      List<byte[]> binaryUuids =
          organizationSearchQuery.getOrganizationId().stream()
              .map(idStr -> toBytes(UUID.fromString(idStr)))
              .toList();
      sql.append(" AND FAC.uuid IN (:organizationUuid) ");
      params.addValue("organizationUuid", binaryUuids);
    }

    if (organizationSearchQuery.getName() != null) {
      String rawSearchValue = organizationSearchQuery.getName().getValue();
      if (organizationSearchQuery.getName().isContains()) {
        sql.append(" AND LOWER(FAC.name) LIKE :nameFilter ");
        params.addValue("nameFilter", "%" + rawSearchValue.toLowerCase() + "%");
      } else if (organizationSearchQuery.getName().isExact()) {
        sql.append(" AND FAC.name = :nameFilter ");
        params.addValue("nameFilter", rawSearchValue);
      } else {
        sql.append(" AND LOWER(FAC.name) LIKE :nameFilter ");
        params.addValue("nameFilter", rawSearchValue.toLowerCase() + "%");
      }
    }

    addLastUpdatedFilter(sql, params, organizationSearchQuery);
  }

  private void addLastUpdatedFilter(
      StringBuilder sql, MapSqlParameterSource params, OrganizationSearchQuery organizationSearchQuery) {
    if (organizationSearchQuery.getLastUpdated() == null
        || organizationSearchQuery.getLastUpdated().getValue() == null) {
      return;
    }

    LocalDateTime lastUpdated = organizationSearchQuery.getLastUpdated().getValue();

    if (organizationSearchQuery.getLastUpdated().getPrefix() == null) {
      sql.append(" AND FAC.last_updated = :lastUpdated");
      params.addValue("lastUpdated", lastUpdated);
      return;
    }

    switch (organizationSearchQuery.getLastUpdated().getPrefix()) {
      case GREATERTHAN:
        sql.append(" AND FAC.last_updated > :lastUpdated");
        break;
      case GREATERTHAN_OR_EQUALS:
        sql.append(" AND FAC.last_updated >= :lastUpdated");
        break;
      case LESSTHAN:
      case ENDS_BEFORE:
        sql.append(" AND FAC.last_updated < :lastUpdated");
        break;
      case LESSTHAN_OR_EQUALS:
        sql.append(" AND FAC.last_updated <= :lastUpdated");
        break;
      case NOT_EQUAL:
        sql.append(" AND FAC.last_updated <> :lastUpdated");
        break;
      case STARTS_AFTER:
        sql.append(" AND FAC.last_updated > :lastUpdated");
        break;
      case EQUAL:
      case APPROXIMATE:
      default:
        sql.append(" AND FAC.last_updated = :lastUpdated");
        break;
    }
    params.addValue("lastUpdated", lastUpdated);
  }

  private RowMapper<FacilityDBRecord> facilityDBRecordRowMapper() {
    return (rs, rowNum) ->
        FacilityDBRecord.builder()
            .id(rs.getLong("id"))
            .uuid(toUuid(rs.getBytes("uuid")))
            .name(rs.getString("name"))
            .phone(rs.getString("phone"))
            .fax(rs.getString("fax"))
            .street(rs.getString("street"))
            .city(rs.getString("city"))
            .state(rs.getString("state"))
            .postalCode(rs.getString("postal_code"))
            .countryCode(rs.getString("country_code"))
            .federalEin(rs.getString("federal_ein"))
            .website(rs.getString("website"))
            .email(rs.getString("email"))
            .serviceLocation(
                rs.getObject("service_location") != null ? rs.getInt("service_location") : null)
            .billingLocation(
                rs.getObject("billing_location") != null ? rs.getInt("billing_location") : null)
            .acceptsAssignment(
                rs.getObject("accepts_assignment") != null ? rs.getInt("accepts_assignment") : null)
            .posCode(rs.getString("pos_code"))
            .x12SenderId(rs.getString("x12_sender_id"))
            .attn(rs.getString("attn"))
            .domainIdentifier(rs.getString("domain_identifier"))
            .facilityNpi(rs.getString("facility_npi"))
            .facilityTaxonomy(rs.getString("facility_taxonomy"))
            .taxIdType(rs.getString("tax_id_type"))
            .color(rs.getString("color"))
            .primaryBusinessEntity(
                rs.getObject("primary_business_entity") != null
                    ? rs.getInt("primary_business_entity")
                    : null)
            .facilityCode(rs.getString("facility_code"))
            .extraValidation(rs.getString("extra_validation"))
            .mailStreet(rs.getString("mail_street"))
            .mailStreet2(rs.getString("mail_street2"))
            .mailCity(rs.getString("mail_city"))
            .mailState(rs.getString("mail_state"))
            .mailZip(rs.getString("mail_zip"))
            .oid(rs.getString("oid"))
            .iban(rs.getString("iban"))
            .info(rs.getString("info"))
            .inactive(rs.getObject("inactive") != null ? rs.getInt("inactive") : null)
            .lastUpdated(toLocalDateTime(rs.getTimestamp("last_updated")))
            .build();
  }
}
