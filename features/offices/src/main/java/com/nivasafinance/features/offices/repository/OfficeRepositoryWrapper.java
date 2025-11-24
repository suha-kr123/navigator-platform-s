package com.nivasafinance.features.offices.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.offices.entity.Office;
import lombok.RequiredArgsConstructor;
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

    public PaginatedResponse<Office> findOffices(String parentCodePrefix, String nameQuery,
                                                 PaginationRequest paginationRequest) {
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
            throw new RuntimeException("Failed to fetch offices", ex);
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
            throw new RuntimeException("Failed to fetch offices by code prefix", ex);
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
                    throw new RuntimeException("Failed to parse office address data", e);
                }
            }
            return office;
        };
    }
}

