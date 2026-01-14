package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.EnrichedCallNotificationResponse;
import com.nivasafinance.common.utils.PhoneNumberUtils;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.service.CallNotificationService;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallNotificationServiceImpl implements CallNotificationService {

    private final UserReadService userReadService;
    private final PersonReadService personReadService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public PaginatedResponse<EnrichedCallNotificationResponse> getNotificationsForCurrentUser(PaginationRequest paginationRequest) {
        String username = UserContext.getUsername();
        if (username == null || username.isBlank()) {
            return buildEmptyPaginatedResponse(paginationRequest);
        }

        Optional<User> userOpt = userReadService.findUserByUsername(username);
        if (userOpt.isEmpty() || userOpt.get().getPerson() == null) {
            return buildEmptyPaginatedResponse(paginationRequest);
        }

        User user = userOpt.get();
        
        // Get user's primary phone number - try both normalized and original formats
        String primaryPhoneOriginal = user.getPerson().getMobileNumbers().stream()
                        .filter(mobile -> mobile.getIsPrimary() != null && mobile.getIsPrimary())
                        .filter(mobile -> mobile.getNumber() != null && !mobile.getNumber().isBlank())
                .map(mobile -> mobile.getNumber())
                        .findFirst()
                .orElse(null);

        if (primaryPhoneOriginal == null) {
            log.debug("No primary phone number found for user: {}", username);
            return buildEmptyPaginatedResponse(paginationRequest);
        }

        String primaryPhoneNormalized = PhoneNumberUtils.normalizePhoneNumber(primaryPhoneOriginal);
        
        // Get total count for pagination
        long totalCount = getTotalCallLogCount(primaryPhoneOriginal, primaryPhoneNormalized);
        
        if (totalCount == 0) {
            return buildEmptyPaginatedResponse(paginationRequest);
        }
        
        // Query call logs by phone number with pagination - try both formats
        List<CallLog> callLogs = findCallLogsByPhoneNumber(primaryPhoneOriginal, primaryPhoneNormalized, paginationRequest);
        
        if (callLogs.isEmpty()) {
            return buildEmptyPaginatedResponse(paginationRequest);
        }

        // Get all call log IDs
        Set<Long> callLogIds = callLogs.stream()
                .map(CallLog::getId)
                .collect(Collectors.toSet());

        // Find leads and advisors linked to these call logs (can have multiple per call log)
        Map<Long, List<EnrichedCallNotificationResponse.LeadInfo>> leadInfoMap = findLeadsByCallLogIds(new ArrayList<>(callLogIds));
        Map<Long, List<EnrichedCallNotificationResponse.AdvisorInfo>> advisorInfoMap = findAdvisorsByCallLogIds(new ArrayList<>(callLogIds));

        // Build enriched responses
        List<EnrichedCallNotificationResponse> responses = callLogs.stream()
                .map(callLog -> {
                    EnrichedCallNotificationResponse.EnrichedCallNotificationResponseBuilder builder = 
                        EnrichedCallNotificationResponse.builder()
                            .callSid(callLog.getProviderId())
                            .callFrom(callLog.getFromNumber())
                            .callTo(callLog.getToNumber())
                            .callStatus(callLog.getStatus() != null ? callLog.getStatus().name() : null)
                            .direction(callLog.getDirection() != null ? callLog.getDirection().name() : null)
                            .eventType(callLog.getStatus() != null ? callLog.getStatus().name() : "UNKNOWN")
                            .timestamp(callLog.getCreatedAt())
                            .createdAt(callLog.getCreatedAt())
                            .leadInfos(leadInfoMap.getOrDefault(callLog.getId(), new ArrayList<>()))
                            .advisorInfos(advisorInfoMap.getOrDefault(callLog.getId(), new ArrayList<>()));
                    
                    return builder.build();
                })
                .collect(Collectors.toList());

        // Build pagination info
        PaginationInfo paginationInfo = buildPaginationInfo(paginationRequest, totalCount);
        
        return new PaginatedResponse<>(responses, paginationInfo);
    }
    
    private long getTotalCallLogCount(String primaryPhoneOriginal, String primaryPhoneNormalized) {
        try {
            String sql = "SELECT COUNT(DISTINCT cl.id) FROM n_call_log cl " +
                        "WHERE cl.from_number = ? OR cl.to_number = ? " +
                        "OR cl.from_number = ? OR cl.to_number = ?";
            
            Long count = jdbcTemplate.queryForObject(
                sql, 
                Long.class,
                primaryPhoneOriginal,
                primaryPhoneOriginal,
                primaryPhoneNormalized != null ? primaryPhoneNormalized : primaryPhoneOriginal,
                primaryPhoneNormalized != null ? primaryPhoneNormalized : primaryPhoneOriginal
            );
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to get total call log count", e);
            return 0L;
        }
    }
    
    private List<CallLog> findCallLogsByPhoneNumber(String primaryPhoneOriginal, String primaryPhoneNormalized, PaginationRequest paginationRequest) {
        try {
            String sql = "SELECT DISTINCT cl.* FROM n_call_log cl " +
                        "WHERE (cl.from_number = ? OR cl.to_number = ? " +
                        "OR cl.from_number = ? OR cl.to_number = ?) " +
                        "ORDER BY cl.created_at DESC " +
                        "LIMIT ? OFFSET ?";
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                primaryPhoneOriginal,
                primaryPhoneOriginal,
                primaryPhoneNormalized != null ? primaryPhoneNormalized : primaryPhoneOriginal,
                primaryPhoneNormalized != null ? primaryPhoneNormalized : primaryPhoneOriginal,
                paginationRequest.getLimit(),
                paginationRequest.getOffset()
            );
            
            return rows.stream()
                    .map(row -> {
                        CallLog callLog = new CallLog();
                        callLog.setId(((Number) row.get("id")).longValue());
                        callLog.setIdentifier((UUID) row.get("identifier"));
                        callLog.setProviderId((String) row.get("provider_id"));
                        callLog.setFromNumber((String) row.get("from_number"));
                        callLog.setToNumber((String) row.get("to_number"));
                        
                        String statusStr = (String) row.get("status");
                        if (statusStr != null) {
                            try {
                                callLog.setStatus(com.nivasafinance.features.call.enums.CallStatus.valueOf(statusStr));
                            } catch (IllegalArgumentException e) {
                                log.debug("Invalid call status: {}", statusStr);
                            }
                        }
                        
                        String directionStr = (String) row.get("direction");
                        if (directionStr != null) {
                            try {
                                callLog.setDirection(com.nivasafinance.features.call.enums.CallDirection.valueOf(directionStr));
                            } catch (IllegalArgumentException e) {
                                log.debug("Invalid call direction: {}", directionStr);
                            }
                        }
                        
                        if (row.get("created_at") != null) {
                            callLog.setCreatedAt(((java.sql.Timestamp) row.get("created_at")).toLocalDateTime());
                        }
                        return callLog;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to find call logs by phone number", e);
            return new ArrayList<>();
        }
    }
    
    private PaginatedResponse<EnrichedCallNotificationResponse> buildEmptyPaginatedResponse(PaginationRequest paginationRequest) {
        PaginationInfo paginationInfo = new PaginationInfo(
            paginationRequest.getOffset(),
            paginationRequest.getLimit(),
            0L,
            0,
            0,
            false,
            false
        );
        return new PaginatedResponse<>(new ArrayList<>(), paginationInfo);
    }
    
    private PaginationInfo buildPaginationInfo(PaginationRequest paginationRequest, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / paginationRequest.getLimit());
        int currentPage = paginationRequest.getOffset() / paginationRequest.getLimit();
        boolean hasNext = (paginationRequest.getOffset() + paginationRequest.getLimit()) < totalElements;
        boolean hasPrevious = paginationRequest.getOffset() > 0;
        
        return new PaginationInfo(
            paginationRequest.getOffset(),
            paginationRequest.getLimit(),
            totalElements,
            totalPages,
            currentPage,
            hasNext,
            hasPrevious
        );
    }

    private Map<Long, List<EnrichedCallNotificationResponse.LeadInfo>> findLeadsByCallLogIds(List<Long> callLogIds) {
        if (callLogIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, List<EnrichedCallNotificationResponse.LeadInfo>> leadInfoMap = new HashMap<>();
        
        try {
            String placeholders = callLogIds.stream().map(id -> "?").collect(Collectors.joining(","));
            String sql = """
                SELECT DISTINCT
                    (log_entry->>'callLogId')::bigint as call_log_id,
                    l.lead_identifier,
                    l.status as lead_status,
                    (log_entry->>'contactId')::bigint as contact_id
                FROM n_lead l,
                LATERAL jsonb_array_elements(COALESCE(l.call_logs, '[]'::jsonb)) AS log_entry
                WHERE (log_entry->>'callLogId')::bigint IN (""" + placeholders + ")";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, callLogIds.toArray());
            
            for (Map<String, Object> row : results) {
                Long callLogId = ((Number) row.get("call_log_id")).longValue();
                UUID leadIdentifier = (UUID) row.get("lead_identifier");
                String leadStatus = (String) row.get("lead_status");
                Long contactId = row.get("contact_id") != null ? ((Number) row.get("contact_id")).longValue() : null;
                
                String contactName = null;
                if (contactId != null) {
                    try {
                        String contactSql = "SELECT person_id FROM n_contact WHERE id = ?";
                        List<Map<String, Object>> contactRows = jdbcTemplate.queryForList(contactSql, contactId);
                        if (!contactRows.isEmpty()) {
                            Long personId = ((Number) contactRows.get(0).get("person_id")).longValue();
                            var person = personReadService.getPersonById(personId);
                            contactName = person.getDisplayName();
                        }
                    } catch (Exception e) {
                        log.debug("Failed to get contact name for contactId: {}", contactId, e);
                    }
                }
                
                EnrichedCallNotificationResponse.LeadInfo leadInfo = 
                    EnrichedCallNotificationResponse.LeadInfo.builder()
                        .leadIdentifier(leadIdentifier)
                        .leadStatus(leadStatus)
                        .contactId(contactId)
                        .contactName(contactName)
                        .build();
                
                leadInfoMap.computeIfAbsent(callLogId, k -> new ArrayList<>()).add(leadInfo);
            }
        } catch (Exception e) {
            log.error("Failed to find leads by call log IDs", e);
        }
        
        return leadInfoMap;
    }

    private Map<Long, List<EnrichedCallNotificationResponse.AdvisorInfo>> findAdvisorsByCallLogIds(List<Long> callLogIds) {
        if (callLogIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, List<EnrichedCallNotificationResponse.AdvisorInfo>> advisorInfoMap = new HashMap<>();
        
        try {
            String placeholders = callLogIds.stream().map(id -> "?").collect(Collectors.joining(","));
            String sql = """
                SELECT DISTINCT
                    (log_entry->>'callLogId')::bigint as call_log_id,
                    a.identifier as advisor_identifier,
                    a.status as advisor_status
                FROM n_advisor a,
                LATERAL jsonb_array_elements(COALESCE(a.call_logs, '[]'::jsonb)) AS log_entry
                WHERE (log_entry->>'callLogId')::bigint IN (""" + placeholders + ")";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, callLogIds.toArray());
            
            for (Map<String, Object> row : results) {
                Long callLogId = ((Number) row.get("call_log_id")).longValue();
                UUID advisorIdentifier = (UUID) row.get("advisor_identifier");
                String advisorStatus = (String) row.get("advisor_status");
                
                EnrichedCallNotificationResponse.AdvisorInfo advisorInfo = 
                    EnrichedCallNotificationResponse.AdvisorInfo.builder()
                        .advisorIdentifier(advisorIdentifier)
                        .advisorStatus(advisorStatus)
                        .build();
                
                advisorInfoMap.computeIfAbsent(callLogId, k -> new ArrayList<>()).add(advisorInfo);
            }
        } catch (Exception e) {
            log.error("Failed to find advisors by call log IDs", e);
        }
        
        return advisorInfoMap;
    }
}

