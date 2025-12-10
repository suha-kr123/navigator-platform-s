package com.nivasafinance.features.lead.cache;

import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDashboardFilters;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.staff.service.StaffReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Cache key generator for Lead Dashboard API
 * Generates a unique key based on:
 * - Current user's office code (for hierarchy filtering)
 * - All filter parameters
 * - Pagination parameters
 */
@Component("leadDashboardCacheKeyGenerator")
@RequiredArgsConstructor
@Slf4j
public class LeadDashboardCacheKeyGenerator implements KeyGenerator {

    private final StaffReadService staffReadService;
    private final OfficeReadService officeReadService;

    @Override
    public Object generate(Object target, Method method, Object... params) {
        PaginationRequest paginationRequest = (PaginationRequest) params[0];
        LeadDashboardFilters filters = (LeadDashboardFilters) params[1];

        // Get current user's office code (used for hierarchy filtering)
        String currentUserOfficeKey = staffReadService.getCurrentStaff().getOfficeKey();
        String currentUserOfficeCode = officeReadService.getOfficeByKey(currentUserOfficeKey).getCode();

        List<Object> keyParts = new ArrayList<>();
        keyParts.add("leadDashboard");
        keyParts.add(currentUserOfficeCode);
        
        // Add pagination parameters
        if (paginationRequest != null) {
            keyParts.add("offset:" + paginationRequest.getOffset());
            keyParts.add("limit:" + paginationRequest.getLimit());
            keyParts.add("sortBy:" + (paginationRequest.getSortBy() != null ? paginationRequest.getSortBy() : "created_at"));
            keyParts.add("sortDir:" + (paginationRequest.getSortDirection() != null ? paginationRequest.getSortDirection() : "DESC"));
        } else {
            keyParts.add("offset:0");
            keyParts.add("limit:20");
            keyParts.add("sortBy:created_at");
            keyParts.add("sortDir:DESC");
        }

        // Add filter parameters
        if (filters != null) {
            addListToKey(keyParts, "leadOwner", filters.getLeadOwner());
            addDateTimeToKey(keyParts, "lastActivityFrom", filters.getLastActivityFrom());
            addDateTimeToKey(keyParts, "lastActivityTo", filters.getLastActivityTo());
            addDateTimeToKey(keyParts, "leadCreatedFrom", filters.getLeadCreatedFrom());
            addDateTimeToKey(keyParts, "leadCreatedTo", filters.getLeadCreatedTo());
            addListToKey(keyParts, "lastUpdatedBy", filters.getLastUpdatedBy());
            addListToKey(keyParts, "status", filters.getStatus());
            addListToKey(keyParts, "substatus", filters.getSubstatus());
            addBigDecimalToKey(keyParts, "minAmount", filters.getMinAmount());
            addBigDecimalToKey(keyParts, "maxAmount", filters.getMaxAmount());
            addListToKey(keyParts, "branch", filters.getBranch());
            addStringToKey(keyParts, "lastCallDirection", filters.getLastCallDirection());
            addStringToKey(keyParts, "lastCallStatus", filters.getLastCallStatus());
            addListToKey(keyParts, "stageKey", filters.getStageKey());
            addListToKey(keyParts, "subStageKey", filters.getSubStageKey());
            addListToKey(keyParts, "stageAssignedTo", filters.getStageAssignedTo());
            addDateTimeToKey(keyParts, "stageAssignedAtFrom", filters.getStageAssignedAtFrom());
            addDateTimeToKey(keyParts, "stageAssignedAtTo", filters.getStageAssignedAtTo());
            addDateTimeToKey(keyParts, "stageEnteredAtFrom", filters.getStageEnteredAtFrom());
            addDateTimeToKey(keyParts, "stageEnteredAtTo", filters.getStageEnteredAtTo());
            addDateTimeToKey(keyParts, "onHoldDateFrom", filters.getOnHoldDateFrom());
            addDateTimeToKey(keyParts, "onHoldDateTo", filters.getOnHoldDateTo());
            addListToKey(keyParts, "onHoldReason", filters.getOnHoldReason());
            addDateToKey(keyParts, "onHoldFollowUpDateFrom", filters.getOnHoldFollowUpDateFrom());
            addDateToKey(keyParts, "onHoldFollowUpDateTo", filters.getOnHoldFollowUpDateTo());
        }

        String cacheKey = String.join("|", keyParts.stream()
                .map(Objects::toString)
                .collect(Collectors.toList()));
        log.debug("Generated cache key for Lead Dashboard: {}", cacheKey);
        return cacheKey;
    }

    private void addListToKey(List<Object> keyParts, String prefix, List<String> list) {
        if (!CollectionUtils.isEmpty(list)) {
            keyParts.add(prefix + ":" + String.join(",", list.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toUpperCase())
                    .sorted()
                    .collect(Collectors.toList())));
        }
    }

    private void addStringToKey(List<Object> keyParts, String prefix, String value) {
        if (value != null && !value.trim().isEmpty()) {
            keyParts.add(prefix + ":" + value.trim().toUpperCase());
        }
    }

    private void addDateTimeToKey(List<Object> keyParts, String prefix, LocalDateTime dateTime) {
        if (dateTime != null) {
            keyParts.add(prefix + ":" + dateTime.toString());
        }
    }

    private void addDateToKey(List<Object> keyParts, String prefix, LocalDate date) {
        if (date != null) {
            keyParts.add(prefix + ":" + date.toString());
        }
    }

    private void addBigDecimalToKey(List<Object> keyParts, String prefix, BigDecimal value) {
        if (value != null) {
            keyParts.add(prefix + ":" + value.toString());
        }
    }
}

