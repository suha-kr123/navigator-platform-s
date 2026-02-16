package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.common.dto.EnrichedCallNotificationResponse;
import com.nivasafinance.common.utils.PhoneNumberUtils;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.repository.CallNotificationRedisRepository;
import com.nivasafinance.features.call.service.CallNotificationSseService;
import com.nivasafinance.features.call.service.CallNotificationService;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
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
    private final CallNotificationRedisRepository redisRepository;
    private final CallNotificationSseService sseService;

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
            // Extract last 10 digits from user's phone for comparison (India numbers are 10 digits)
            String userPhoneLast10 = extractLast10Digits(primaryPhoneOriginal);
            String normalizedLast10 = primaryPhoneNormalized != null ? extractLast10Digits(primaryPhoneNormalized) : userPhoneLast10;
            
            // For OUTBOUND calls: user's number should match from_number (we're making the call)
            // For INBOUND calls: user's number should match to_number (call is coming to us)
            // Compare last 10 digits to handle different formats (with/without country code)
            String sql = "SELECT COUNT(DISTINCT cl.id) FROM n_call_log cl " +
                        "WHERE (cl.direction = 'OUTBOUND' AND (" +
                        "    RIGHT(REGEXP_REPLACE(cl.from_number, '[^0-9]', '', 'g'), 10) = ? " +
                        "    OR RIGHT(REGEXP_REPLACE(cl.from_number, '[^0-9]', '', 'g'), 10) = ?)) " +
                        "OR (cl.direction = 'INBOUND' AND (" +
                        "    RIGHT(REGEXP_REPLACE(cl.to_number, '[^0-9]', '', 'g'), 10) = ? " +
                        "    OR RIGHT(REGEXP_REPLACE(cl.to_number, '[^0-9]', '', 'g'), 10) = ?))";
            
            Long count = jdbcTemplate.queryForObject(
                sql, 
                Long.class,
                userPhoneLast10,
                normalizedLast10,
                userPhoneLast10,
                normalizedLast10
            );
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to get total call log count", e);
            return 0L;
        }
    }
    
    private List<CallLog> findCallLogsByPhoneNumber(String primaryPhoneOriginal, String primaryPhoneNormalized, PaginationRequest paginationRequest) {
        try {
            // Extract last 10 digits from user's phone for comparison (India numbers are 10 digits)
            String userPhoneLast10 = extractLast10Digits(primaryPhoneOriginal);
            String normalizedLast10 = primaryPhoneNormalized != null ? extractLast10Digits(primaryPhoneNormalized) : userPhoneLast10;
            
            // For OUTBOUND calls: user's number should match from_number (we're making the call)
            // For INBOUND calls: user's number should match to_number (call is coming to us)
            // Compare last 10 digits to handle different formats (with/without country code)
            String sql = "SELECT DISTINCT cl.* FROM n_call_log cl " +
                        "WHERE (cl.direction = 'OUTBOUND' AND (" +
                        "    RIGHT(REGEXP_REPLACE(cl.from_number, '[^0-9]', '', 'g'), 10) = ? " +
                        "    OR RIGHT(REGEXP_REPLACE(cl.from_number, '[^0-9]', '', 'g'), 10) = ?)) " +
                        "OR (cl.direction = 'INBOUND' AND (" +
                        "    RIGHT(REGEXP_REPLACE(cl.to_number, '[^0-9]', '', 'g'), 10) = ? " +
                        "    OR RIGHT(REGEXP_REPLACE(cl.to_number, '[^0-9]', '', 'g'), 10) = ?)) " +
                        "ORDER BY cl.created_at DESC " +
                        "LIMIT ? OFFSET ?";
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                userPhoneLast10,
                normalizedLast10,
                userPhoneLast10,
                normalizedLast10,
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
            // Find leads where the call log phone number matches a contact's phone number
            // For INCOMING calls: check from_number (caller is the lead contact)
            // For OUTGOING calls: check to_number (we're calling the lead contact)
            String placeholders = callLogIds.stream().map(id -> "?").collect(Collectors.joining(","));
            String sql = "SELECT DISTINCT " +
                    "cl.id as call_log_id, " +
                    "cl.direction as call_direction, " +
                    "l.lead_identifier, " +
                    "l.status as lead_status, " +
                    "(log_entry->>'contactId')::bigint as contact_id, " +
                    "matching_contact.id as matching_contact_id " +
                    "FROM n_call_log cl " +
                    "JOIN n_lead l ON EXISTS ( " +
                    "    SELECT 1 FROM jsonb_array_elements(COALESCE(l.call_logs, '[]'::jsonb)) AS log_entry " +
                    "    WHERE (log_entry->>'callLogId')::bigint = cl.id " +
                    ") " +
                    "LEFT JOIN LATERAL ( " +
                    "    SELECT (contact_id)::bigint as id " +
                    "    FROM jsonb_array_elements_text(COALESCE(l.contacts, '[]'::jsonb)) AS contact_id " +
                    ") contact_ids ON true " +
                    "LEFT JOIN n_contact matching_contact ON matching_contact.id = contact_ids.id " +
                    "LEFT JOIN n_person matching_person ON matching_person.id = matching_contact.person_id " +
                    "LEFT JOIN LATERAL jsonb_array_elements(COALESCE(l.call_logs, '[]'::jsonb)) AS log_entry ON " +
                    "    (log_entry->>'callLogId')::bigint = cl.id " +
                    "WHERE cl.id IN (" + placeholders + ") " +
                    "AND EXISTS ( " +
                    "    SELECT 1 FROM jsonb_array_elements(COALESCE(matching_person.mobile_numbers, '[]'::jsonb)) AS m " +
                    "    WHERE (cl.direction = 'INBOUND' AND " +
                    "        RIGHT(REGEXP_REPLACE(m->>'number', '[^0-9]', '', 'g'), 10) = " +
                    "        RIGHT(REGEXP_REPLACE(cl.from_number, '[^0-9]', '', 'g'), 10)) " +
                    "       OR (cl.direction = 'OUTBOUND' AND " +
                    "        RIGHT(REGEXP_REPLACE(m->>'number', '[^0-9]', '', 'g'), 10) = " +
                    "        RIGHT(REGEXP_REPLACE(cl.to_number, '[^0-9]', '', 'g'), 10)) " +
                    ")";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, callLogIds.toArray());
            
            for (Map<String, Object> row : results) {
                Long callLogId = ((Number) row.get("call_log_id")).longValue();
                UUID leadIdentifier = (UUID) row.get("lead_identifier");
                String leadStatus = (String) row.get("lead_status");
                Long contactId = row.get("contact_id") != null ? ((Number) row.get("contact_id")).longValue() : null;
                Long matchingContactId = row.get("matching_contact_id") != null ? ((Number) row.get("matching_contact_id")).longValue() : null;
                
                // Use matching contact ID if available, otherwise use the one from call_logs JSONB
                Long finalContactId = matchingContactId != null ? matchingContactId : contactId;
                
                String contactName = null;
                if (finalContactId != null) {
                    try {
                        String contactSql = "SELECT person_id FROM n_contact WHERE id = ?";
                        List<Map<String, Object>> contactRows = jdbcTemplate.queryForList(contactSql, finalContactId);
                        if (!contactRows.isEmpty()) {
                            Long personId = ((Number) contactRows.get(0).get("person_id")).longValue();
                            var person = personReadService.getPersonById(personId);
                            contactName = person.getDisplayName();
                        }
                    } catch (Exception e) {
                        log.debug("Failed to get contact name for contactId: {}", finalContactId, e);
                    }
                }
                
                EnrichedCallNotificationResponse.LeadInfo leadInfo = 
                    EnrichedCallNotificationResponse.LeadInfo.builder()
                        .leadIdentifier(leadIdentifier)
                        .leadStatus(leadStatus)
                        .contactId(finalContactId)
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
            // Find advisors where the call log phone number matches the advisor's phone number
            // For INBOUND calls: check from_number (caller is the advisor)
            // For OUTBOUND calls: check to_number (we're calling the advisor)
            String placeholders = callLogIds.stream().map(id -> "?").collect(Collectors.joining(","));
            String sql = "SELECT DISTINCT " +
                    "cl.id as call_log_id, " +
                    "cl.direction as call_direction, " +
                    "a.identifier as advisor_identifier, " +
                    "a.status as advisor_status, " +
                    "advisor_person.display_name as advisor_name " +
                    "FROM n_call_log cl " +
                    "JOIN n_advisor a ON EXISTS ( " +
                    "    SELECT 1 FROM jsonb_array_elements(COALESCE(a.call_logs, '[]'::jsonb)) AS log_entry " +
                    "    WHERE (log_entry->>'callLogId')::bigint = cl.id " +
                    ") " +
                    "LEFT JOIN n_person advisor_person ON advisor_person.id = a.person_id " +
                    "WHERE cl.id IN (" + placeholders + ") " +
                    "AND EXISTS ( " +
                    "    SELECT 1 FROM jsonb_array_elements(COALESCE(advisor_person.mobile_numbers, '[]'::jsonb)) AS m " +
                    "    WHERE (cl.direction = 'INBOUND' AND " +
                    "        RIGHT(REGEXP_REPLACE(m->>'number', '[^0-9]', '', 'g'), 10) = " +
                    "        RIGHT(REGEXP_REPLACE(cl.from_number, '[^0-9]', '', 'g'), 10)) " +
                    "       OR (cl.direction = 'OUTBOUND' AND " +
                    "        RIGHT(REGEXP_REPLACE(m->>'number', '[^0-9]', '', 'g'), 10) = " +
                    "        RIGHT(REGEXP_REPLACE(cl.to_number, '[^0-9]', '', 'g'), 10)) " +
                    ")";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, callLogIds.toArray());
            
            for (Map<String, Object> row : results) {
                Long callLogId = ((Number) row.get("call_log_id")).longValue();
                UUID advisorIdentifier = (UUID) row.get("advisor_identifier");
                String advisorStatus = (String) row.get("advisor_status");
                String advisorName = (String) row.get("advisor_name");
                
                EnrichedCallNotificationResponse.AdvisorInfo advisorInfo = 
                    EnrichedCallNotificationResponse.AdvisorInfo.builder()
                        .advisorIdentifier(advisorIdentifier)
                        .advisorStatus(advisorStatus)
                        .advisorName(advisorName)
                        .build();
                
                advisorInfoMap.computeIfAbsent(callLogId, k -> new ArrayList<>()).add(advisorInfo);
            }
        } catch (Exception e) {
            log.error("Failed to find advisors by call log IDs", e);
        }
        
        return advisorInfoMap;
    }
    
    /**
     * Extract last 10 digits from phone number (removes country code, formatting, etc.)
     * This ensures consistent matching regardless of how phone numbers are stored.
     */
    private String extractLast10Digits(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        // Remove all non-digit characters and take last 10 digits
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() >= 10) {
            return digitsOnly.substring(digitsOnly.length() - 10);
        }
        return digitsOnly;
    }

    /**
     * Get the most recent call notification from Redis for the current user (for reconnection scenarios).
     * 
     * Fetches the most recent notification that is immediately available in Redis. Notifications are saved to Redis
     * instantaneously when received, while database writes happen asynchronously later. This method
     * is specifically designed for catching missed notifications when SSE reconnects after a disconnect.
     * 
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Client disconnects from SSE (network issue, stream ended, etc.)</li>
     *   <li>Notifications are sent to Redis immediately during the disconnect period</li>
     *   <li>Client reconnects and fetches from this method</li>
     *   <li>DB writes may not have completed yet (async write with delay)</li>
     *   <li>Fetching from DB would miss these notifications, but Redis has them instantly</li>
     * </ul>
     * 
     * <p><strong>Important:</strong> This method should only be called on SSE reconnection, not as
     * a primary notification source. Use the SSE stream for real-time delivery.</p>
     * 
     * <p><strong>Note:</strong> Returns only the most recent notification. Older notifications will be
     * available in the database and can be fetched from there.</p>
     * 
     * <p><strong>Limitations:</strong> Redis has a TTL of 24 hours and limited count per user (default: 5).
     * This is a temporary buffer for missed notifications during disconnection, not a long-term storage solution.</p>
     * 
     * @return Optional containing the most recent notification from Redis, or empty if none found
     */
    @Override
    public CallNotificationResponse getRecentNotificationsFromRedis() {
        String username = UserContext.getUsername();
        if (username == null || username.isBlank()) {
            log.debug("Missing username in UserContext - returning empty");
            return null;
        }

        Optional<User> userOpt = userReadService.findUserByUsername(username);
        if (userOpt.isEmpty() || userOpt.get().getPerson() == null) {
            log.debug("User not found or has no person associated: {}", username);
            return null;
        }

        User user = userOpt.get();
        
        // Get user's primary phone number - try both normalized and original formats
        // Following the same structure as getNotificationsForCurrentUser
        String primaryPhoneOriginal = user.getPerson().getMobileNumbers().stream()
                .filter(mobile -> mobile.getIsPrimary() != null && mobile.getIsPrimary())
                .filter(mobile -> mobile.getNumber() != null && !mobile.getNumber().isBlank())
                .map(mobile -> mobile.getNumber())
                .findFirst()
                .orElse(null);

        if (primaryPhoneOriginal == null) {
            log.debug("No primary phone number found for user: {}", username);
            return null;
        }

        String primaryPhoneNormalized = PhoneNumberUtils.normalizePhoneNumber(primaryPhoneOriginal);
        
        // Fetch from Redis - try both original and normalized formats
        // Notifications are stored in Redis by phone number (DialWhomNumber or callTo)
        List<CallNotificationResponse> notifications = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>(); // Track duplicates by callSid + eventType
        
        // Try original phone number first
        try {
            List<CallNotificationResponse> phoneNotifications = redisRepository.findByUserPhone(primaryPhoneOriginal);
            for (CallNotificationResponse notification : phoneNotifications) {
                String key = notification.getCallSid() + ":" + 
                            (notification.getEventType() != null ? notification.getEventType() : "UNKNOWN");
                if (!seenKeys.contains(key)) {
                    seenKeys.add(key);
                    notifications.add(notification);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch notifications from Redis by original phone for user: {}", username, e);
        }
        
        // Try normalized phone number (might match if stored differently)
        if (primaryPhoneNormalized != null && !primaryPhoneNormalized.equals(primaryPhoneOriginal)) {
            try {
                List<CallNotificationResponse> normalizedNotifications = redisRepository.findByUserPhone(primaryPhoneNormalized);
                for (CallNotificationResponse notification : normalizedNotifications) {
                    String key = notification.getCallSid() + ":" + 
                                (notification.getEventType() != null ? notification.getEventType() : "UNKNOWN");
                    if (!seenKeys.contains(key)) {
                        seenKeys.add(key);
                        notifications.add(notification);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch notifications from Redis by normalized phone for user: {}", username, e);
            }
        }

        if (notifications.isEmpty()) {
            log.debug("No notifications found in Redis for user: {}", username);
            return null;
        }

        // Sort by createdAt descending (most recent first)
        notifications.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) {
                return 0;
            }
            if (a.getCreatedAt() == null) {
                return 1; // nulls last
            }
            if (b.getCreatedAt() == null) {
                return -1; // nulls last
            }
            return b.getCreatedAt().compareTo(a.getCreatedAt()); // descending order
        });

        // Return only the most recent notification (last 1 call)
        // Others will be stored in the database and can be fetched from there
        CallNotificationResponse mostRecent = notifications.get(0);
        log.info("Returning most recent notification from Redis for user: {} (callSid: {}, total in Redis: {})", 
                username, mostRecent.getCallSid(), notifications.size());
        
        return mostRecent;
    }

    /* === send notification async to user via SSE === */
    @Override
    @Async
    public void sendNotificationAsync(CallNotificationResponse notification, String userPhone) {
        try {
            // normalize phone number to 10 digits
            String normalizedUserPhone = extractLast10Digits(userPhone);
            log.info("=== SSE NOTIFICATION FLOW START ===");
            log.info("Looking up users for phone: {}, callSid: {}", userPhone, notification.getCallSid());
            
            List<User> users = userReadService.findUsersByPersonPhoneNumber(normalizedUserPhone);
            log.info("Found {} users for phone: {}, callSid: {}", users.size(), userPhone, notification.getCallSid());
            
            if (users.isEmpty()) {
                log.warn("⚠️ No users found for phone number: {}, notification will not be sent via SSE. CallSid: {}", 
                        normalizedUserPhone, notification.getCallSid());
                return;
            }
            
            users.stream()
                    .map(user -> {
                        log.info("✓ Found user: {} (username: {}) for phone: {}, callSid: {}", 
                                user.getId(), user.getUsername(), userPhone, notification.getCallSid());
                        return user.getUsername();
                    })
                    .forEach(username -> {
                        log.info("→ Attempting to send SSE notification to username: {} for call: {}", 
                                username, notification.getCallSid());
                        try {
                            sseService.sendNotificationToUser(notification, username);
                            log.info("✓ Successfully sent SSE notification to username: {}", username);
                        } catch (Exception e) {
                            log.error("✗ Failed to send SSE notification to user: {}", username, e);
                        }
                    });
            log.info("=== SSE NOTIFICATION FLOW END ===");
        } catch (Exception e) {
            log.error("✗ Failed to process async notification for phone: {}, callSid: {}", 
                    userPhone, notification.getCallSid(), e);
        }
    }
   
}

