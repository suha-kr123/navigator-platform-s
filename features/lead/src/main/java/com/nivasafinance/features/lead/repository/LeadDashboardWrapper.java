package com.nivasafinance.features.lead.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDashboardFilters;
import com.nivasafinance.features.lead.dto.LeadDashboardResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.staff.service.StaffReadService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LeadDashboardWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LeadDashboardWrapper.class);
    private static final Map<String, String> SORTABLE_COLUMNS;

    static {
        SORTABLE_COLUMNS = Map.of("requestLoanAmount", "l.requested_amount",
                "leadCreatedAt", "l.created_at",
                "lastActivityDate", "l.updated_at",
                "stageAssignedAt", "stage_assigned_at",
                "stageEnteredAt", "stage_entered_at");
    }

    private final JdbcTemplate jdbcTemplate;
    private final CodeValueMasterService codeValueMasterService;
    private final StaffReadService staffReadService;
    private final OfficeReadService officeReadService;

    @SuppressWarnings("text-blocks")
    public PaginatedResponse<LeadDashboardResponse> findLeadDashboard(
            PaginationRequest paginationRequest,
            LeadDashboardFilters filters) {
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
        appendStageFilter(effectiveFilters, whereClause, queryParams);
        appendSubStageFilter(effectiveFilters, whereClause, queryParams);
        appendStageAssignedToFilter(effectiveFilters, whereClause, queryParams);
        appendStageAssignedAtFilter(effectiveFilters, whereClause, queryParams);
        appendStageEnteredAtFilter(effectiveFilters, whereClause, queryParams);
        appendOnHoldDateFilter(effectiveFilters, whereClause, queryParams);
        appendOnHoldReasonFilter(effectiveFilters, whereClause, queryParams);
        appendOnHoldFollowUpDateFilter(effectiveFilters, whereClause, queryParams);

        String fromClause = baseFromClause();

        String sortColumn = resolveSortColumn(effectivePagination.getSortBy());
        String sortDirection = resolveSortDirection(effectivePagination.getSortDirection());

        // Optimized COUNT query - only join tables needed for filtering
        String countFromClause = """
                FROM n_lead l
                LEFT JOIN n_office o ON o.key = l.office_key
                LEFT JOIN n_call_log latest_call ON latest_call.id = (l.other_details->>'lastCallId')::bigint
                """;
        String countSql = "SELECT COUNT(*) " + countFromClause + whereClause;

        String dataSql = """
                SELECT
                    l.id                                        AS internal_lead_id,
                    l.lead_identifier                           AS lead_identifier,
                    l.requested_amount                          AS requested_amount,
                    (l.credit_rating_details->>'eligibleLoanAmount')::numeric AS eligible_amount,
                    (l.proposed_details->>'proposedLoanAmount')::numeric AS proposed_amount,
                    (l.disbursement_details->>'disbursedAmount')::numeric AS disbursed_amount,
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
                    (l.other_details->>'noOfCampaignCalls')::bigint    AS no_of_campaign_calls,
                    partners.partner_names                             AS partners,
                    l.substatus                                        AS substatus,
                    COALESCE(jsonb_array_length(COALESCE(l.call_logs, '[]'::jsonb)), 0) AS number_of_calls,
                    latest_call.direction                              AS last_call_direction,
                    latest_call.status                                 AS last_call_status,
                    latest_call.created_at                             AS last_call_date,
                    l.reasons->>'onhold'                               AS onhold_reason_key,
                    CASE
                        WHEN l.onhold_details->>'onHoldMovementDate' IS NOT NULL
                         AND l.onhold_details->>'onHoldMovementDate' != ''
                        THEN to_timestamp(l.onhold_details->>'onHoldMovementDate', 'DD-MM-YYYY HH24:MI:SS')
                        ELSE NULL
                    END AS onhold_date,
                    CASE
                        WHEN l.onhold_details->>'holdFollowUpDate' IS NOT NULL
                         AND l.onhold_details->>'holdFollowUpDate' != ''
                        THEN TO_DATE(l.onhold_details->>'holdFollowUpDate', 'DD-MM-YYYY')
                        ELSE NULL
                    END AS hold_follow_up_date,
                    o.code                                             AS office_code,
                    sourcing_channel.sourcing_channel_name             AS sourcing_channel_name,
                    (l.workflow_details->>'workflowConfigKey')         AS workflow_config_key,
                    ((l.workflow_details->'currentStageDetails')->>'stageKey') AS current_stage_key,
                    ((l.workflow_details->'currentStageDetails')->>'subStageKey') AS current_sub_stage_key,
                    ((l.workflow_details->'currentStageDetails')->>'assignedTo') AS stage_assigned_to,
                    CASE
                        WHEN (l.workflow_details->'currentStageDetails')->>'assignedAt' IS NOT NULL
                         AND (l.workflow_details->'currentStageDetails')->>'assignedAt' != ''
                        THEN to_timestamp(((l.workflow_details->'currentStageDetails')->>'assignedAt'), 'DD-MM-YYYY HH24:MI:SS')
                        ELSE NULL
                    END AS stage_assigned_at,
                    CASE
                        WHEN (l.workflow_details->'currentStageDetails')->>'enteredAt' IS NOT NULL
                         AND (l.workflow_details->'currentStageDetails')->>'enteredAt' != ''
                        THEN to_timestamp(((l.workflow_details->'currentStageDetails')->>'enteredAt'), 'DD-MM-YYYY HH24:MI:SS')
                        ELSE NULL
                    END AS stage_entered_at
                """
                + fromClause + whereClause +
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
                    dataQueryParams.toArray());

            PaginationInfo paginationInfo = buildPaginationInfo(
                    effectivePagination.getOffset(),
                    effectivePagination.getLimit(),
                    total);

            return new PaginatedResponse<>(content, paginationInfo);
        } catch (Exception e) {
            logger.error("Failed to fetch lead dashboard details. SQL: {}", dataSql, e);
            logger.error("Query params: {}", queryParams, e);
            logger.error("Root cause: ", e.getCause() != null ? e.getCause() : e);
            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new RuntimeException("Failed to fetch lead dashboard details: " + errorMsg, e);
        }
    }

    private void appendOwnerFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getLeadOwner())) {
            List<String> owners = new ArrayList<>(filters.getLeadOwner());
            boolean includeUnassigned = owners.remove("UNASSIGNED");

            if (!owners.isEmpty() && includeUnassigned) {
                whereClause.append(" AND (l.owner IN (").append(createPlaceholders(owners.size()))
                        .append(") OR l.owner IS NULL) ");
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
                whereClause.append(" AND l.status IN (").append(createPlaceholders(normalizedStatuses.size()))
                        .append(") ");
                params.addAll(normalizedStatuses);
            }
        } else {
            // Default: when status filter is not provided, only return ACTIVE status
            whereClause.append(" AND l.status = ? ");
            params.add("ACTIVE");
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
                whereClause.append(" AND l.substatus IN (").append(createPlaceholders(normalizedSubstatuses.size()))
                        .append(") ");
                params.addAll(normalizedSubstatuses);
            }
        } else {
            // If substatus filter is not provided, filter by substatus IS NULL
            whereClause.append(" AND l.substatus IS NULL ");
        }
    }

    private void appendBranchFilter(LeadDashboardFilters filters, String currentOfficeCode, StringBuilder whereClause,
            List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getBranch())) {
            // Validate branch offices are within hierarchy
            List<String> validatedBranches = officeReadService.getOfficeByKeys(filters.getBranch()).stream()
                    .filter(branch -> branch.getCode().startsWith(currentOfficeCode)).map(OfficeResponse::getKey)
                    .toList();
            if (!validatedBranches.isEmpty()) {
                whereClause.append(" AND l.office_key IN (").append(createPlaceholders(validatedBranches.size()))
                        .append(") ");
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

    private void appendLeadCreatedDateFilter(LeadDashboardFilters filters, StringBuilder whereClause,
            List<Object> params) {
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

    private void appendActivityDateFilter(LeadDashboardFilters filters, StringBuilder whereClause,
            List<Object> params) {
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

    private void appendActivityUpdatedByFilter(LeadDashboardFilters filters, StringBuilder whereClause,
            List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getLastUpdatedBy())) {
            whereClause.append(" AND l.updated_by IN (")
                    .append(createPlaceholders(filters.getLastUpdatedBy().size()))
                    .append(") ");
            params.addAll(filters.getLastUpdatedBy());
        }
    }

    private void appendLastCallDirectionFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getLastCallDirection())) {
            List<String> normalizedDirections = filters.getLastCallDirection()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(direction -> direction.toUpperCase(Locale.ROOT).trim())
                    .filter(direction -> !direction.isEmpty())
                    .toList();
            if (!normalizedDirections.isEmpty()) {
                whereClause.append(" AND latest_call.direction IN (")
                        .append(createPlaceholders(normalizedDirections.size()))
                        .append(") ");
                params.addAll(normalizedDirections);
            }
        }
    }

    private void appendLastCallStatusFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getLastCallStatus())) {
            List<String> normalizedStatuses = filters.getLastCallStatus()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(status -> status.toUpperCase(Locale.ROOT).trim())
                    .filter(status -> !status.isEmpty())
                    .toList();
            if (!normalizedStatuses.isEmpty()) {
                whereClause.append(" AND latest_call.status IN (")
                        .append(createPlaceholders(normalizedStatuses.size()))
                        .append(") ");
                params.addAll(normalizedStatuses);
            }
        }
    }

    private void appendStageFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getStageKey())) {
            List<String> normalizedStageKeys = filters.getStageKey()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(stageKey -> stageKey.trim())
                    .filter(stageKey -> !stageKey.isEmpty())
                    .toList();
            if (!normalizedStageKeys.isEmpty()) {
                // Optimized: Use jsonb_extract_path_text which is more efficient than chained operators
                // This can better utilize GIN indexes on workflow_details
                whereClause.append(" AND jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'stageKey') IN (")
                        .append(createPlaceholders(normalizedStageKeys.size()))
                        .append(") ");
                params.addAll(normalizedStageKeys);
            }
        }
    }

    private void appendSubStageFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getSubStageKey())) {
            List<String> normalizedSubStageKeys = filters.getSubStageKey()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(subStageKey -> subStageKey.trim())
                    .filter(subStageKey -> !subStageKey.isEmpty())
                    .toList();
            if (!normalizedSubStageKeys.isEmpty()) {
                // Optimized: Use jsonb_extract_path_text for better performance
                whereClause.append(" AND jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'subStageKey') IN (")
                        .append(createPlaceholders(normalizedSubStageKeys.size()))
                        .append(") ");
                params.addAll(normalizedSubStageKeys);
            }
        }
    }

    private void appendStageAssignedToFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getStageAssignedTo())) {
            List<String> assignedToList = new ArrayList<>(filters.getStageAssignedTo());
            boolean includeUnassigned = assignedToList.remove("UNASSIGNED");

            if (!assignedToList.isEmpty() && includeUnassigned) {
                // Optimized: Use jsonb_extract_path_text for better performance
                whereClause.append(" AND (jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'assignedTo') IN (")
                        .append(createPlaceholders(assignedToList.size()))
                        .append(") OR jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'assignedTo') IS NULL) ");
                params.addAll(assignedToList);
            } else if (!assignedToList.isEmpty()) {
                whereClause.append(" AND jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'assignedTo') IN (")
                        .append(createPlaceholders(assignedToList.size()))
                        .append(") ");
                params.addAll(assignedToList);
            } else if (includeUnassigned) {
                whereClause.append(" AND jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'assignedTo') IS NULL ");
            }
        }
    }

    private void appendStageAssignedAtFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        LocalDateTime assignedAtFrom = filters.getStageAssignedAtFrom();
        LocalDateTime assignedAtTo = filters.getStageAssignedAtTo();

        if (assignedAtFrom != null || assignedAtTo != null) {
            // Optimized: Extract once using jsonb_extract_path_text, NULL values excluded automatically
            whereClause.append(" AND jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'assignedAt') IS NOT NULL ")
                    .append("AND jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'assignedAt') != '' ");
            if (assignedAtFrom != null) {
                whereClause.append("AND to_timestamp(jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'assignedAt'), 'DD-MM-YYYY HH24:MI:SS') >= ? ");
                params.add(assignedAtFrom);
            }
            if (assignedAtTo != null) {
                whereClause.append("AND to_timestamp(jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'assignedAt'), 'DD-MM-YYYY HH24:MI:SS') <= ? ");
                params.add(assignedAtTo);
            }
        }
    }

    private void appendStageEnteredAtFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        LocalDateTime enteredAtFrom = filters.getStageEnteredAtFrom();
        LocalDateTime enteredAtTo = filters.getStageEnteredAtTo();

        if (enteredAtFrom != null || enteredAtTo != null) {
            // Optimized: Extract once using jsonb_extract_path_text, NULL values excluded automatically
            whereClause.append(" AND jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'enteredAt') IS NOT NULL ")
                    .append("AND jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'enteredAt') != '' ");
            if (enteredAtFrom != null) {
                whereClause.append("AND to_timestamp(jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'enteredAt'), 'DD-MM-YYYY HH24:MI:SS') >= ? ");
                params.add(enteredAtFrom);
            }
            if (enteredAtTo != null) {
                whereClause.append("AND to_timestamp(jsonb_extract_path_text(l.workflow_details, 'currentStageDetails', 'enteredAt'), 'DD-MM-YYYY HH24:MI:SS') <= ? ");
                params.add(enteredAtTo);
            }
        }
    }

    private void appendOnHoldDateFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        LocalDateTime onHoldDateFrom = filters.getOnHoldDateFrom();
        LocalDateTime onHoldDateTo = filters.getOnHoldDateTo();

        if (onHoldDateFrom != null) {
            // Optimized: Extract once and compare, NULL values are automatically excluded
            whereClause.append(" AND jsonb_extract_path_text(l.onhold_details, 'onHoldMovementDate') IS NOT NULL ")
                    .append("AND jsonb_extract_path_text(l.onhold_details, 'onHoldMovementDate') != '' ")
                    .append("AND to_timestamp(jsonb_extract_path_text(l.onhold_details, 'onHoldMovementDate'), 'DD-MM-YYYY HH24:MI:SS') >= ? ");
            params.add(onHoldDateFrom);
        }

        if (onHoldDateTo != null) {
            whereClause.append(" AND jsonb_extract_path_text(l.onhold_details, 'onHoldMovementDate') IS NOT NULL ")
                    .append("AND jsonb_extract_path_text(l.onhold_details, 'onHoldMovementDate') != '' ")
                    .append("AND to_timestamp(jsonb_extract_path_text(l.onhold_details, 'onHoldMovementDate'), 'DD-MM-YYYY HH24:MI:SS') <= ? ");
            params.add(onHoldDateTo);
        }
    }

    private void appendOnHoldReasonFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getOnHoldReason())) {
            List<String> normalizedReasons = filters.getOnHoldReason()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(reason -> reason.trim())
                    .filter(reason -> !reason.isEmpty())
                    .toList();
            if (!normalizedReasons.isEmpty()) {
                // Optimized: Use jsonb_extract_path_text for better performance
                whereClause.append(" AND jsonb_extract_path_text(l.reasons, 'onhold') IN (")
                        .append(createPlaceholders(normalizedReasons.size()))
                        .append(") ");
                params.addAll(normalizedReasons);
            }
        }
    }

    private void appendOnHoldFollowUpDateFilter(LeadDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        LocalDate followUpDateFrom = filters.getOnHoldFollowUpDateFrom();
        LocalDate followUpDateTo = filters.getOnHoldFollowUpDateTo();

        if (followUpDateFrom != null) {
            // Optimized: Extract once and compare, NULL values are automatically excluded
            whereClause.append(" AND jsonb_extract_path_text(l.onhold_details, 'holdFollowUpDate') IS NOT NULL ")
                    .append("AND TO_DATE(jsonb_extract_path_text(l.onhold_details, 'holdFollowUpDate'), 'DD-MM-YYYY') >= ? ");
            params.add(java.sql.Date.valueOf(followUpDateFrom));
        }

        if (followUpDateTo != null) {
            whereClause.append(" AND jsonb_extract_path_text(l.onhold_details, 'holdFollowUpDate') IS NOT NULL ")
                    .append("AND TO_DATE(jsonb_extract_path_text(l.onhold_details, 'holdFollowUpDate'), 'DD-MM-YYYY') <= ? ");
            params.add(java.sql.Date.valueOf(followUpDateTo));
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
                LEFT JOIN (
                    SELECT
                        lead_lender.lead_id,
                        string_agg(lndr.name, ', ' ORDER BY lndr.name) AS partner_names
                    FROM n_lead_lender lead_lender
                    JOIN n_lender lndr ON lndr.key = lead_lender.lender_key
                    WHERE lead_lender.status IN ('SELECTED', 'SUBMITTED')
                    GROUP BY lead_lender.lead_id
                ) partners ON partners.lead_id = l.id
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
                hasPrevious);
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
                    .eligibleAmount(rs.getBigDecimal("eligible_amount"))
                    .proposedAmount(rs.getBigDecimal("proposed_amount"))
                    .disbursedAmount(rs.getBigDecimal("disbursed_amount"))
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
                    .noOfCampaignCalls(rs.getLong("no_of_campaign_calls"))
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
                try {
                    CodeValueResponse priority = codeValueMasterService.getByKey(priorityKey);
                    builder.priority(priority);
                } catch (Exception e) {
                    // Ignore if priority not found - leave as null
                }
            }

            String onHoldReasonKey = rs.getString("onhold_reason_key");
            if (onHoldReasonKey != null) {
                try {
                    CodeValueResponse onHoldReason = codeValueMasterService.getByKey(onHoldReasonKey);
                    builder.onHoldReason(onHoldReason);
                } catch (Exception e) {
                    // Ignore if onhold reason not found - leave as null
                }
            }

            LocalDateTime onHoldDate = getLocalDateTime(rs, "onhold_date");
            if (onHoldDate != null) {
                builder.onHoldDate(onHoldDate);
            }

            LocalDate holdFollowUpDate = getLocalDate(rs, "hold_follow_up_date");
            if (holdFollowUpDate != null) {
                builder.holdFollowUpDate(holdFollowUpDate);
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

            // Map workflow details
            builder.workflowConfigKey(rs.getString("workflow_config_key"))
                    .currentStageKey(rs.getString("current_stage_key"))
                    .currentSubStageKey(rs.getString("current_sub_stage_key"))
                    .stageAssignedTo(rs.getString("stage_assigned_to"))
                    .stageAssignedAt(getLocalDateTime(rs, "stage_assigned_at"))
                    .stageEnteredAt(getLocalDateTime(rs, "stage_entered_at"));

            // Populate currentSubStageName
            String currentSubStageKey = rs.getString("current_sub_stage_key");
            if (currentSubStageKey != null && !currentSubStageKey.trim().isEmpty()) {
                try {
                    CodeValueResponse subStage = codeValueMasterService.getByKey(currentSubStageKey);
                    if (subStage != null && subStage.getValue() != null) {
                        builder.currentSubStageName(subStage.getValue());
                    }
                } catch (Exception e) {
                    // Ignore if sub-stage not found - leave name as null
                }
            }

            return builder.build();
        }

        private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
            return rs.getTimestamp(column) != null ? rs.getTimestamp(column).toLocalDateTime() : null;
        }

        private LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
            return rs.getDate(column) != null ? rs.getDate(column).toLocalDate() : null;
        }

        private LocalTime getLocalTime(ResultSet rs, String column) throws SQLException {
            return rs.getTime(column) != null ? rs.getTime(column).toLocalTime() : null;
        }
    }
}
