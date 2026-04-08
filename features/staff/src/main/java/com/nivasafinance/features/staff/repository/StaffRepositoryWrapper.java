package com.nivasafinance.features.staff.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.exception.StaffExceptionFactory;
import com.nivasafinance.features.staff.exception.StaffNotFoundException;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StaffRepositoryWrapper {

    private static final String BASE_SELECT = """
            SELECT s.id,
                   s.identifier,
                   s.user_id,
                   s.office_key,
                   s.referral_code,
                   s.created_by,
                   s.created_at,
                   s.updated_by,
                   s.updated_at,
                   s.version,
                   s.is_deleted
            FROM n_staff s
            LEFT JOIN n_user u ON u.id = s.user_id
            LEFT JOIN n_person p ON p.id = u.person_id
            WHERE 1 = 1
              AND s.is_deleted = false
            """;

    private static final String BASE_COUNT = """
            SELECT COUNT(*)
            FROM n_staff s
            LEFT JOIN n_user u ON u.id = s.user_id
            LEFT JOIN n_person p ON p.id = u.person_id
            WHERE 1 = 1
              AND s.is_deleted = false
            """;

    private static final String ADMIN_BASE_SELECT = """
            SELECT s.id,
                   s.identifier,
                   s.user_id,
                   s.office_key,
                   s.referral_code,
                   s.created_by,
                   s.created_at,
                   s.updated_by,
                   s.updated_at,
                   s.version,
                   s.is_deleted
            FROM n_staff s
            LEFT JOIN n_user u ON u.id = s.user_id
            LEFT JOIN n_person p ON p.id = u.person_id
            WHERE 1 = 1
            """;

    private static final String ADMIN_BASE_COUNT = """
            SELECT COUNT(*)
            FROM n_staff s
            LEFT JOIN n_user u ON u.id = s.user_id
            LEFT JOIN n_person p ON p.id = u.person_id
            WHERE 1 = 1
            """;

    private static final String DELETED_BASE_SELECT = """
            SELECT s.id,
                   s.identifier,
                   s.user_id,
                   s.office_key,
                   s.referral_code,
                   s.created_by,
                   s.created_at,
                   s.updated_by,
                   s.updated_at,
                   s.version,
                   s.is_deleted
            FROM n_staff s
            WHERE s.is_deleted = true
            """;

    private static final String DELETED_BASE_COUNT = """
            SELECT COUNT(*)
            FROM n_staff s
            WHERE s.is_deleted = true
            """;

    private static final String DELETED_LIST_SELECT = """
            SELECT s.id,
                   s.identifier,
                   s.office_key,
                   o.name AS office_name,
                   s.referral_code,
                   s.is_deleted AS staff_deleted,
                   u.id AS user_id,
                   u.username,
                   u.status,
                   u.is_deleted AS user_deleted,
                   p.id AS person_id,
                   p.first_name,
                   p.middle_name,
                   p.last_name,
                   p.display_name,
                   p.email,
                   p.mobile_numbers
            FROM n_staff s
            LEFT JOIN n_office o ON o.key = s.office_key
            LEFT JOIN n_user u ON u.id = s.user_id
            LEFT JOIN n_person p ON p.id = u.person_id AND p.is_deleted = false
            WHERE s.is_deleted = true
            """;

    private final StaffRepository staffRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    private static final String OFFICE_FILTER = " AND s.office_key = :officeKey";

    public boolean existsByUserIdAndOfficeKey(Long userId, String officeKey) {
        return staffRepository.existsByUserIdAndOfficeKey(userId, officeKey);
    }

    public Staff saveWithException(Staff staff) {
        try {
            return staffRepository.save(staff);
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.saveFailed(messageSource);
        }
    }

    public Optional<Staff> findByUserId(Long userId) {
        try {
            return staffRepository.findByUserId(userId);
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public Staff findByUserIdWithException(Long userId) {
        try {
            return staffRepository.findByUserId(userId)
                    .orElseThrow(() -> StaffExceptionFactory.notFoundByUserId(userId, messageSource));
        } catch (StaffNotFoundException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public Optional<Staff> findByIdentifier(UUID identifier) {
        try {
            return staffRepository.findByIdentifier(identifier);
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    /**
     * Unfiltered — returns staff by identifier including soft-deleted. Used by admin delete/undo-delete.
     */
    public Optional<Staff> findByIdentifierIncludingDeleted(UUID identifier) {
        try {
            return staffRepository.findByIdentifierIncludingDeleted(identifier);
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    /**
     * Unfiltered — returns staff including soft-deleted. Used by creation uniqueness checks.
     */
    public Optional<Staff> findByUserIdIncludingDeleted(Long userId) {
        try {
            return staffRepository.findByUserIdIncludingDeleted(userId);
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public PaginatedResponse<Staff> findStaff(String officeKey, String nameQuery, PaginationRequest paginationRequest) {
        return executeStaffQuery(officeKey, nameQuery, paginationRequest);
    }

    public PaginatedResponse<Staff> findDeletedStaff(PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortColumn = resolveSortColumn(paginationRequest.getSortBy());
        String sortDirection = resolveSortDirection(paginationRequest.getSortDirection());

        StringBuilder selectQuery = new StringBuilder(DELETED_BASE_SELECT);
        MapSqlParameterSource params = new MapSqlParameterSource();
        selectQuery.append(" ORDER BY ").append(sortColumn).append(' ').append(sortDirection)
                .append(" LIMIT :limit OFFSET :offset");
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        try {
            List<Staff> staff = jdbcTemplate.query(selectQuery.toString(), params, staffRowMapper());
            long totalElements = jdbcTemplate.queryForObject(DELETED_BASE_COUNT, new MapSqlParameterSource(), Long.class);
            int totalPages = limit == 0 ? 0 : (int) Math.ceil(totalElements / (double) limit);
            int currentPage = limit == 0 ? 0 : offset / limit;
            return new PaginatedResponse<>(staff, new PaginationInfo(offset, limit, totalElements, totalPages, currentPage, offset + limit < totalElements, offset > 0));
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public PaginatedResponse<StaffResponse> findDeletedStaffWithLightweightRelations(PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortColumn = resolveSortColumn(paginationRequest.getSortBy());
        String sortDirection = resolveSortDirection(paginationRequest.getSortDirection());

        StringBuilder selectQuery = new StringBuilder(DELETED_LIST_SELECT);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        selectQuery.append(" ORDER BY ").append(sortColumn).append(' ').append(sortDirection)
                .append(" LIMIT :limit OFFSET :offset");

        try {
            List<StaffResponse> staffResponses = jdbcTemplate.query(selectQuery.toString(), params, (rs, rowNum) ->
                    StaffResponse.builder()
                            .id(rs.getLong("id"))
                            .identifier(readUuid(rs.getObject("identifier")))
                            .officeKey(rs.getString("office_key"))
                            .officeName(rs.getString("office_name"))
                            .referralCode(rs.getString("referral_code"))
                            .deleted(rs.getBoolean("staff_deleted"))
                            .userResponse(buildUserSummary(
                                    rs.getObject("user_id", Long.class),
                                    rs.getString("username"),
                                    rs.getString("status"),
                                    rs.getBoolean("user_deleted"),
                                    rs.getObject("person_id", Long.class),
                                    rs.getString("first_name"),
                                    rs.getString("middle_name"),
                                    rs.getString("last_name"),
                                    rs.getString("display_name"),
                                    rs.getString("email"),
                                    rs.getString("mobile_numbers")
                            ))
                            .build()
            );

            Long totalCount = jdbcTemplate.queryForObject(DELETED_BASE_COUNT, new MapSqlParameterSource(), Long.class);
            long totalElements = totalCount != null ? totalCount : 0L;
            int totalPages = limit == 0 ? 0 : (int) Math.ceil(totalElements / (double) limit);
            int currentPage = limit == 0 ? 0 : offset / limit;
            return new PaginatedResponse<>(staffResponses, new PaginationInfo(
                    offset,
                    limit,
                    totalElements,
                    totalPages,
                    currentPage,
                    offset + limit < totalElements,
                    offset > 0
            ));
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public PaginatedResponse<Staff> adminSearchStaff(String nameQuery, PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortColumn = resolveSortColumn(paginationRequest.getSortBy());
        String sortDirection = resolveSortDirection(paginationRequest.getSortDirection());

        String normalizedSearch = (nameQuery != null && !nameQuery.isBlank() && nameQuery.trim().length() >= 3)
                ? nameQuery.trim() : null;

        StringBuilder selectQuery = new StringBuilder(ADMIN_BASE_SELECT);
        StringBuilder countQuery = new StringBuilder(ADMIN_BASE_COUNT);
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (normalizedSearch != null) {
            selectQuery.append(" AND LOWER(p.display_name) LIKE :search");
            countQuery.append(" AND LOWER(p.display_name) LIKE :search");
            params.addValue("search", "%" + normalizedSearch.toLowerCase() + "%");
        }

        selectQuery.append(" ORDER BY ").append(sortColumn).append(' ').append(sortDirection)
                .append(" LIMIT :limit OFFSET :offset");
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        try {
            List<Staff> staff = jdbcTemplate.query(selectQuery.toString(), params, staffRowMapper());
            MapSqlParameterSource countParams = new MapSqlParameterSource();
            if (params.hasValue("search")) {
                countParams.addValue("search", params.getValue("search"));
            }
            long totalElements = jdbcTemplate.queryForObject(countQuery.toString(), countParams, Long.class);
            int totalPages = limit == 0 ? 0 : (int) Math.ceil(totalElements / (double) limit);
            int currentPage = limit == 0 ? 0 : offset / limit;
            return new PaginatedResponse<>(staff, new PaginationInfo(offset, limit, totalElements, totalPages, currentPage, offset + limit < totalElements, offset > 0));
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
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
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public List<Staff> findAllByOfficeKeys(List<String> officeKeys) {
        if (officeKeys == null || officeKeys.isEmpty()) {
            return List.of();
        }

        StringBuilder selectQuery = new StringBuilder(BASE_SELECT);
        MapSqlParameterSource params = new MapSqlParameterSource();

        selectQuery.append(" AND s.office_key IN (");
        for (int i = 0; i < officeKeys.size(); i++) {
            if (i > 0) {
                selectQuery.append(", ");
            }
            selectQuery.append(":officeKey").append(i);
            params.addValue("officeKey" + i, officeKeys.get(i));
        }
        selectQuery.append(")");

        selectQuery.append(" ORDER BY s.created_at ASC");

        try {
            return jdbcTemplate.query(selectQuery.toString(), params, staffRowMapper());
        } catch (DataAccessException ex) {
            throw StaffExceptionFactory.retrieveEntityFailed(messageSource);
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
            staff.setReferralCode(rs.getString("referral_code"));

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

            staff.setIsDeleted(rs.getBoolean("is_deleted"));

            return staff;
        };
    }

    private UserResponse buildUserSummary(Long userId,
                                          String username,
                                          String status,
                                          Boolean deleted,
                                          Long personId,
                                          String firstName,
                                          String middleName,
                                          String lastName,
                                          String displayName,
                                          String email,
                                          String mobileNumbersJson) {
        if (userId == null) {
            return null;
        }

        return UserResponse.builder()
                .id(userId)
                .username(username)
                .status(readUserStatus(status))
                .deleted(deleted)
                .personResponse(buildPersonSummary(
                        personId,
                        firstName,
                        middleName,
                        lastName,
                        displayName,
                        email,
                        mobileNumbersJson
                ))
                .build();
    }

    private PersonResponse buildPersonSummary(Long personId,
                                              String firstName,
                                              String middleName,
                                              String lastName,
                                              String displayName,
                                              String email,
                                              String mobileNumbersJson) {
        if (personId == null) {
            return null;
        }

        return PersonResponse.builder()
                .id(personId)
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .displayName(displayName)
                .email(email)
                .mobileNumbers(readMobileNumbers(mobileNumbersJson))
                .build();
    }

    private List<MobileNumberDetails> readMobileNumbers(String mobileNumbersJson) {
        if (!StringUtils.hasText(mobileNumbersJson)) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(mobileNumbersJson, new TypeReference<List<MobileNumberDetails>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse staff person mobile numbers", ex);
        }
    }

    private UserStatus readUserStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return UserStatus.valueOf(status);
    }

    private UUID readUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }
}

