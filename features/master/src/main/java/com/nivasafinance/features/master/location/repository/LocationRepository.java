package com.nivasafinance.features.master.location.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.master.location.dto.LocationDisplayNames;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LocationRepository {

    private static final String FIND_DISPLAY_NAMES_BY_CODES = """
            SELECT c.name AS country_name,
                   s.name AS state_name,
                   r.name AS region_name,
                   d.value AS district_value,
                   t.value AS taluka_value,
                   v.value AS village_value
            FROM (SELECT 1) x
            LEFT JOIN n_master_country c   ON c.code = :countryCode   AND c.is_active = true
            LEFT JOIN n_master_state s     ON s.code = :stateCode     AND s.country_id = c.id  AND s.is_active = true
            LEFT JOIN n_master_region r     ON r.code = :regionCode    AND r.state_id = s.id    AND r.is_active = true
            LEFT JOIN n_master_district d  ON d.code = :districtCode  AND d.state_id = s.id    AND d.is_active = true
            LEFT JOIN n_master_taluka t    ON t.code = :talukaCode    AND t.district_id = d.id AND t.is_active = true
            LEFT JOIN n_master_village v   ON v.code = :villageCode   AND v.taluka_id = t.id   AND v.is_active = true
            """;

    private static final RowMapper<LocationDisplayNames> ROW_MAPPER = new RowMapper<>() {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        @Override
        public LocationDisplayNames mapRow(ResultSet rs, int rowNum) throws SQLException {
            return LocationDisplayNames.builder()
                    .countryName(rs.getString("country_name"))
                    .stateName(rs.getString("state_name"))
                    .regionValue(parseJsonbToMasterLanguageData(rs, "region_name"))
                    .districtValue(parseJsonbToMasterLanguageData(rs, "district_value"))
                    .talukaValue(parseJsonbToMasterLanguageData(rs, "taluka_value"))
                    .villageValue(parseJsonbToMasterLanguageData(rs, "village_value"))
                    .build();
        }

        private MasterLanguageData parseJsonbToMasterLanguageData(ResultSet rs, String column) throws SQLException {
            String json = rs.getString(column);
            if (json == null || json.isBlank()) {
                return null;
            }
            try {
                Map<String, String> map = OBJECT_MAPPER.readValue(json, new TypeReference<>() { });
                return MasterLanguageData.fromMap(map);
            } catch (Exception e) {
                return null;
            }
        }
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Optional<LocationDisplayNames> findDisplayNamesByCodes(
            String countryCode, String stateCode, String regionCode, String districtCode, String talukaCode, String villageCode) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("countryCode", countryCode)
                .addValue("stateCode", stateCode)
                .addValue("regionCode", regionCode)
                .addValue("districtCode", districtCode)
                .addValue("talukaCode", talukaCode)
                .addValue("villageCode", villageCode);
        var list = jdbcTemplate.query(FIND_DISPLAY_NAMES_BY_CODES, params, ROW_MAPPER);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
