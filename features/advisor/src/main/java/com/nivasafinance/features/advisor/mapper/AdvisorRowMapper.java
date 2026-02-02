package com.nivasafinance.features.advisor.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.nivasafinance.features.advisor.dto.AdvisorBasicResponse;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;

@Component
public class AdvisorRowMapper implements RowMapper<AdvisorBasicResponse> {

    @Override
    public AdvisorBasicResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        AdvisorBasicResponse.AdvisorBasicResponseBuilder builder =
                AdvisorBasicResponse.builder();
        builder.advisorIdentifier(UUID.fromString(rs.getString("advisor_identifier")));
        builder.name(rs.getString("person_name"));
        builder.mobileNumber(rs.getString("mobile_number"));
        builder.officeKey(rs.getString("office_key"));
        builder.status(AdvisorStatus.valueOf(rs.getString("status")));
        builder.createdAt(rs.getTimestamp("created_at").toLocalDateTime());
        builder.updatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        builder.owner(rs.getString("owner"));
        return builder.build();
    }
}

