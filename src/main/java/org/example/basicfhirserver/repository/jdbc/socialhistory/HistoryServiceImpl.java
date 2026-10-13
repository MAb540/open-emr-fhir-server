package org.example.basicfhirserver.repository.jdbc.socialhistory;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toLocalDateTime;
import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toUuid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HistoryServiceImpl implements HistoryService {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  // Local Class Constants
  public static final int COLUMN_TOBACCO_INDEX_SMOKING_STATUS = 3;
  public static final int COLUMN_TOBACCO_INDEX_SMOKING_PACK_COUNT = 4;
  public static final String COLUMN_SMOKING_PACKS_PER_DAY = "packs_per_day";
  public static final String COLUMN_SMOKING_STATUS_CODES = "smoking_status_codes";

  // Code Types Constants for local system lookups
  private static final String CODE_TYPE_SNOMED_CT = "SNOMED-CT";
  private static final String CODE_TYPE_SNOMED = "SNOMED";
  private static final String CODE_TYPE_LOINC = "LOINC";
  private static final String CODE_TYPE_RXNORM = "RXNORM";
  private static final String CODE_TYPE_ICD10 = "ICD10";

  public HistoryServiceImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Override
  public List<HistoryDBRecord> findHistoryById(UUID uuid) {
    return List.of();
  }

  @Override
  public List<HistoryDBRecord> findHistory(ObservationSearchQuery searchQuery) {
    StringBuilder sql = vitalsQuery();
    MapSqlParameterSource params = new MapSqlParameterSource();

    List<HistoryDBRecord> historyDBRecords = namedParameterJdbcTemplate.query(
            sql.toString(), params, historyObservationRowMapper());

    // Strict 1-to-1 loop mutation matching: historyDBRecords.forEach(r -> createResultRecordFromDatabaseResult(r));
    historyDBRecords.forEach(this::createResultRecordFromDatabaseResult);

    return historyDBRecords;
  }

  public Map<String, Object> getListOption(String listId, String optionId) {
    Map<String, Object> search = new HashMap<>();
    search.put("option_id", optionId);
    List<Map<String, Object>> records = getOptionsByListName(listId, search);
    if (!records.isEmpty()) { // should only be one record
      return records.get(0);
    }
    return null;
  }

  public List<Map<String, Object>> getOptionsByListName(String listName, Map<String, Object> search) {
    StringBuilder sql = new StringBuilder("SELECT * FROM list_options WHERE list_id = :listId ");
    MapSqlParameterSource params = new MapSqlParameterSource("listId", listName);

    List<String> whitelistedColumns = List.of(
            "option_id", "seq", "is_default", "option_value", "mapping", "notes", "codes", "activity", "edit_options", "toggle_setting_1", "toggle_setting_2", "subtype"
    );
    for (String column : whitelistedColumns) {
      if (search.get(column) != null && !search.get(column).toString().isEmpty()) {
        sql.append(" AND ").append(column).append(" = :").append(column).append(" ");
        params.addValue(column, search.get(column));
      }
    }
    sql.append(" ORDER BY `seq` ");

    return namedParameterJdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
      Map<String, Object> row = new HashMap<>();
      row.put("option_id", rs.getString("option_id"));
      row.put("codes", rs.getString("codes"));
      row.put("seq", rs.getInt("seq"));
      return row;
    });
  }

  protected void createResultRecordFromDatabaseResult(HistoryDBRecord record) {
      String tobaccoColumn = record.getTobacco() != null ? record.getTobacco() : "";
      String[] tobacco = tobaccoColumn.split("\\|", -1);

    Map<String, Object> listOption = null;

    if (tobacco.length > COLUMN_TOBACCO_INDEX_SMOKING_STATUS && !tobacco[COLUMN_TOBACCO_INDEX_SMOKING_STATUS].isEmpty()) {
      listOption = getListOption("smoking_status", tobacco[COLUMN_TOBACCO_INDEX_SMOKING_STATUS]);
      if (listOption == null) {
        listOption = new HashMap<>();
      }
      record.setSmokingStatusCodes(null);
      if (listOption.get("codes") != null && !listOption.get("codes").toString().isEmpty()) {
        record.setSmokingStatusCodes(this.addCoding(listOption.get("codes").toString()).toString());
      }
    }

    if (tobacco.length > COLUMN_TOBACCO_INDEX_SMOKING_PACK_COUNT && !tobacco[COLUMN_TOBACCO_INDEX_SMOKING_PACK_COUNT].isEmpty()) {
      record.setPacksPerDay(0); // Equiv to: $record[self::COLUMN_SMOKING_PACKS_PER_DAY] = 0;
      if (listOption != null && listOption.get("codes") != null && !listOption.get("codes").toString().isEmpty()) {
        try {
          record.setPacksPerDay(Integer.parseInt(tobacco[COLUMN_TOBACCO_INDEX_SMOKING_PACK_COUNT]));
        } catch (NumberFormatException e) {
          record.setPacksPerDay(0);
        }
      }
    }
  }

  protected Map<String, Map<String, String>> addCoding(String diagnosis) {
    if (diagnosis == null || diagnosis.isEmpty()) {
      return new HashMap<>();
    }
    String[] diags = diagnosis.split(";");
    Map<String, Map<String, String>> diagnosisMap = new HashMap<>();

    for (String diag : diags) {
      ParsedCodeResult parsedCode = this.parseCode(diag.trim());
      String codeType = parsedCode.codeType;
      String code = parsedCode.code;
      String system = this.getSystemForCodeType(codeType);
      String codec = this.lookupCodeDescription(diag.trim());

      Map<String, String> details = new HashMap<>();
      details.put("code", code);
      details.put("description", codec);
      details.put("code_type", codeType);
      details.put("system", system);

      diagnosisMap.put(code, details);
    }
    return diagnosisMap;
  }

  // =========================================================================
  // Clean 1-to-1 Local Implementations of CodeTypesService Methods
  // =========================================================================

  private static class ParsedCodeResult {
    final String code;
    final String codeType;
    ParsedCodeResult(String code, String codeType) { this.code = code; this.codeType = codeType; }
  }

  private ParsedCodeResult parseCode(String code) {
    String parsedCode = code;
    String parsedType = null;
    if (code != null && code.contains(":")) {
      String[] parts = code.split(":", 2);
      parsedType = parts[0];
      parsedCode = parts[1];
    }
    return new ParsedCodeResult(parsedCode, parsedType);
  }

  private String getSystemForCodeType(String codeType) {
    if (codeType == null) return null;
    switch (codeType) {
      case CODE_TYPE_SNOMED_CT:
      case CODE_TYPE_SNOMED:   return "http://snomed.info";
      case CODE_TYPE_LOINC:     return "http://loinc.org";
      case CODE_TYPE_RXNORM:    return "http://nih.gov";
      case CODE_TYPE_ICD10:     return "http://hl7.org";
      default:                  return null;
    }
  }

  private String lookupCodeDescription(String code) {
    // Strict 1-to-1 representation of your table text description lookup stub
    return "";
  }

  private StringBuilder vitalsQuery() {
    return new StringBuilder("""
                   SELECT
                   history.id
                  ,history.uuid
                  ,history.date
                  ,history.tobacco
                  ,history.alcohol
                  ,history.exercise_patterns
                  ,history.recreational_drugs
                  ,history.created_by
                  ,patients.pid
                  ,patients.puuid
                  ,provenance_created.provider_uuid
                  ,provenance_created.provider_npi
                  ,provenance_created.provider_username
              FROM
      history_data history
      JOIN
              (
                      SELECT
                              max(id) AS id
      FROM history_data
      GROUP BY pid
              ) latest_history_records ON history.id = latest_history_records.id
      LEFT JOIN
              (
                      SELECT
                              uuid AS puuid
                      ,pid
                              FROM patient_data
              ) patients ON history.pid = patients.pid
      LEFT JOIN (
              select
                      uuid AS provider_uuid
              ,npi AS provider_npi
              ,username AS provider_username
              ,id AS provider_id
                      FROM
                      users
      ) provenance_created ON history.created_by = provenance_created.provider_id
                      WHERE 1 = 1
      """);
  }

  private RowMapper<HistoryDBRecord> historyObservationRowMapper() {
    return (rs, rowNum) -> HistoryDBRecord.builder()
            .id(rs.getLong("id"))
            .uuid(toUuid(rs.getBytes("uuid")))
            .date(toLocalDateTime(rs.getTimestamp("date")))
            .tobacco(rs.getString("tobacco"))
            .alcohol(rs.getString("alcohol"))
            .exercisePatterns(rs.getString("exercise_patterns")).recreationalDrugs(rs.getString("recreational_drugs"))
            .createdBy(rs.getObject("created_by") != null ? rs.getLong("created_by") : null)
            .pid(rs.getObject("pid") != null ? rs.getLong("pid") : null)
            .puuid(toUuid(rs.getBytes("puuid")))
            .providerUuid(rs.getString("provider_uuid") != null ? UUID.fromString(rs.getString("provider_uuid")) : null)
            .providerNpi(rs.getString("provider_npi"))
            .providerUsername(rs.getString("provider_username"))
            .build();
  }
}
