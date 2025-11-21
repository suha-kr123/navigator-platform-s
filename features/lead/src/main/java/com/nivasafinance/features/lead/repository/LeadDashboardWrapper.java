package com.nivasafinance.features.lead.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDashboardFilters;
import com.nivasafinance.features.lead.dto.LeadDashboardResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.staff.service.StaffReadService;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LeadDashboardWrapper {

    private static final Map<String, String> SORTABLE_COLUMNS;

    static {
        SORTABLE_COLUMNS = Map.of("requestLoanAmount", "l.requested_amount",
                "leadCreatedAt", "l.created_at",
                "lastActivityDate", "l.updated_at");
    }

    private final JdbcTemplate jdbcTemplate;
    private final CodeValueMasterService codeValueMasterService;
    private final StaffReadService staffReadService;
    private final OfficeReadService officeReadService;

    public PaginatedResponse<LeadDashboardResponse> findLeadDashboard(
            PaginationRequest paginationRequest,
            LeadDashboardFilters filters
    ) {
        PaginationRequest effectivePagination = paginationRequest != null ? paginationRequest : new PaginationRequest();
        LeadDashboardFilters effectiveFilters = filters != null ? filters : new LeadDashboardFilters();

        String currentUserOfficeKey = staffReadService.getCurrentStaff().getOfficeKey();
        String currentUserOfficeCode = officeReadService.getOfficeByKey(currentUserOfficeKey).getCode();

        List<Object> queryParams = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        // Always apply office hierarchy filter first
        appendOfficeHierarchyFilter(currentUserOfficeCode, whereClause, queryParams);

        appendOwnerFilter(effectiveFilters, whereClause, queryParams);
        appendStatusFilter(effectiveFilters, whereClause, queryParams);
        appendSubstatusFilter(effectiveFilters, whereClause, queryParams);
        appendBranchFilter(effectiveFilters, currentUserOfficeCode, whereClause, queryParams);
        appendAmountFilter(effectiveFilters, whereClause, queryParams);
        appendLeadCreatedDateFilter(effectiveFilters, whereClause, queryParams);
        appendActivityDateFilter(effectiveFilters, whereClause, queryParams);
        appendActivityUpdatedByFilter(effectiveFilters, whereClause, queryParams);
        appendLastCallDirectionFilter(effectiveFilters, whereClause, queryParams);
        appendLastCallStatusFilter(effectiveFilters, whereClause, queryParams);

        String fromClause = baseFromClause();

        String sortColumn = resolveSortColumn(effectivePagination.getSortBy());
        String sortDirection = resolveSortDirection(effectivePagination.getSortDirection());

        String countSql = "SELECT COUNT(*) " + fromClause + whereClause;

        String dataSql = """
                                 SELECT
                                     l.id                                        AS internal_lead_id,
                                     l.lead_identifier                           AS lead_identifier,
                                     l.requested_amount                          AS requested_amount,
                                     prod.name                                   AS product_name,
                                     primary_contact_person.display_name         AS primary_person_name,
                                     (jsonb_path_query_first(COALESCE(primary_contact_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primary_person_number,
                                     o.name                                      AS office_name,
                                     l.owner                                     AS owner_username,
                                     l.status                                    AS lead_status,
                                     l.created_at                                AS lead_created_at,
                                     l.updated_at                   AS last_activity_at,
                                     l.updated_by                   AS last_activity_by,
                                     lead_owner_person.display_name              AS lead_owner_name,
                                     advisor_person.display_name                 AS advisor_name,
                                     (jsonb_path_query_first(COALESCE(advisor_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS advisor_number,
                                     latest_note.content                         AS note_content,
                                     (l.other_details->>'preferredCallStartTime')::time AS preferred_call_start_time,
                                     (l.other_details->>'preferredCallEndTime')::time   AS preferred_call_end_time,
                                     l.other_details->>'priority'                       AS priority_key,
                                     partners.partner_names                             AS partners,
                                     l.substatus                                        AS substatus,
                                     COALESCE(jsonb_array_length(COALESCE(l.call_logs, '[]'::jsonb)), 0) AS number_of_calls,
                                     latest_call.direction                              AS last_call_direction,
                                     latest_call.status                                 AS last_call_status,
                                     latest_call.created_at                             AS last_call_date,
                                     l.reasons->>'onhold'                               AS onhold_reason_key,
                                     (l.onhold_details->>'onHoldMovementDate')::timestamp AS onhold_date,
                                     o.code                                             AS office_code,
                                     sourcing_channel.sourcing_channel_name             AS sourcing_channel_name
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
                    new LeadDashboardRowMapper(codeValueMasterService),
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
            List<String> owners = new ArrayList<>(filters.getLeadOwner());
            boolean includeUnassigned = owners.remove("UNASSIGNED");

            if (!owners.isEmpty() && includeUnassigned) {
                whereClause.append(" AND (l.owner IN (").append(createPlaceholders(owners.size())).append(") OR l.owner IS NULL) ");
                params.addAll(owners);
            } else if (!owners.isEmpty()) {
                whereClause.append(" AND l.owner IN (").append(createPlaceholders(owners.size())).append(") ");
                params.addAll(owners);
            } else if (includeUnassigned) {
                whereClause.append(" AND l.owner IS NULL ");
            }
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

    private void appendOfficeHierarchyFilter(String currentOfficeCode, StringBuilder whereClause, List<Object> params) {
        whereClause.append(" AND o.code LIKE ? ");
        params.add(currentOfficeCode + "%");
    }

    private void appendSubstatusFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getSubstatus())) {
            List<String> normalizedSubstatuses = filters.getSubstatus()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(substatus -> substatus.toUpperCase(Locale.ROOT))
                    .toList();
            if (!normalizedSubstatuses.isEmpty()) {
                whereClause.append(" AND l.substatus IN (").append(createPlaceholders(normalizedSubstatuses.size())).append(") ");
                params.addAll(normalizedSubstatuses);
            }
        }
    }

    private void appendBranchFilter(LeadDashboardFilters filters, String currentOfficeCode, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getBranch())) {
            // Validate branch offices are within hierarchy
            List<String> validatedBranches = officeReadService.getOfficeByKeys(filters.getBranch()).stream().filter(branch -> branch.getCode().startsWith(currentOfficeCode)).map(OfficeResponse::getKey).toList();
            if (!validatedBranches.isEmpty()) {
                whereClause.append(" AND l.office_key IN (").append(createPlaceholders(validatedBranches.size())).append(") ");
                params.addAll(validatedBranches);
            }
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

    private void appendLastCallDirectionFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (filters.getLastCallDirection() != null && !filters.getLastCallDirection().trim().isEmpty()) {
            String normalizedDirection = filters.getLastCallDirection().toUpperCase(Locale.ROOT);
            whereClause.append(" AND latest_call.direction = ? ");
            params.add(normalizedDirection);
        }
    }

    private void appendLastCallStatusFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (filters.getLastCallStatus() != null && !filters.getLastCallStatus().trim().isEmpty()) {
            String normalizedStatus = filters.getLastCallStatus().toUpperCase(Locale.ROOT);
            whereClause.append(" AND latest_call.status = ? ");
            params.add(normalizedStatus);
        }
    }

    private String baseFromClause() {
        return """
                FROM n_lead l
                LEFT JOIN n_product prod ON prod.code = l.product_code
                LEFT JOIN n_office o ON o.key = l.office_key
                LEFT JOIN n_user lead_owner_user ON lead_owner_user.username = l.owner
                LEFT JOIN n_person lead_owner_person ON lead_owner_person.id = lead_owner_user.person_id
                LEFT JOIN n_contact primary_contact ON primary_contact.id = (l.other_details->>'primaryContactId')::bigint
                LEFT JOIN n_person primary_contact_person ON primary_contact.person_id = primary_contact_person.id
                LEFT JOIN n_advisor_lead_mapping alm ON alm.lead_id = l.id
                LEFT JOIN n_advisor advisor ON advisor.id = alm.advisor_id
                LEFT JOIN n_person advisor_person ON advisor.person_id = advisor_person.id
                LEFT JOIN LATERAL (
                    SELECT
                        n.content
                    FROM jsonb_array_elements_text(COALESCE(l.notes, '[]'::jsonb)) note_id
                    JOIN n_note n ON n.id = note_id::bigint
                    ORDER BY n.created_at DESC
                    LIMIT 1
                ) latest_note ON true
                LEFT JOIN LATERAL (
                    SELECT string_agg(lndr.name, ', ' ORDER BY lndr.name) AS partner_names
                    FROM n_lead_lender lead_lender
                    JOIN n_lender lndr ON lndr.key = lead_lender.lender_key
                    WHERE lead_lender.lead_id = l.id
                      AND lead_lender.status IN ('SELECTED', 'SUBMITTED')
                ) partners ON true
                LEFT JOIN n_call_log latest_call ON latest_call.id = (l.other_details->>'lastCallId')::bigint
                LEFT JOIN n_sourcing_channel_details sourcing_channel ON sourcing_channel.id = l.sourcing_channel_id
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

        private final CodeValueMasterService codeValueMasterService;

        private LeadDashboardRowMapper(CodeValueMasterService codeValueMasterService) {
            this.codeValueMasterService = codeValueMasterService;
        }

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
                    .advisorName(rs.getString("advisor_name"))
                    .advisorNumber(rs.getString("advisor_number"))
                    .leadOwner(rs.getString("lead_owner_name"))
                    .preferredCallStartTime(getLocalTime(rs, "preferred_call_start_time"))
                    .preferredCallEndTime(getLocalTime(rs, "preferred_call_end_time"))
                    .partners(rs.getString("partners"))
                    .numberOfCalls(rs.getLong("number_of_calls"))
                    .lastCallDirection(rs.getString("last_call_direction"))
                    .lastCallStatus(rs.getString("last_call_status"))
                    .lastCallDate(getLocalDateTime(rs, "last_call_date"))
                    .office(rs.getString("office_code"));

            String status = rs.getString("lead_status");
            if (status != null) {
                try {
                    builder.status(LeadStatus.valueOf(status));
                } catch (IllegalArgumentException ex) {
                    // ignore invalid status values
                }
            }

            String substatus = rs.getString("substatus");
            if (substatus != null) {
                try {
                    builder.subStatus(LeadSubStatus.valueOf(substatus));
                } catch (IllegalArgumentException ex) {
                    // ignore invalid substatus values
                }
            }

            String priorityKey = rs.getString("priority_key");
            if (priorityKey != null) {
                CodeValueResponse priority = codeValueMasterService.getByKey(priorityKey);
                builder.priority(priority);
            }

            String onHoldReasonKey = rs.getString("onhold_reason_key");
            if (onHoldReasonKey != null) {
                CodeValueResponse onHoldReason = codeValueMasterService.getByKey(onHoldReasonKey);
                builder.onHoldReason(onHoldReason);
            }

            LocalDateTime onHoldDate = getLocalDateTime(rs, "onhold_date");
            if (onHoldDate != null) {
                builder.onHoldDate(onHoldDate);
            }

            String sourcingChannelName = rs.getString("sourcing_channel_name");
            if (sourcingChannelName != null) {
                try {
                    CodeValueResponse sourcingChannel = codeValueMasterService.getByKey(sourcingChannelName);
                    builder.sourcingChannel(sourcingChannel);
                } catch (Exception e) {
                    // ignore if sourcing channel not found
                }
            }

            return builder.build();
        }

        private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column) != null ? rs.getTimestamp(column).toLocalDateTime() : null;
        }

        private LocalTime getLocalTime(ResultSet rs, String column) throws SQLException {
            return rs.getTime(column) != null ? rs.getTime(column).toLocalTime() : null;
        }
    }
}
