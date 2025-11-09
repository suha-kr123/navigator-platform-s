package com.nivasafinance.features.lead.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDashboardFilters;
import com.nivasafinance.features.lead.dto.LeadDashboardResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class LeadDashboardWrapper {

    private static final Map<String, String> SORTABLE_COLUMNS;

    static {
        SORTABLE_COLUMNS = Map.of("requestLoanAmount", "l.requested_amount",
                "leadCreatedAt", "l.created_at",
                "lastActivityDate", "l.updated_at");
    }

    private final JdbcTemplate jdbcTemplate;

    public LeadDashboardWrapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PaginatedResponse<LeadDashboardResponse> findLeadDashboard(
            PaginationRequest paginationRequest,
            LeadDashboardFilters filters
    ) {
        PaginationRequest effectivePagination = paginationRequest != null ? paginationRequest : new PaginationRequest();
        LeadDashboardFilters effectiveFilters = filters != null ? filters : new LeadDashboardFilters();

        List<Object> queryParams = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        appendOwnerFilter(effectiveFilters, whereClause, queryParams);
        appendStatusFilter(effectiveFilters, whereClause, queryParams);
        appendBranchFilter(effectiveFilters, whereClause, queryParams);
        appendAmountFilter(effectiveFilters, whereClause, queryParams);
        appendLeadCreatedDateFilter(effectiveFilters, whereClause, queryParams);
        appendActivityDateFilter(effectiveFilters, whereClause, queryParams);
        appendActivityUpdatedByFilter(effectiveFilters, whereClause, queryParams);

        String fromClause = baseFromClause();

        /*if (Boolean.TRUE.equals(effectiveFilters.getLeadThroughAdvisors())) {
            whereClause.append(" AND advisor_mapping.advisor_id IS NOT NULL ");
        }*/ //todo handle advisor

        String sortColumn = resolveSortColumn(effectivePagination.getSortBy());
        String sortDirection = resolveSortDirection(effectivePagination.getSortDirection());

        String countSql = "SELECT COUNT(*) " + fromClause + whereClause;

        String dataSql = """
                                 SELECT
                                     l.id                                        AS internal_lead_id,
                                     l.lead_identifier                           AS lead_identifier,
                                     l.requested_amount                          AS requested_amount,
                                     prod.name                                   AS product_name,
                                     COALESCE(
                                         applicant_person.display_name,
                                         co_applicant_person.display_name,
                                         contact_person.display_name
                                     )                                           AS primary_person_name,
                                     COALESCE(
                                         (
                                             SELECT mn->>'number'
                                             FROM jsonb_array_elements(COALESCE(applicant_person.mobile_numbers, '[]'::jsonb)) mn
                                             WHERE (mn->>'isPrimary')::boolean = true
                                             LIMIT 1
                                         ),
                                         (
                                             SELECT mn->>'number'
                                             FROM jsonb_array_elements(COALESCE(co_applicant_person.mobile_numbers, '[]'::jsonb)) mn
                                             WHERE (mn->>'isPrimary')::boolean = true
                                             LIMIT 1
                                         ),
                                         (
                                             SELECT mn->>'number'
                                             FROM jsonb_array_elements(COALESCE(contact_person.mobile_numbers, '[]'::jsonb)) mn
                                             WHERE (mn->>'isPrimary')::boolean = true
                                             LIMIT 1
                                         )
                                     )                                           AS primary_person_number,
                                     o.name                                      AS office_name,
                                     l.owner                                     AS owner_username,
                                     l.status                                    AS lead_status,
                                     l.created_at                                AS lead_created_at,
                                     l.updated_at                   AS last_activity_at,
                                     l.updated_by                   AS last_activity_by,
                                     lead_owner_person.display_name              AS lead_owner_name,
                                     latest_note.content                         AS note_content
                                 """ + fromClause + whereClause +
                         " ORDER BY " + sortColumn + " " + sortDirection +
                         " LIMIT ? OFFSET ?";

        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, queryParams.toArray());
            long total = totalCount != null ? totalCount : 0L;

            List<Object> dataQueryParams = new ArrayList<>(queryParams);
            dataQueryParams.add(effectivePagination.getLimit());
            dataQueryParams.add(effectivePagination.getOffset());

            List<LeadDashboardResponse> content = jdbcTemplate.query(
                    dataSql,
                    new LeadDashboardRowMapper(),
                    dataQueryParams.toArray()
            );

            PaginationInfo paginationInfo = buildPaginationInfo(
                    effectivePagination.getOffset(),
                    effectivePagination.getLimit(),
                    total
            );

            return new PaginatedResponse<>(content, paginationInfo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch lead dashboard details", e);
        }
    }

    private void appendOwnerFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getLeadOwner())) {
            whereClause.append(" AND l.owner IN (").append(createPlaceholders(filters.getLeadOwner().size())).append(") ");
            params.addAll(filters.getLeadOwner());
        }
    }

    private void appendStatusFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getStatus())) {
            List<String> normalizedStatuses = filters.getStatus()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(status -> status.toUpperCase(Locale.ROOT))
                    .toList();
            if (!normalizedStatuses.isEmpty()) {
                whereClause.append(" AND l.status IN (").append(createPlaceholders(normalizedStatuses.size())).append(") ");
                params.addAll(normalizedStatuses);
            }
        }
    }

    private void appendBranchFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getBranch())) {
            whereClause.append(" AND l.office_key IN (").append(createPlaceholders(filters.getBranch().size())).append(") ");
            params.addAll(filters.getBranch());
        }
    }

    private void appendAmountFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        BigDecimal minAmount = filters.getMinAmount();
        BigDecimal maxAmount = filters.getMaxAmount();

        if (minAmount != null) {
            whereClause.append(" AND l.requested_amount >= ? ");
            params.add(minAmount);
        }

        if (maxAmount != null) {
            whereClause.append(" AND l.requested_amount <= ? ");
            params.add(maxAmount);
        }
    }

    private void appendLeadCreatedDateFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        LocalDateTime createdFrom = filters.getLeadCreatedFrom();
        LocalDateTime createdTo = filters.getLeadCreatedTo();

        if (createdFrom != null) {
            whereClause.append(" AND l.created_at >= ? ");
            params.add(createdFrom);
        }

        if (createdTo != null) {
            whereClause.append(" AND l.created_at <= ? ");
            params.add(createdTo);
        }
    }

    private void appendActivityDateFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        LocalDateTime activityFrom = filters.getLastActivityFrom();
        LocalDateTime activityTo = filters.getLastActivityTo();

        if (activityFrom != null) {
            whereClause.append(" AND l.updated_at >= ? ");
            params.add(activityFrom);
        }

        if (activityTo != null) {
            whereClause.append(" AND l.updated_at <= ? ");
            params.add(activityTo);
        }
    }

    private void appendActivityUpdatedByFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getLastUpdatedBy())) {
            whereClause.append(" AND l.updated_by IN (")
                    .append(createPlaceholders(filters.getLastUpdatedBy().size()))
                    .append(") ");
            params.addAll(filters.getLastUpdatedBy());
        }
    }

    private String baseFromClause() {
        return """
                FROM n_lead l
                LEFT JOIN n_product prod ON prod.code = l.product_code
                LEFT JOIN n_office o ON o.key = l.office_key
                LEFT JOIN n_user lead_owner_user ON lead_owner_user.username = l.owner
                LEFT JOIN n_person lead_owner_person ON lead_owner_person.id = lead_owner_user.person_id
                LEFT JOIN n_applicant applicant ON l.applicant = applicant.id
                LEFT JOIN n_person applicant_person ON applicant.person_id = applicant_person.id
                LEFT JOIN LATERAL (
                    SELECT (co_app)::bigint as applicant_id
                    FROM jsonb_array_elements(COALESCE(l.co_applicants, '[]'::jsonb)) AS co_app
                    LIMIT 1
                ) first_co_applicant ON true
                LEFT JOIN n_applicant co_applicant ON first_co_applicant.applicant_id = co_applicant.id
                LEFT JOIN n_person co_applicant_person ON co_applicant.person_id = co_applicant_person.id
                LEFT JOIN LATERAL (
                    SELECT (cont)::bigint as contact_id
                    FROM jsonb_array_elements(COALESCE(l.contacts, '[]'::jsonb)) AS cont
                    LIMIT 1
                ) first_contact ON true
                LEFT JOIN n_contact contact ON first_contact.contact_id = contact.id
                LEFT JOIN n_person contact_person ON contact.person_id = contact_person.id
                LEFT JOIN LATERAL (
                    SELECT
                        n.content
                    FROM jsonb_array_elements_text(COALESCE(l.notes, '[]'::jsonb)) note_id
                    JOIN n_note n ON n.id = note_id::bigint
                    ORDER BY n.created_at DESC
                    LIMIT 1
                ) latest_note ON true
                """;
    }

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null) {
            return SORTABLE_COLUMNS.get("leadCreatedAt");
        }
        return SORTABLE_COLUMNS.getOrDefault(sortBy, SORTABLE_COLUMNS.get("leadCreatedAt"));
    }

    private String resolveSortDirection(String direction) {
        if (direction == null) {
            return "DESC";
        }
        String upperDirection = direction.toUpperCase(Locale.ROOT);
        return ("ASC".equals(upperDirection) || "DESC".equals(upperDirection)) ? upperDirection : "DESC";
    }

    private String createPlaceholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }

    private PaginationInfo buildPaginationInfo(int offset, int limit, long total) {
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) total / (double) limit);
        int currentPage = limit == 0 ? 0 : offset / limit;
        boolean hasNext = offset + limit < total;
        boolean hasPrevious = offset > 0;

        return new PaginationInfo(
                offset,
                limit,
                total,
                totalPages,
                currentPage,
                hasNext,
                hasPrevious
        );
    }

    private static class LeadDashboardRowMapper implements RowMapper<LeadDashboardResponse> {
        @Override
        public LeadDashboardResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeadDashboardResponse.LeadDashboardResponseBuilder builder = LeadDashboardResponse.builder()
                    .leadIdentifier(UUID.fromString(rs.getString("lead_identifier")))
                    .requestedAmount(rs.getBigDecimal("requested_amount"))
                    .productName(rs.getString("product_name"))
                    .primaryPersonName(rs.getString("primary_person_name"))
                    .primaryPersonNumber(rs.getString("primary_person_number"))
                    .officeName(rs.getString("office_name"))
                    .ownerUsername(rs.getString("owner_username"))
                    .leadCreatedAt(getLocalDateTime(rs, "lead_created_at"))
                    .lastActivityDate(getLocalDateTime(rs, "last_activity_at"))
                    .lastActivityBy(rs.getString("last_activity_by"))
                    .recentNote(rs.getString("note_content"))
                    //todo add advisor
                    .leadOwner(rs.getString("lead_owner_name"));

            String status = rs.getString("lead_status");
            if (status != null) {
                try {
                    builder.status(LeadStatus.valueOf(status));
                } catch (IllegalArgumentException ex) {
                    // ignore invalid status values
                }
            }

            return builder.build();
        }

        private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column) != null ? rs.getTimestamp(column).toLocalDateTime() : null;
        }
    }
}

