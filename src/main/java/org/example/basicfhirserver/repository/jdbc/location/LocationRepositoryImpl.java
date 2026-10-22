package org.example.basicfhirserver.repository.jdbc.location;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.resources.location.LocationSearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LocationRepositoryImpl implements LocationRepository {

  private static final int DEFAULT_PAGE_SIZE = 5;
  private static final int DEFAULT_PAGE_OFFSET = 0;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public LocationRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public List<LocationDBRecord> findById(UUID uuid) {
    StringBuilder sql = locationQuery();

    sql.append(" AND uuid_mapping.uuid = :uuid");

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("uuid", toBytes(uuid));

    return namedParameterJdbcTemplate.query(
        sql.toString(), parameters, locationDBRecordRowMapper());
  }

  @Override
  public Page<LocationDBRecord> find(LocationSearchQuery locationSearchQuery) {
    StringBuilder sql = locationQuery();
    MapSqlParameterSource params = new MapSqlParameterSource();
    addFilter(sql, params, locationSearchQuery);

    long total = countTotal(sql, params);

    int offset =
        locationSearchQuery.getOffset() != null
            ? locationSearchQuery.getOffset()
            : DEFAULT_PAGE_OFFSET;
    int limit =
        locationSearchQuery.getCount() != null ? locationSearchQuery.getCount() : DEFAULT_PAGE_SIZE;

    sql.append(" ORDER BY location.last_updated DESC limit :limit offset :offset ");
    params.addValue("limit", limit);
    params.addValue("offset", offset);

    List<LocationDBRecord> records =
        namedParameterJdbcTemplate.query(sql.toString(), params, locationDBRecordRowMapper());

    return new PageImpl<>(records, PageRequest.of(offset / limit, limit), total);
  }

  private long countTotal(StringBuilder filteredSql, MapSqlParameterSource params) {
    String countSql = "SELECT COUNT(*) from ( %s ) as query_count";
    Long total =
        namedParameterJdbcTemplate.queryForObject(
            countSql.formatted(filteredSql), params, Long.class);
    return total != null ? total : 0L;
  }

  private StringBuilder locationQuery() {
    return new StringBuilder(
        """
                SELECT location.*,
                       uuid_mapping.uuid
                FROM (
                        SELECT uuid AS table_uuid,
                               'Home Address' AS name,
                               '' AS identifier,
                               'none' AS identifier_type,
                               street,
                               city,
                               postal_code,
                               state,
                               country_code,
                               phone_cell AS phone,
                               NULL AS fax,
                               NULL AS website,
                               email,
                               `date` AS last_updated,
                               'PATIENT' AS `type`,
                               '' AS location_role_type
                        FROM patient_data
                        UNION
                        SELECT uuid AS table_uuid,
                               name,
                               facility_npi AS identifier,
                               'npi' AS identifier_type,
                               street,
                               city,
                               postal_code,
                               state,
                               country_code,
                               phone,
                               fax,
                               website,
                               email,
                               last_updated,
                               'FACILITY' AS `type`,
                               pos_code AS location_role_type
                        FROM facility
                        UNION
                        SELECT uuid AS table_uuid,
                               'Home Address' AS name,
                               '' AS identifier,
                               'none' AS identifier_type,
                               street,
                               city,
                               zip AS postal_code,
                               state,
                               country_code,
                               phone,
                               fax,
                               url AS website,
                               email,
                               last_updated,
                               'USER' AS `type`,
                               '' AS location_role_type
                        FROM users
                ) location
                LEFT JOIN uuid_mapping
                       ON uuid_mapping.target_uuid = location.table_uuid
                      AND uuid_mapping.resource = 'Location'
                WHERE 1=1
                """);
  }

  private void addFilter(
      StringBuilder sql, MapSqlParameterSource params, LocationSearchQuery locationSearchQuery) {
    if (locationSearchQuery.getId() != null) {
      byte[] binaryUuid = toBytes(UUID.fromString(locationSearchQuery.getId()));
      sql.append(" AND uuid_mapping.uuid = :locationUuid ");
      params.addValue("locationUuid", binaryUuid);
    }

    addStringFilter(sql, params, "location.name", locationSearchQuery.getName(), "nameFilter");
    addAddressFilter(sql, params, locationSearchQuery.getAddress());
    addStringFilter(
        sql, params, "location.city", locationSearchQuery.getAddressCity(), "addressCityFilter");
    addStringFilter(
        sql, params, "location.state", locationSearchQuery.getAddressState(), "addressStateFilter");
    addStringFilter(
        sql,
        params,
        "location.postal_code",
        locationSearchQuery.getAddressPostalCode(),
        "addressPostalCodeFilter");

    addLastUpdatedFilter(sql, params, locationSearchQuery);
  }

  private void addStringFilter(
      StringBuilder sql,
      MapSqlParameterSource params,
      String column,
      SearchValue<String> searchValue,
      String parameterName) {
    if (searchValue == null || searchValue.getValue() == null) {
      return;
    }
    String value = searchValue.getValue();
    if (searchValue.isExact()) {
      sql.append(" AND ").append(column).append(" = :").append(parameterName);
      params.addValue(parameterName, value);
    } else if (searchValue.isContains()) {
      sql.append(" AND LOWER(").append(column).append(") LIKE :").append(parameterName);
      params.addValue(parameterName, "%" + value.toLowerCase() + "%");
    } else {
      sql.append(" AND LOWER(").append(column).append(") LIKE :").append(parameterName);
      params.addValue(parameterName, value.toLowerCase() + "%");
    }
  }

  private void addAddressFilter(
      StringBuilder sql, MapSqlParameterSource params, SearchValue<String> address) {
    if (address == null || address.getValue() == null) {
      return;
    }
    String value = address.getValue();
    if (address.isExact()) {
      sql.append(
          " AND (location.street = :addressFilter OR location.city = :addressFilter"
              + " OR location.state = :addressFilter OR location.postal_code = :addressFilter) ");
      params.addValue("addressFilter", value);
      return;
    }
    String likeValue =
        address.isContains() ? "%" + value.toLowerCase() + "%" : value.toLowerCase() + "%";
    sql.append(
        " AND (LOWER(location.street) LIKE :addressFilter OR LOWER(location.city) LIKE"
            + " :addressFilter OR LOWER(location.state) LIKE :addressFilter OR"
            + " LOWER(location.postal_code) LIKE :addressFilter) ");
    params.addValue("addressFilter", likeValue);
  }

  private void addLastUpdatedFilter(
      StringBuilder sql, MapSqlParameterSource params, LocationSearchQuery locationSearchQuery) {
    if (locationSearchQuery.getLastUpdated() == null
        || locationSearchQuery.getLastUpdated().getValue() == null) {
      return;
    }

    LocalDateTime lastUpdated = locationSearchQuery.getLastUpdated().getValue();

    if (locationSearchQuery.getLastUpdated().getPrefix() == null) {
      sql.append(" AND location.last_updated = :lastUpdated");
      params.addValue("lastUpdated", lastUpdated);
      return;
    }

    switch (locationSearchQuery.getLastUpdated().getPrefix()) {
      case GREATERTHAN:
        sql.append(" AND location.last_updated > :lastUpdated");
        break;
      case GREATERTHAN_OR_EQUALS:
        sql.append(" AND location.last_updated >= :lastUpdated");
        break;
      case LESSTHAN:
      case ENDS_BEFORE:
        sql.append(" AND location.last_updated < :lastUpdated");
        break;
      case LESSTHAN_OR_EQUALS:
        sql.append(" AND location.last_updated <= :lastUpdated");
        break;
      case NOT_EQUAL:
        sql.append(" AND location.last_updated <> :lastUpdated");
        break;
      case STARTS_AFTER:
        sql.append(" AND location.last_updated > :lastUpdated");
        break;
      case EQUAL:
      case APPROXIMATE:
      default:
        sql.append(" AND location.last_updated = :lastUpdated");
        break;
    }
    params.addValue("lastUpdated", lastUpdated);
  }

  private RowMapper<LocationDBRecord> locationDBRecordRowMapper() {
    return (rs, rowNum) ->
        LocationDBRecord.builder()
            .tableUuid(toUuid(rs.getBytes("table_uuid")))
            .name(rs.getString("name"))
            .identifier(rs.getString("identifier"))
            .identifierType(rs.getString("identifier_type"))
            .street(rs.getString("street"))
            .city(rs.getString("city"))
            .postalCode(rs.getString("postal_code"))
            .state(rs.getString("state"))
            .countryCode(rs.getString("country_code"))
            .phone(rs.getString("phone"))
            .fax(rs.getString("fax"))
            .website(rs.getString("website"))
            .email(rs.getString("email"))
            .lastUpdated(toLocalDateTime(rs.getTimestamp("last_updated")))
            .type(rs.getString("type"))
            .locationRoleType(rs.getString("location_role_type"))
            .fhirLocationUuid(toUuid(rs.getBytes("uuid")))
            .build();
  }
}
