package com.nivasafinance.features.leadactivity.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadactivity.dto.LeadActivityResponse;
import com.nivasafinance.features.leadactivity.enums.ResourceAction;
import com.nivasafinance.features.leadactivity.enums.ResourceEnum;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LeadActivityRepositoryWrapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT_QUERY = """
            SELECT
                la.identifier,
                la.resource_type,
                la.resource_action,
                la.resource_id,
                la.description,
                la.metadata,
                la.created_at,
                la.created_by,
                la.updated_at,
                la.updated_by,
                la.version
            FROM n_lead_activity la
            WHERE la.lead_id = ?
            """;

    private static final String BASE_COUNT_QUERY = "SELECT COUNT(*) FROM n_lead_activity WHERE lead_id = ?";

    public PaginatedResponse<LeadActivityResponse> findAllByLeadIdentifierWithException(
            Long leadId, PaginationRequest paginationRequest) {
        try {
            int limit = paginationRequest.getLimit();
            int offset = paginationRequest.getOffset();

            String sortColumn = resolveSortColumn(paginationRequest.getSortBy());
            String sortDirection = resolveSortDirection(paginationRequest.getSortDirection());

            Long totalCount = jdbcTemplate.queryForObject(BASE_COUNT_QUERY, Long.class, leadId);
            long total = (totalCount != null) ? totalCount : 0L;

            String paginatedSql = BASE_SELECT_QUERY
                    + " ORDER BY " + sortColumn + " " + sortDirection
                    + " LIMIT ? OFFSET ?";

            List<LeadActivityResponse> activities = jdbcTemplate.query(
                    paginatedSql,
                    new LeadActivityRowMapper(),
                    leadId,
                    limit,
                    offset
            );

            int totalPages = limit > 0 ? (int) Math.ceil((double) total / limit) : 0;
            int currentPage = limit > 0 ? offset / limit : 0;
            boolean hasNext = offset + limit < total;
            boolean hasPrevious = offset > 0;

            PaginationInfo paginationInfo = new PaginationInfo(
                    offset,
                    limit,
                    total,
                    totalPages,
                    currentPage,
                    hasNext,
                    hasPrevious
            );

            return new PaginatedResponse<>(activities, paginationInfo);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve lead activities by identifier", e);
        }
    }

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null) {
            return "created_at";
        }
        String normalized = sortBy.replaceAll("_", "").toLowerCase();
        return switch (normalized) {
            case "updatedat" -> "updated_at";
            case "resourcetype" -> "resource_type";
            case "resourceaction" -> "resource_action";
            case "resourceid" -> "resource_id";
            case "createdat" -> "created_at";
            default -> "created_at";
        };
    }

    private String resolveSortDirection(String sortDirection) {
        if ("ASC".equalsIgnoreCase(sortDirection)) {
            return "ASC";
        }
        return "DESC";
    }

    private static class LeadActivityRowMapper implements RowMapper<LeadActivityResponse> {

        @Override
        public LeadActivityResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeadActivityResponse.LeadActivityResponseBuilder builder = LeadActivityResponse.builder()
                    .identifier(UUID.fromString(rs.getString("identifier")))
                    .resource(ResourceEnum.valueOf(rs.getString("resource_type")))
                    .action(ResourceAction.valueOf(rs.getString("resource_action")))
                    .description(rs.getString("description"))
                    .createdBy(rs.getString("created_by"));

            Timestamp createdTimestamp = rs.getTimestamp("created_at");
            if (createdTimestamp != null) {
                builder.createdAt(createdTimestamp.toLocalDateTime());
            }
            return builder.build();
        }
    }
}


