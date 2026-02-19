package com.nivasafinance.features.lead.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.referral.enums.EntityType;

@Component
public class LeadBasicResponseMapper implements RowMapper<LeadBasicResponse> {

    @Override
    public LeadBasicResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        String statusStr = rs.getString("status");
        String substatusStr = rs.getString("substatus");
        java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");

        LeadBasicResponse.LeadBasicResponseBuilder builder = LeadBasicResponse.builder();
        builder.id(rs.getLong("id"));
        builder.leadIdentifier(UUID.fromString(rs.getString("lead_identifier")));
        builder.primaryContactName(rs.getString("primary_contact_name"));
        builder.primaryContactPhone(rs.getString("primary_contact_phone"));
        builder.requestedAmount(rs.getBigDecimal("requested_amount"));
        builder.currentStage(rs.getString("current_stage"));
        builder.status(statusStr != null ? LeadStatus.valueOf(statusStr) : null);
        builder.substatus(substatusStr != null ? LeadSubStatus.valueOf(substatusStr) : null);
        builder.createdAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : null);
        builder.office(rs.getString("office"));
        builder.productCode(rs.getString("product_code"));
        builder.referredByCode(rs.getString("referred_by_code"));
        String referredByTypeStr = rs.getString("referred_by_type");
        if (referredByTypeStr != null) {
            try {
                builder.referredByType(EntityType.valueOf(referredByTypeStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
        String referredByIdentifierStr = rs.getString("referred_by_identifier");
        if (referredByIdentifierStr != null) {
            try {
                builder.referredByIdentifier(UUID.fromString(referredByIdentifierStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
        builder.referredByName(rs.getString("referred_by_name"));
        builder.referredByNumber(rs.getString("referred_by_number"));
        return builder.build();
    }

}
