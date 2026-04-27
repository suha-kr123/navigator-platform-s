package com.nivasafinance.features.transaction.repository.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@Component
public class LeadTransactionRowMapper implements RowMapper<LeadTransactionResponse> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public LeadTransactionResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        LeadTransactionResponse.LeadTransactionResponseBuilder builder = LeadTransactionResponse.builder();

        builder.identifier(UUID.fromString(rs.getString("identifier")));
        builder.status(rs.getString("status"));
        builder.amount(rs.getBigDecimal("amount"));
        builder.createdBy(rs.getString("created_by"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            builder.createdAt(createdAt.toLocalDateTime());
        }

        builder.domainType(rs.getString("domain_type"));

        String leadIdStr = rs.getString("lead_identifier");
        if (leadIdStr != null) {
            builder.leadIdentifier(UUID.fromString(leadIdStr));
        }

        builder.referralCode(rs.getString("referral_code"));

        builder.payeeType(rs.getString("payee_type"));
        String payeeIdStr = rs.getString("payee_identifier");
        if (payeeIdStr != null) {
            builder.payeeIdentifier(UUID.fromString(payeeIdStr));
        }

        builder.latestEventType(rs.getString("latest_event_type"));
        builder.latestActor(rs.getString("latest_actor"));

        Timestamp latestEventAt = rs.getTimestamp("latest_event_at");
        if (latestEventAt != null) {
            builder.latestEventAt(latestEventAt.toLocalDateTime());
        }

        builder.remarks(rs.getString("remarks"));
        builder.paymentMode(rs.getString("payment_mode"));
        builder.externalReference(rs.getString("external_reference"));
        builder.paymentStatus(rs.getString("payment_status"));

        String paymentDataJson = rs.getString("payment_data");
        if (paymentDataJson != null) {
            try {
                builder.paymentData(OBJECT_MAPPER.readValue(paymentDataJson, new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                builder.paymentData(null);
            }
        }

        return builder.build();
    }
}
