package com.nivasafinance.features.staff.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.staff.entity.Staff;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StaffRepositoryWrapper {

    private static final String BASE_SELECT = """
            SELECT s.id,
                   s.identifier,
                   s.user_id,
                   s.office_key,
                   s.created_by,
                   s.created_at,
                   s.updated_by,
                   s.updated_at,
                   s.version
            FROM n_staff s
            LEFT JOIN n_user u ON u.id = s.user_id
            LEFT JOIN n_person p ON p.id = u.person_id
            WHERE 1 = 1
            """;

    private static final String BASE_COUNT = """
            SELECT COUNT(*)
            FROM n_staff s
            LEFT JOIN n_user u ON u.id = s.user_id
            LEFT JOIN n_person p ON p.id = u.person_id
            WHERE 1 = 1
            """;

    private final StaffRepository staffRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String OFFICE_FILTER = " AND s.office_key = :officeKey";

    public boolean existsByUserIdAndOfficeKey(Long userId, String officeKey) {
        return staffRepository.existsByUserIdAndOfficeKey(userId, officeKey);
    }

    public Staff saveWithException(Staff staff) {
        try {
            return staffRepository.save(staff);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to save staff", ex);
        }
    }

    public PaginatedResponse<Staff> findStaff(String officeKey, String nameQuery, PaginationRequest paginationRequest) {
        return executeStaffQuery(officeKey, nameQuery, paginationRequest);
    }

    private PaginatedResponse<Staff> executeStaffQuery(String officeKey, String nameQuery, PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();

        String normalizedSearch = normalizeSearchQuery(nameQuery);
        String sortColumn = resolveSortColumn(paginationRequest.getSortBy());
        String sortDirection = resolveSortDirection(paginationRequest.getSortDirection());

        StringBuilder selectQuery = new StringBuilder(BASE_SELECT);
        StringBuilder countQuery = new StringBuilder(BASE_COUNT);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (StringUtils.hasText(officeKey)) {
            selectQuery.append(OFFICE_FILTER);
            countQuery.append(OFFICE_FILTER);
            params.addValue("officeKey", officeKey);
        }

        if (normalizedSearch != null) {
            selectQuery.append(" AND LOWER(p.display_name) LIKE :search");
            countQuery.append(" AND LOWER(p.display_name) LIKE :search");
            params.addValue("search", "%" + normalizedSearch.toLowerCase() + "%");
        }

        selectQuery.append(" ORDER BY ")
                .append(sortColumn)
                .append(' ')
                .append(sortDirection)
                .append(" LIMIT :limit OFFSET :offset");

        params.addValue("limit", limit);
        params.addValue("offset", offset);

        try {
            List<Staff> staff = jdbcTemplate.query(selectQuery.toString(), params, staffRowMapper());

            MapSqlParameterSource countParams = new MapSqlParameterSource();
            if (params.hasValue("search")) {
                countParams.addValue("search", params.getValue("search"));
            }
            if (params.hasValue("officeKey")) {
                countParams.addValue("officeKey", params.getValue("officeKey"));
            }

            long totalElements = jdbcTemplate.queryForObject(countQuery.toString(), countParams, Long.class);
            int totalPages = limit == 0 ? 0 : (int) Math.ceil(totalElements / (double) limit);
            int currentPage = limit == 0 ? 0 : offset / limit;
            boolean hasNext = offset + limit < totalElements;
            boolean hasPrevious = offset > 0;

            PaginationInfo paginationInfo = new PaginationInfo(
                    offset,
                    limit,
                    totalElements,
                    totalPages,
                    currentPage,
                    hasNext,
                    hasPrevious
            );

            return new PaginatedResponse<>(staff, paginationInfo);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to fetch staff list", ex);
        }
    }

    private String normalizeSearchQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return null;
        }

        String trimmed = query.trim();

        if (trimmed.length() < 3) {
            throw new BadRequestException("Search term must be at least 3 characters long");
        }

        return trimmed;
    }

    private String resolveSortColumn(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "s.created_at";
        }

        String normalized = sortBy.trim().toLowerCase();
        return switch (normalized) {
            case "id" -> "s.id";
            case "updated_at", "updatedat" -> "s.updated_at";
            case "created_at", "createdat" -> "s.created_at";
            default -> "s.created_at";
        };
    }

    private String resolveSortDirection(String sortDirection) {
        if (!StringUtils.hasText(sortDirection)) {
            return "DESC";
        }
        return "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
    }

    private RowMapper<Staff> staffRowMapper() {
        return (rs, rowNum) -> {
            Staff staff = new Staff();
            staff.setId(rs.getLong("id"));
            Object identifierObj = rs.getObject("identifier");
            if (identifierObj instanceof UUID uuid) {
                staff.setIdentifier(uuid);
            } else if (identifierObj != null) {
                staff.setIdentifier(UUID.fromString(identifierObj.toString()));
            }
            staff.setUserId(rs.getLong("user_id"));
            staff.setOfficeKey(rs.getString("office_key"));

            staff.setCreatedBy(rs.getString("created_by"));
            staff.setUpdatedBy(rs.getString("updated_by"));

            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (createdAt != null) {
                staff.setCreatedAt(createdAt.toLocalDateTime());
            }
            if (updatedAt != null) {
                staff.setUpdatedAt(updatedAt.toLocalDateTime());
            }

            long version = rs.getLong("version");
            if (!rs.wasNull()) {
                staff.setVersion(version);
            }

            return staff;
        };
    }
}


