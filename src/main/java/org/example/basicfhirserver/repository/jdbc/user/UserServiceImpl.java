package org.example.basicfhirserver.repository.jdbc.user;

import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toBytes;
import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toUuid;

@Service
public class UserServiceImpl implements UserService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserServiceImpl(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<UserDBRecord> findById(UUID uuid) {
        StringBuilder sql = userQuery();

        sql.append(" AND users.uuid = :uuid");
        byte[] binaryUuid = toBytes(uuid);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uuid", binaryUuid);

        return namedParameterJdbcTemplate.query(sql.toString(),
                parameters,
                userRowMapper());
    }

    @Override
    public List<UserDBRecord> find(PractitionerSearchQuery practitionerSearchQuery) {

        StringBuilder sql = userQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();
        addFilter(sql, params, practitionerSearchQuery);

        return namedParameterJdbcTemplate.query(sql.toString(),
                params,
                userRowMapper());
    }

    private StringBuilder userQuery() {
        return new StringBuilder("""
                SELECT\s
                    `users`.`id`,
                    `users`.`username`,
                    `users`.`password`,
                    `users`.`authorized`,
                    `users`.`info`,
                    `users`.`source`,
                    `users`.`fname`,
                    `users`.`mname`,
                    `users`.`lname`,
                    `users`.`federaltaxid`,
                    `users`.`federaldrugid`,
                    `users`.`upin`,
                    `users`.`facility`,
                    `users`.`facility_id`,
                    `users`.`see_auth`,
                    `users`.`active`,
                    `users`.`npi`,
                    `users`.`title`,
                    `users`.`specialty`,
                    `users`.`billname`,
                    `users`.`email`,
                    `users`.`url`,
                    `users`.`assistant`,
                    `users`.`organization`,
                    `users`.`valedictory`,
                    `users`.`street`,
                    `users`.`streetb`,
                    `users`.`city`,
                    `users`.`state`,
                    `users`.`zip`,
                    `users`.`street2`,
                    `users`.`streetb2`,
                    `users`.`city2`,
                    `users`.`state2`,
                    `users`.`zip2`,
                    `users`.`phone`,
                    `users`.`fax`,
                    `users`.`phonew1`,
                    `users`.`phonew2`,
                    `users`.`phonecell`,
                    `users`.`notes`,
                    `users`.`cal_ui`,
                    `users`.`taxonomy`,
                    `users`.`calendar`,
                    `users`.`abook_type`,
                    `users`.`default_warehouse`,
                    `users`.`irnpool`,
                    `users`.`state_license_number`,
                    `users`.`newcrop_user_role`,
                    `users`.`email_direct`,
                    `users`.`physician_type`,
                    `users`.`cpoe`,
                    `users`.`suffix`,
                    `users`.`main_menu_role`,
                    `users`.`weno_prov_id`,
                    `users`.`patient_menu_role`,
                    `users`.`portal_user`,
                    `users`.`supervisor_id`,
                    `users`.`uuid`,
                    `users`.`google_signin_email`,
                    `users`.`billing_facility`,
                    `users`.`billing_facility_id`,
                    `users`.`date_created`,
                    `users`.`last_updated`,
                    `users`.`country_code`,
                    `users`.`country_code2`,
                    `abook`.`title` AS abook_title,
                    `physician`.`title` AS physician_title,
                    `physician`.`codes` AS physician_code\s
                FROM `users`
                LEFT JOIN `list_options` AS `abook`\s
                    ON `users`.`abook_type` = `abook`.`option_id`\s
                LEFT JOIN `list_options` AS `physician`\s
                    ON `users`.`physician_type` = `physician`.`option_id`
                WHERE 1=1
                """
        );
    }

    private void addFilter(
            StringBuilder sql,
            MapSqlParameterSource params,
            PractitionerSearchQuery practitionerSearchQuery
    ) {

        if (practitionerSearchQuery.getName() != null) {
            String rawSearchValue = practitionerSearchQuery.getName().getValue();
            if (practitionerSearchQuery.getName().isContains()) {
                sql.append("""
                        AND (LOWER(users.lname) LIKE :nameFilter OR LOWER(users.fname) LIKE :nameFilter)
                        """);
                params.addValue("nameFilter", "%" + rawSearchValue.toLowerCase() + "%");
            } else if (practitionerSearchQuery.getName().isExact()) {
                sql.append("""
                        AND (users.lname = :nameFilter OR users.fname = :nameFilter)
                        """);
                params.addValue("nameFilter", rawSearchValue);
            } else {
                sql.append("""
                        AND (LOWER(users.lname) LIKE :nameFilter OR LOWER(users.fname) LIKE :nameFilter)
                        """);
                params.addValue("nameFilter", rawSearchValue.toLowerCase() + "%");
            }
        }

        if (practitionerSearchQuery.getIdentifier() != null) {
            String rawSearchValue = practitionerSearchQuery.getIdentifier();
            sql.append("""
                    AND (users.npi = :npi)
                    """);
            params.addValue("npi", rawSearchValue);
        }

    }


    private RowMapper<UserDBRecord> userRowMapper() {
        return (rs, rowNum) -> UserDBRecord.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .authorized(rs.getObject("authorized") != null ? rs.getInt("authorized") : null)
                .info(rs.getString("info"))
                .source(rs.getString("source"))
                .fname(rs.getString("fname"))
                .mname(rs.getString("mname"))
                .lname(rs.getString("lname"))
                .federaltaxid(rs.getString("federaltaxid"))
                .federaldrugid(rs.getString("federaldrugid"))
                .upin(rs.getString("upin"))
                .facility(rs.getString("facility")) // Correct
                .facilityId(rs.getInt("facility_id")) // Correct
                .seeAuth(rs.getObject("see_auth") != null ? rs.getInt("see_auth") : null)
                .active(rs.getObject("active") != null ? rs.getInt("active") : null)
                .npi(rs.getString("npi"))
                .title(rs.getString("title"))
                .specialty(rs.getString("specialty"))
                .billname(rs.getString("billname"))
                .email(rs.getString("email"))
                .url(rs.getString("url"))
                .assistant(rs.getString("assistant"))
                .organization(rs.getString("organization"))
                .valedictory(rs.getString("valedictory"))
                .street(rs.getString("street"))
                .streetb(rs.getString("streetb"))
                .city(rs.getString("city"))
                .state(rs.getString("state"))
                .zip(rs.getString("zip"))
                .street2(rs.getString("street2"))
                .streetb2(rs.getString("streetb2"))
                .city2(rs.getString("city2"))
                .state2(rs.getString("state2"))
                .zip2(rs.getString("zip2"))
                .phone(rs.getString("phone"))
                .fax(rs.getString("fax"))
                .phonew1(rs.getString("phonew1"))
                .phonew2(rs.getString("phonew2"))
                .phonecell(rs.getString("phonecell"))
                .notes(rs.getString("notes"))
                .calUi(rs.getString("cal_ui"))
                .taxonomy(rs.getString("taxonomy"))
                .calendar(rs.getObject("calendar") != null ? rs.getInt("calendar") : null)
                .abookType(rs.getString("abook_type"))
                .defaultWarehouse(rs.getString("default_warehouse"))
                .irnpool(rs.getString("irnpool"))
                .stateLicenseNumber(rs.getString("state_license_number"))
                .newcropUserRole(rs.getString("newcrop_user_role"))
                .emailDirect(rs.getString("email_direct"))
                .physicianType(rs.getString("physician_type"))
                .cpoe(rs.getObject("cpoe") != null ? rs.getInt("cpoe") : null)
                .suffix(rs.getString("suffix"))
                .mainMenuRole(rs.getString("main_menu_role"))
                .wenoProvId(rs.getString("weno_prov_id"))
                .patientMenuRole(rs.getString("patient_menu_role"))
                .portalUser(rs.getObject("portal_user") != null ? rs.getInt("portal_user") : null)
                .supervisorId(rs.getObject("supervisor_id") != null ? rs.getLong("supervisor_id") : null)
                .uuid(toUuid(rs.getBytes("uuid")))
                .googleSigninEmail(rs.getString("google_signin_email"))
                .billingFacility(rs.getString("billing_facility"))
                .billingFacilityId(rs.getObject("billing_facility_id") != null ? rs.getInt("billing_facility_id") : null)
                .dateCreated(getInstant(rs, "date_created"))
                .lastUpdated(getInstant(rs, "last_updated"))
                .countryCode(rs.getString("country_code"))
                .countryCode2(rs.getString("country_code2"))
                .abookTitle(rs.getString("abook_title"))
                .physicianTitle(rs.getString("physician_title"))
                .physicianCode(rs.getString("physician_code"))
                .build();
    }

    private Instant getInstant(ResultSet rs, String columnName) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toInstant() : null;
    }


