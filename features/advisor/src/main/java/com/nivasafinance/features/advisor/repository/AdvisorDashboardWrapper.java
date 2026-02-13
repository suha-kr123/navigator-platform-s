package com.nivasafinance.features.advisor.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardFilters;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardResponse;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
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

@Service
@AllArgsConstructor
public class AdvisorDashboardWrapper {

    private static final Map<String, String> SORTABLE_COLUMNS;

    static {
        SORTABLE_COLUMNS = Map.of(
                "createdAt", "a.created_at",
                "segmentation", "a.segmentation_details->>'segmentation'"
        );
    }

    private final JdbcTemplate jdbcTemplate;
    private final CodeValueMasterService codeValueMasterService;
    private final StaffReadService staffReadService;
    private final OfficeReadService officeReadService;

    public PaginatedResponse<AdvisorDashboardResponse> findAdvisorDashboard(
            PaginationRequest paginationRequest,
            AdvisorDashboardFilters filters
    ) {
        PaginationRequest effectivePagination = paginationRequest != null ? paginationRequest : new PaginationRequest();
        AdvisorDashboardFilters effectiveFilters = filters != null ? filters : new AdvisorDashboardFilters();

        String currentUserOfficeKey;
        String currentUserOfficeCode;
        
        try {
            currentUserOfficeKey = staffReadService.getCurrentStaff().getOfficeKey();
            if (currentUserOfficeKey == null || currentUserOfficeKey.trim().isEmpty()) {
                throw new IllegalStateException("Current user does not have an office assigned");
            }
            currentUserOfficeCode = officeReadService.getOfficeByKey(currentUserOfficeKey).getCode();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("Staff not found")) {
                throw new IllegalStateException("Current user does not have a staff record. Please contact administrator.", e);
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve current user office information", e);
        }

        List<Object> queryParams = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        // Always apply office hierarchy filter first
        appendOfficeHierarchyFilter(currentUserOfficeCode, whereClause, queryParams);

        appendStatusFilter(effectiveFilters, whereClause, queryParams);
        appendOfficeFilter(effectiveFilters, currentUserOfficeCode, whereClause, queryParams);
        appendCreatedAtFilter(effectiveFilters, whereClause, queryParams);
        appendLastLeadDateFilter(effectiveFilters, whereClause, queryParams);
        appendSegmentationFilter(effectiveFilters, whereClause, queryParams);
        appendSourcingChannelFilter(effectiveFilters, whereClause, queryParams);
        appendSalesOwnerFilter(effectiveFilters, whereClause, queryParams);

        String fromClause = baseFromClause();

        String sortColumn = resolveSortColumn(effectivePagination.getSortBy());
        String sortDirection = resolveSortDirection(effectivePagination.getSortDirection());

        // Build WHERE clause for count query - need to handle lastLeadDate filter differently
        StringBuilder countWhereClause = new StringBuilder(" WHERE 1=1 ");
        List<Object> countQueryParams = new ArrayList<>();
        
        appendOfficeHierarchyFilter(currentUserOfficeCode, countWhereClause, countQueryParams);
        appendStatusFilter(effectiveFilters, countWhereClause, countQueryParams);
        appendOfficeFilter(effectiveFilters, currentUserOfficeCode, countWhereClause, countQueryParams);
        appendCreatedAtFilter(effectiveFilters, countWhereClause, countQueryParams);
        appendSegmentationFilter(effectiveFilters, countWhereClause, countQueryParams);
        appendSourcingChannelFilter(effectiveFilters, countWhereClause, countQueryParams);
        appendSalesOwnerFilter(effectiveFilters, countWhereClause, countQueryParams);

        // Optimized COUNT query - use simpler FROM clause for counting
        String countFromClause = """
                FROM n_advisor a
                LEFT JOIN n_office o ON o.key = a.office_key
                LEFT JOIN n_sourcing_channel_details sourcing_channel ON sourcing_channel.id = a.source_channel_id
                """;
        String countSql = "SELECT COUNT(DISTINCT a.id) " + countFromClause + countWhereClause;

        String dataSql = """
                SELECT
                    a.identifier AS advisor_id,
                    p.display_name AS name,
                    (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS phone_number,
                    a.created_at AS created_at,
                    NULL::timestamp AS last_lead_date,
                    a.status AS status,
                    o.name AS office,
                    a.segmentation_details->>'segmentation' AS segmentation_key,
                    sourcing_channel.sourcing_channel_name AS sourcing_channel_name,
                    (SELECT COUNT(*)
                     FROM n_lead l2
                     JOIN n_sourcing_channel_details sc2 ON sc2.id = l2.sourcing_channel_id
                     JOIN n_referral_code_registry r2 ON r2.referral_code = sc2.marketing_details->>'referredByCode'
                     WHERE r2.entity_type::text = 'ADVISOR' AND r2.entity_identifier = a.identifier) AS no_of_leads,
                    0 AS no_of_advisors,
                    a.owner AS sales_owner,
                    (a.other_details->>'preferredCallStartTime')::time AS preferred_call_start_time,
                    (a.other_details->>'preferredCallEndTime')::time AS preferred_call_end_time,
                    sourcing_channel.marketing_details->>'referredByCode' AS referred_by_code,
                    r.entity_type::text AS referred_by_type,
                    r.entity_identifier AS referred_by_identifier,
                    COALESCE(ref_adv_p.display_name, ref_st_p.display_name, ref_lead_p.display_name, ref_lead_app_p.display_name, ref_app_by_uuid_p.display_name) AS referred_by_name,
                    COALESCE(
                        (jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                        (jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                        (jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                        (jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                        (jsonb_path_query_first(COALESCE(ref_app_by_uuid_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number')
                    ) AS referred_by_number
                """ + fromClause + whereClause +
                " ORDER BY " + sortColumn + " " + sortDirection +
                " LIMIT ? OFFSET ?";

        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, countQueryParams.toArray());
            long total = totalCount != null ? totalCount : 0L;

