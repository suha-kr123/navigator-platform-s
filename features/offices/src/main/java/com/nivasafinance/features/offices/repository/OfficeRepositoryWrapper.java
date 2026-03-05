package com.nivasafinance.features.offices.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OfficeRepositoryWrapper {

    private static final String BASE_SELECT = """
            SELECT o.id,
                   o.name,
                   o.key,
                   o.code,
                   o.is_active,
                   o.address_data,
                   o.parent_id,
                   o.created_by,
                   o.created_at,
                   o.updated_by,
                   o.updated_at,
                   o.version
            FROM n_office o
            WHERE 1 = 1
            """;

    private static final String BASE_COUNT = """
            SELECT COUNT(*)
            FROM n_office o
            WHERE 1 = 1
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public PaginatedResponse<Office> findOffices(String parentCodePrefix, String nameQuery,
                                                 Boolean activeOnly, PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();

        StringBuilder selectQuery = new StringBuilder(BASE_SELECT);
        StringBuilder countQuery = new StringBuilder(BASE_COUNT);
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (StringUtils.hasText(parentCodePrefix)) {
            selectQuery.append(" AND o.code LIKE :codePrefix");
            countQuery.append(" AND o.code LIKE :codePrefix");
            params.addValue("codePrefix", parentCodePrefix + "%");
        }

        if (StringUtils.hasText(nameQuery)) {
            selectQuery.append(" AND LOWER(o.name) LIKE :nameQuery");
            countQuery.append(" AND LOWER(o.name) LIKE :nameQuery");
            params.addValue("nameQuery", "%" + nameQuery.trim().toLowerCase() + "%");
        }

        if (activeOnly != null) {
            selectQuery.append(" AND o.is_active = :activeOnly");
            countQuery.append(" AND o.is_active = :activeOnly");
            params.addValue("activeOnly", activeOnly);
        }

        String sortColumn = resolveSortColumn(paginationRequest.getSortBy());
        String sortDirection = resolveSortDirection(paginationRequest.getSortDirection());

        selectQuery.append(" ORDER BY ")
                .append(sortColumn)
                .append(' ')
                .append(sortDirection)
                .append(" LIMIT :limit OFFSET :offset");

        params.addValue("limit", limit);
        params.addValue("offset", offset);

        try {
            List<Office> offices = jdbcTemplate.query(selectQuery.toString(), params, officeRowMapper());

            MapSqlParameterSource countParams = new MapSqlParameterSource();
            if (params.hasValue("codePrefix")) {
                countParams.addValue("codePrefix", params.getValue("codePrefix"));
            }
            if (params.hasValue("nameQuery")) {
                countParams.addValue("nameQuery", params.getValue("nameQuery"));
            }
            if (params.hasValue("activeOnly")) {
                countParams.addValue("activeOnly", params.getValue("activeOnly"));
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

            return new PaginatedResponse<>(offices, paginationInfo);
        } catch (DataAccessException ex) {
            throw OfficeExceptionFactory.retrieveFailed(messageSource);
        }
    }

    public List<Office> findAllByCodePrefix(String codePrefix) {
        StringBuilder selectQuery = new StringBuilder(BASE_SELECT);
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (StringUtils.hasText(codePrefix)) {
            selectQuery.append(" AND o.code LIKE :codePrefix");
            params.addValue("codePrefix", codePrefix + "%");
        }

        selectQuery.append(" ORDER BY o.code ASC");

        try {
            return jdbcTemplate.query(selectQuery.toString(), params, officeRowMapper());
        } catch (DataAccessException ex) {
            throw OfficeExceptionFactory.retrieveFailed(messageSource);
        }
    }

    /**
     * Returns one level only: roots when parentId is null, or direct children when parentId is set.
     */
    public List<Office> findAllByParentId(Long parentId) {
        StringBuilder selectQuery = new StringBuilder(BASE_SELECT);
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (parentId == null) {
            selectQuery.append(" AND o.parent_id IS NULL");
        } else {
            selectQuery.append(" AND o.parent_id = :parentId");
            params.addValue("parentId", parentId);
        }

        selectQuery.append(" ORDER BY o.code ASC");

        try {
            return jdbcTemplate.query(selectQuery.toString(), params, officeRowMapper());
        } catch (DataAccessException ex) {
            throw OfficeExceptionFactory.retrieveFailed(messageSource);
        }
    }

    private static final int SEARCH_MAX_RESULTS = 100;

    private static final int SEARCH_MIN_LENGTH = 3;

    public List<Office> findAllByNameContaining(String nameQuery) {
        if (!StringUtils.hasText(nameQuery) || nameQuery.trim().length() < SEARCH_MIN_LENGTH) {
            return List.of();
        }
        StringBuilder selectQuery = new StringBuilder(BASE_SELECT);
        MapSqlParameterSource params = new MapSqlParameterSource();
        selectQuery.append(" AND LOWER(o.name) LIKE :pattern ORDER BY o.code ASC LIMIT :limit");
        params.addValue("pattern", "%" + nameQuery.trim().toLowerCase() + "%");
        params.addValue("limit", SEARCH_MAX_RESULTS);
        try {
            return jdbcTemplate.query(selectQuery.toString(), params, officeRowMapper());
        } catch (DataAccessException ex) {
            throw OfficeExceptionFactory.retrieveFailed(messageSource);
        }
    }

    public java.util.Map<Long, Integer> countChildrenByParentIds(java.util.List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        StringBuilder query = new StringBuilder("SELECT parent_id, COUNT(*) AS cnt FROM n_office WHERE parent_id IN (");
        MapSqlParameterSource params = new MapSqlParameterSource();
        for (int i = 0; i < parentIds.size(); i++) {
            if (i > 0) {
                query.append(", ");
            }
            String param = "pid" + i;
            query.append(":").append(param);
            params.addValue(param, parentIds.get(i));
        }
        query.append(") GROUP BY parent_id");
        try {
            java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(query.toString(), params);
            java.util.Map<Long, Integer> result = new java.util.HashMap<>();
            for (java.util.Map<String, Object> row : rows) {
                Long pid = ((Number) row.get("parent_id")).longValue();
                Integer cnt = ((Number) row.get("cnt")).intValue();
                result.put(pid, cnt);
            }
            return result;
        } catch (DataAccessException ex) {
            throw OfficeExceptionFactory.retrieveFailed(messageSource);
        }
    }
    private String resolveSortColumn(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "o.created_at";
        }

        return switch (sortBy.trim().toLowerCase()) {
            case "name" -> "o.name";
            case "code" -> "o.code";
            case "key" -> "o.key";
            case "updated_at", "updatedat" -> "o.updated_at";
            case "created_at", "createdat" -> "o.created_at";
            default -> "o.created_at";
        };
    }

    private String resolveSortDirection(String sortDirection) {
        if (!StringUtils.hasText(sortDirection)) {
            return "DESC";
        }
        return "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
    }

    private RowMapper<Office> officeRowMapper() {
        return (rs, rowNum) -> {
            Office office = new Office();
            office.setId(rs.getLong("id"));
            office.setName(rs.getString("name"));
            office.setKey(rs.getString("key"));
            office.setCode(rs.getString("code"));
            Object activeObj = null;
            try {
                activeObj = rs.getObject("is_active");
            } catch (Exception ignored) {}
            if (activeObj != null) {
                office.setIsActive(rs.getBoolean("is_active"));
            }
            office.setParentId(rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null);
            office.setCreatedBy(rs.getString("created_by"));
            office.setUpdatedBy(rs.getString("updated_by"));

            if (rs.getTimestamp("created_at") != null) {
                office.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                office.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            long version = rs.getLong("version");
            if (!rs.wasNull()) {
                office.setVersion(version);
            }
            String addressJson = rs.getString("address_data");
            if (StringUtils.hasText(addressJson)) {
                try {
                    AddressData addressData = objectMapper.readValue(addressJson, AddressData.class);
                    office.setAddressData(addressData);
                } catch (Exception e) {
                    throw OfficeExceptionFactory.retrieveFailed(messageSource);
                }
            }
            return office;
        };
    }
}