//    private RowMapper<UserDBRecord> practitionerRowMapper() {
//        return (rs, rowNum) -> UserDBRecord.builder()
//                .id(rs.getLong("id"))
//                .uuid(rs.getBytes("uuid"))
//                .userName(rs.getString("user_name"))
//                .providerId(rs.getLong("provider_id"))
//                .providerUuid(rs.getBytes("provider_uuid"))
//                .providerLastUpdated(getInstant(rs, "provider_last_updated"))
//                .locationUuid(rs.getBytes("location_uuid"))
//                .workPhone(rs.getString("work_phone"))
//                .workPhoneUse(rs.getString("work_phone_use"))
//                .workPhoneSystem(rs.getString("work_phone_system"))
//                .fax(rs.getString("fax"))
//                .faxUse(rs.getString("fax_use"))
//                .faxSystem(rs.getString("fax_system"))
//                .email(rs.getString("email"))
//                .emailUse(rs.getString("email_use"))
//                .emailSystem(rs.getString("email_system"))
//                .url(rs.getString("url"))
//                .urlUse(rs.getString("url_use"))
//                .urlSystem(rs.getString("url_system"))
//                .facilityUuid(rs.getBytes("facility_uuid"))
//                .facilityName(rs.getString("facility_name"))
//                .roleCode(rs.getString("role_code"))
//                .roleTitle(rs.getString("role_title"))
//                .roleLastUpdated(getInstant(rs, "role_last_updated"))
//                .specialtyCode(rs.getString("specialty_code"))
//                .specialtyTitle(rs.getString("specialty_title"))
//                .specialtyLastUpdated(getInstant(rs, "specialty_last_updated"))
//                .physicianTypeCodes(rs.getString("physician_type_codes"))
//                .physicianType(rs.getString("physician_type"))
//                .physicianTypeTitle(rs.getString("physician_type_title"))
//                .build();
//    }

//    private Instant getInstant(ResultSet rs, String columnName) throws SQLException {
//        java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
//        return timestamp != null ? timestamp.toInstant() : null;
//    }

}