            List<Object> dataQueryParams = new ArrayList<>(queryParams);
            dataQueryParams.add(effectivePagination.getLimit());
            dataQueryParams.add(effectivePagination.getOffset());

            List<AdvisorDashboardResponse> content = jdbcTemplate.query(
                    dataSql,
                    new AdvisorDashboardRowMapper(codeValueMasterService),
                    dataQueryParams.toArray()
            );

            PaginationInfo paginationInfo = buildPaginationInfo(
                    effectivePagination.getOffset(),
                    effectivePagination.getLimit(),
                    total
            );

            return new PaginatedResponse<>(content, paginationInfo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch advisor dashboard details", e);
        }
    }

    private void appendOfficeHierarchyFilter(String currentOfficeCode, StringBuilder whereClause, List<Object> params) {
        whereClause.append(" AND o.code LIKE ? ");
        params.add(currentOfficeCode + "%");
    }

    private void appendStatusFilter(AdvisorDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getStatus())) {
            List<String> normalizedStatuses = filters.getStatus()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(status -> status.toUpperCase(Locale.ROOT))
                    .toList();
            if (!normalizedStatuses.isEmpty()) {
                whereClause.append(" AND a.status IN (").append(createPlaceholders(normalizedStatuses.size())).append(") ");
                params.addAll(normalizedStatuses);
            }
        } else {
            // Default: when status filter is not provided, only return CREATED or ACTIVE status
            whereClause.append(" AND a.status IN (?, ?) ");
            params.add("CREATED");
            params.add("ACTIVE");
        }
    }

    private void appendOfficeFilter(AdvisorDashboardFilters filters, String currentOfficeCode, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getOffice())) {
            // Validate offices are within hierarchy
            List<String> validatedOffices = officeReadService.getOfficeByKeys(filters.getOffice()).stream()
                    .filter(office -> office.getCode().startsWith(currentOfficeCode))
                    .map(OfficeResponse::getKey)
                    .toList();
            if (!validatedOffices.isEmpty()) {
                whereClause.append(" AND a.office_key IN (").append(createPlaceholders(validatedOffices.size())).append(") ");
                params.addAll(validatedOffices);
            }
        }
    }

    private void appendCreatedAtFilter(AdvisorDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        LocalDateTime createdFrom = filters.getCreatedAtFrom();
        LocalDateTime createdTo = filters.getCreatedAtTo();

        if (createdFrom != null) {
            whereClause.append(" AND a.created_at >= ? ");
            params.add(createdFrom);
        }

        if (createdTo != null) {
            whereClause.append(" AND a.created_at <= ? ");
            params.add(createdTo);
        }
    }

    private void appendLastLeadDateFilter(AdvisorDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        // No longer applied: n_advisor_lead_mapping is not used
    }

    private void appendSegmentationFilter(AdvisorDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getSegmentation())) {
            whereClause.append(" AND a.segmentation_details->>'segmentation' IN (")
                    .append(createPlaceholders(filters.getSegmentation().size()))
                    .append(") ");
            params.addAll(filters.getSegmentation());
        }
    }

    private void appendSourcingChannelFilter(AdvisorDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getSourcingChannel())) {
            whereClause.append(" AND sourcing_channel.sourcing_channel_name IN (")
                    .append(createPlaceholders(filters.getSourcingChannel().size()))
                    .append(") ");
            params.addAll(filters.getSourcingChannel());
        }
    }

    private void appendSalesOwnerFilter(AdvisorDashboardFilters filters, StringBuilder whereClause, List<Object> params) {
        if (!CollectionUtils.isEmpty(filters.getSalesOwner())) {
            List<String> owners = new ArrayList<>(filters.getSalesOwner());
            boolean includeUnassigned = owners.remove("UNASSIGNED");

            if (!owners.isEmpty() && includeUnassigned) {
                whereClause.append(" AND (a.owner IN (").append(createPlaceholders(owners.size()))
                        .append(") OR a.owner IS NULL) ");
                params.addAll(owners);
            } else if (!owners.isEmpty()) {
                whereClause.append(" AND a.owner IN (").append(createPlaceholders(owners.size())).append(") ");
                params.addAll(owners);
            } else if (includeUnassigned) {
                whereClause.append(" AND a.owner IS NULL ");
            }
        }
    }

    private String baseFromClause() {
        return """
                FROM n_advisor a
                LEFT JOIN n_person p ON p.id = a.person_id
                LEFT JOIN n_office o ON o.key = a.office_key
                LEFT JOIN n_sourcing_channel_details sourcing_channel ON sourcing_channel.id = a.source_channel_id
                LEFT JOIN n_referral_code_registry r ON r.referral_code = sourcing_channel.marketing_details->>'referredByCode'
                LEFT JOIN n_advisor ref_adv ON ref_adv.identifier = r.entity_identifier AND r.entity_type::text = 'ADVISOR'
                LEFT JOIN n_person ref_adv_p ON ref_adv_p.id = ref_adv.person_id
                LEFT JOIN n_staff ref_st ON ref_st.identifier = r.entity_identifier AND r.entity_type::text = 'STAFF'
                LEFT JOIN n_user ref_st_u ON ref_st_u.id = ref_st.user_id
                LEFT JOIN n_person ref_st_p ON ref_st_p.id = ref_st_u.person_id
                LEFT JOIN n_lead ref_lead ON ref_lead.lead_identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT'
                LEFT JOIN n_contact ref_lead_c ON ref_lead_c.id = (ref_lead.other_details->>'primaryContactId')::bigint
                LEFT JOIN n_person ref_lead_p ON ref_lead_p.id = ref_lead_c.person_id
                LEFT JOIN n_applicant ref_lead_app ON ref_lead_app.id = ref_lead.applicant
                LEFT JOIN n_person ref_lead_app_p ON ref_lead_app_p.id = ref_lead_app.person_id
                LEFT JOIN n_applicant ref_app_by_uuid ON ref_app_by_uuid.identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT'
                LEFT JOIN n_person ref_app_by_uuid_p ON ref_app_by_uuid_p.id = ref_app_by_uuid.person_id
                """;
    }

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null) {
            return SORTABLE_COLUMNS.get("createdAt");
        }
        return SORTABLE_COLUMNS.getOrDefault(sortBy, SORTABLE_COLUMNS.get("createdAt"));
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

    private static class AdvisorDashboardRowMapper implements RowMapper<AdvisorDashboardResponse> {

        private final CodeValueMasterService codeValueMasterService;

        private AdvisorDashboardRowMapper(CodeValueMasterService codeValueMasterService) {
            this.codeValueMasterService = codeValueMasterService;
        }

        @Override
        public AdvisorDashboardResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdvisorDashboardResponse.AdvisorDashboardResponseBuilder builder = AdvisorDashboardResponse.builder()
                    .advisorId(UUID.fromString(rs.getString("advisor_id")))
                    .name(rs.getString("name"))
                    .phoneNumber(rs.getString("phone_number"))
                    .createdAt(getLocalDateTime(rs, "created_at"))
                    .lastLeadAt(getLocalDateTime(rs, "last_lead_date"))
                    .office(rs.getString("office"))
                    .noOfLeads(getLong(rs, "no_of_leads"))
                    .noOfAdvisors(getLong(rs, "no_of_advisors"))
                    .salesOwner(rs.getString("sales_owner"));

            String status = rs.getString("status");
            if (status != null) {
                try {
                    builder.status(AdvisorStatus.valueOf(status));
                } catch (IllegalArgumentException ex) {
                    // ignore invalid status values
                }
            }

            String segmentationKey = rs.getString("segmentation_key");
            if (segmentationKey != null) {
                try {
                    CodeValueResponse segmentation = codeValueMasterService.getByKey(segmentationKey);
                    builder.segmentation(segmentation);
                } catch (Exception e) {
                    // ignore if segmentation not found
                }
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

            builder.preferredCallStartTime(getLocalTime(rs, "preferred_call_start_time"))
                    .preferredCallEndTime(getLocalTime(rs, "preferred_call_end_time"))
                    .referredByCode(rs.getString("referred_by_code"))
                    .referredByName(rs.getString("referred_by_name"))
                    .referredByNumber(rs.getString("referred_by_number"));

            String referredByTypeStr = rs.getString("referred_by_type");
            if (referredByTypeStr != null) {
                try {
                    builder.referredByType(EntityType.valueOf(referredByTypeStr));
                } catch (IllegalArgumentException ex) {
                }
            }
            String referredByIdentifierStr = rs.getString("referred_by_identifier");
            if (referredByIdentifierStr != null) {
                try {
                    builder.referredByIdentifier(java.util.UUID.fromString(referredByIdentifierStr));
                } catch (IllegalArgumentException ex) {
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

        private Long getLong(ResultSet rs, String column) throws SQLException {
            long value = rs.getLong(column);
            return rs.wasNull() ? 0L : value;
        }
    }
}

