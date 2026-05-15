package com.nivasafinance.features.leadotp.repository;

import com.nivasafinance.features.otp.core.enums.OtpStatus;
import com.nivasafinance.features.leadotp.entity.LeadOneTimeToken;
import com.nivasafinance.features.leadotp.exception.LeadOtpExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeadOneTimeTokenRepositoryWrapper {

    private final LeadOneTimeTokenRepository repository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public LeadOneTimeToken saveWithException(LeadOneTimeToken token) {
        try {
            return repository.saveAndFlush(token);
        } catch (DataAccessException e) {
            throw LeadOtpExceptionFactory.saveTrackingFailed(e);
        }
    }

    public void invalidateActiveTokens(String reference, Long leadId, Long contactId) {
        String sql = """
                UPDATE n_lead_one_time_token
                SET status = :newStatus,
                    updated_at = CURRENT_TIMESTAMP
                WHERE "reference" = :reference
                  AND lead_id = :leadId
                  AND contact_id = :contactId
                  AND status IN (:currentStatuses)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reference", reference)
                .addValue("leadId", leadId)
                .addValue("contactId", contactId)
                .addValue("newStatus", OtpStatus.INVALIDATED.name())
                .addValue("currentStatuses", List.of(OtpStatus.SENT.name()));
        try {
            namedParameterJdbcTemplate.update(sql, params);
        } catch (DataAccessException e) {
            throw LeadOtpExceptionFactory.updateTrackingFailed(e);
        }
    }

    public Optional<LeadOneTimeToken> findByLeadContactAndTokenId(
            String reference,
            Long leadId,
            Long contactId,
            Long oneTimeTokenId) {
        String sql = """
                SELECT id,
                       lead_id,
                       contact_id,
                       ott_id,
                       "reference",
                       status
                FROM n_lead_one_time_token
                WHERE "reference" = :reference
                  AND lead_id = :leadId
                  AND contact_id = :contactId
                  AND ott_id = :oneTimeTokenId
                  AND status = :status
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reference", reference)
                .addValue("leadId", leadId)
                .addValue("contactId", contactId)
                .addValue("oneTimeTokenId", oneTimeTokenId)
                .addValue("status", OtpStatus.SENT.name());
        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(LeadOneTimeToken.builder()
                        .id(rs.getLong("id"))
                        .leadId(rs.getLong("lead_id"))
                        .contactId(rs.getLong("contact_id"))
                        .ottId(rs.getLong("ott_id"))
                        .reference(rs.getString("reference"))
                        .status(OtpStatus.valueOf(rs.getString("status")))
                        .build());
            });
        } catch (DataAccessException e) {
            throw LeadOtpExceptionFactory.retrieveTrackingFailed(e);
        }
    }

    public Optional<LeadOneTimeToken> findByReferenceAndTokenId(String reference, Long oneTimeTokenId) {
        String sql = """
                SELECT id,
                       lead_id,
                       contact_id,
                       ott_id,
                       "reference",
                       status
                FROM n_lead_one_time_token
                WHERE "reference" = :reference
                  AND ott_id = :oneTimeTokenId
                  AND status = :status
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reference", reference)
                .addValue("oneTimeTokenId", oneTimeTokenId)
                .addValue("status", OtpStatus.SENT.name());
        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(LeadOneTimeToken.builder()
                        .id(rs.getLong("id"))
                        .leadId((Long) rs.getObject("lead_id"))
                        .contactId((Long) rs.getObject("contact_id"))
                        .ottId(rs.getLong("ott_id"))
                        .reference(rs.getString("reference"))
                        .status(OtpStatus.valueOf(rs.getString("status")))
                        .build());
            });
        } catch (DataAccessException e) {
            throw LeadOtpExceptionFactory.retrieveTrackingFailed(e);
        }
    }

    public void updateStatus(Long trackingId, OtpStatus status) {
        String sql = """
                UPDATE n_lead_one_time_token
                SET status = :status,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", trackingId)
                .addValue("status", status.name());
        try {
            namedParameterJdbcTemplate.update(sql, params);
        } catch (DataAccessException e) {
            throw LeadOtpExceptionFactory.updateTrackingFailed(e);
        }
    }

    public void updateLeadId(Long trackingId, Long leadId) {
        String sql = """
                UPDATE n_lead_one_time_token
                SET lead_id = :leadId,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", trackingId)
                .addValue("leadId", leadId);
        try {
            namedParameterJdbcTemplate.update(sql, params);
        } catch (DataAccessException e) {
            throw LeadOtpExceptionFactory.updateTrackingFailed(e);
        }
    }
}
