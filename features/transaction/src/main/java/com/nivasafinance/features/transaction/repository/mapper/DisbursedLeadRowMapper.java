package com.nivasafinance.features.transaction.repository.mapper;

import com.nivasafinance.features.transaction.dto.DisbursedLeadResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

@Component
public class DisbursedLeadRowMapper implements RowMapper<DisbursedLeadResponse> {

    @Override
    public DisbursedLeadResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        DisbursedLeadResponse.DisbursedLeadResponseBuilder builder = DisbursedLeadResponse.builder();

        builder.leadIdentifier(UUID.fromString(rs.getString("lead_identifier")));
        builder.applicantName(rs.getString("applicant_name"));
        builder.productCode(rs.getString("product_code"));
        builder.disbursedAmount(rs.getBigDecimal("disbursed_amount"));

        java.sql.Date disbursedDate = rs.getDate("disbursed_date");
        if (disbursedDate != null) {
            builder.disbursedDate(disbursedDate.toLocalDate());
        }

        builder.officeKey(rs.getString("office_key"));
        builder.referralCode(rs.getString("referral_code"));
        builder.payeeType(rs.getString("payee_type"));
        builder.payeeName(rs.getString("payee_name"));

        String payeeIdStr = rs.getString("payee_identifier");
        if (payeeIdStr != null) {
            builder.payeeIdentifier(UUID.fromString(payeeIdStr));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            builder.createdAt(createdAt.toLocalDateTime());
        }

        return builder.build();
    }
}
