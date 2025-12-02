package com.nivasafinance.features.advisoractivity.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisoractivity.dto.AdvisorActivityResponse;
import com.nivasafinance.features.advisoractivity.entity.AdvisorActivity;
import com.nivasafinance.features.advisoractivity.enums.ResourceAction;
import com.nivasafinance.features.advisoractivity.enums.ResourceEnum;
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
public class AdvisorActivityRepositoryWrapper {

    private final JdbcTemplate jdbcTemplate;
    private final AdvisorActivityRepository advisorActivityRepository;

    private static final String BASE_SELECT_QUERY = """
            SELECT
                aa.identifier,
                aa.resource_type,
                aa.resource_action,
                aa.resource_id,
                aa.description,
                aa.metadata,
                aa.created_at,
                aa.created_by,
                aa.updated_at,
                aa.updated_by,
                aa.version
            FROM n_advisor_activity aa
            WHERE aa.advisor_id = ?
            """;

    private static final String BASE_COUNT_QUERY = "SELECT COUNT(*) FROM n_advisor_activity WHERE advisor_id = ?";

    public AdvisorActivity saveWithException(AdvisorActivity advisorActivity){
        try {
            return advisorActivityRepository.save(advisorActivity);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to Save advisor Activity", e);
        }
    }

    public PaginatedResponse<AdvisorActivityResponse> findAllByAdvisorIdWithException(
            Long advisorId, PaginationRequest paginationRequest) {
        try {
            int limit = paginationRequest.getLimit();
            int offset = paginationRequest.getOffset();

            String sortColumn = resolveSortColumn(paginationRequest.getSortBy());
            String sortDirection = resolveSortDirection(paginationRequest.getSortDirection());

            Long totalCount = jdbcTemplate.queryForObject(BASE_COUNT_QUERY, Long.class, advisorId);
            long total = (totalCount != null) ? totalCount : 0L;

            String paginatedSql = BASE_SELECT_QUERY
                    + " ORDER BY " + sortColumn + " " + sortDirection
                    + " LIMIT ? OFFSET ?";

            List<AdvisorActivityResponse> activities = jdbcTemplate.query(
                    paginatedSql,
                    new AdvisorActivityRowMapper(),
                    advisorId,
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
            throw new RuntimeException("Failed to retrieve advisor activities by identifier", e);
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

    private static class AdvisorActivityRowMapper implements RowMapper<AdvisorActivityResponse> {

        @Override
        public AdvisorActivityResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdvisorActivityResponse.AdvisorActivityResponseBuilder builder = AdvisorActivityResponse.builder()
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

