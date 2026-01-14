package com.nivasafinance.webhooks.call.service.impl;

import com.nivasafinance.common.utils.PhoneNumberUtils;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.call.service.CallNotificationSseService;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.features.call.repository.CallNotificationRedisRepository;
import com.nivasafinance.webhooks.call.service.CallWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallWebhookServiceImpl implements CallWebhookService {

    private final CallNotificationRedisRepository notificationRepository;
    private final CallNotificationSseService sseService;
    private final UserReadService userReadService;

    @Autowired
    private ApplicationContext applicationContext;

    private CallWebhookService getSelf() {
        return applicationContext.getBean(CallWebhookService.class);
    }

    @Override
    public Map<String, Object> handleWebhook(MultiValueMap<String, String> formData) {
        List<String> errors = validateWebhookData(formData);
        if (!errors.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid webhook data");
            response.put("errors", errors);
            return response;
        }
        
        try {
            processWebhook(formData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Webhook received");
            return response;
        } catch (Exception ex) {
            log.error("Error processing call webhook", ex);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error processing webhook: " + ex.getMessage());
            return response;
        }
    }

    @Override
    @Async
    public void sendNotificationAsync(CallNotificationResponse notification, String userPhone) {
        try {
            log.info("=== SSE NOTIFICATION FLOW START ===");
            log.info("Looking up users for phone: {}, callSid: {}", userPhone, notification.getCallSid());
            
            List<User> users = userReadService.findUsersByPersonPhoneNumber(userPhone);
            log.info("Found {} users for phone: {}, callSid: {}", users.size(), userPhone, notification.getCallSid());
            
            if (users.isEmpty()) {
                log.warn("⚠️ No users found for phone number: {}, notification will not be sent via SSE. CallSid: {}", 
                        userPhone, notification.getCallSid());
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

    private void processWebhook(MultiValueMap<String, String> formData) {
        String callSid = formData.getFirst("CallSid");
        if (callSid == null || callSid.isBlank()) {
            log.warn("Webhook payload missing CallSid, skipping processing");
            return;
        }
        
        String callFrom = formData.getFirst("CallFrom");
        String callTo = formData.getFirst("CallTo");
        String dialWhomNumber = formData.getFirst("DialWhomNumber");
        String callStatus = formData.getFirst("CallStatus");
        String direction = normalizeDirection(formData.getFirst("Direction"));
        String eventType = formData.getFirst("EventType");
        String agentEmail = formData.getFirst("AgentEmail");
        
        if (eventType == null || eventType.isBlank()) {
            eventType = callStatus != null && !callStatus.isBlank() ? callStatus : "UNKNOWN";
        }
        
        if (notificationRepository.existsByCallSidAndEventType(callSid, eventType)) {
            return;
        }
        
        LocalDateTime timestamp = parseTimestamp(formData);
        LocalDateTime now = LocalDateTime.now();
        
        CallNotificationResponse notification = CallNotificationResponse.builder()
                .callSid(callSid)
                .callFrom(callFrom)
                .callTo(callTo)
                .callStatus(callStatus)
                .direction(direction)
                .eventType(eventType)
                .agentEmail(agentEmail)
                .timestamp(timestamp)
                .createdAt(now)
                .build();
        
        notificationRepository.save(notification, dialWhomNumber);
        log.info("💾 Notification saved to Redis for callSid: {}, dialWhomNumber: {}", callSid, dialWhomNumber);
        
        // Use DialWhomNumber (the agent number) for notifications
        // Fallback to callTo if DialWhomNumber is not provided
        String userPhone = (dialWhomNumber != null && !dialWhomNumber.isBlank()) 
                ? dialWhomNumber 
                : callTo;
        
        log.info("📞 Processing webhook notification. CallSid: {}, DialWhomNumber: {}, CallTo: {}, userPhone: {}", 
                callSid, dialWhomNumber, callTo, userPhone);
        
        if (userPhone != null && !userPhone.isBlank()) {
            // Normalize phone number before lookup
            String normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(userPhone);
            if (normalizedPhone != null && !normalizedPhone.isBlank()) {
                log.info("🚀 Calling sendNotificationAsync for phone: {} (normalized: {}), callSid: {}", 
                        userPhone, normalizedPhone, callSid);
                getSelf().sendNotificationAsync(notification, normalizedPhone);
                log.info("✅ sendNotificationAsync called for phone: {}, callSid: {}", 
                        normalizedPhone, callSid);
            } else {
                log.warn("Failed to normalize phone number: {}, skipping notification", userPhone);
            }
        } else {
            log.warn("userPhone is null or blank, skipping SSE notification. CallSid: {}", callSid);
        }
    }

    private List<String> validateWebhookData(MultiValueMap<String, String> formData) {
        List<String> errors = new ArrayList<>();
        
        if (!formData.containsKey("CallSid") || formData.getFirst("CallSid") == null) {
            errors.add("Missing required parameter: CallSid");
        }
        if (!formData.containsKey("CallFrom") || formData.getFirst("CallFrom") == null) {
            errors.add("Missing required parameter: CallFrom");
        }
        if (!formData.containsKey("CallTo") || formData.getFirst("CallTo") == null) {
            errors.add("Missing required parameter: CallTo");
        }
        if (!formData.containsKey("CallStatus") && !formData.containsKey("Status")) {
            errors.add("Missing required parameter: CallStatus or Status");
        }
        if (!formData.containsKey("Direction") || formData.getFirst("Direction") == null) {
            errors.add("Missing required parameter: Direction");
        }
        
        return errors;
    }

    private String normalizeDirection(String direction) {
        if (direction == null) {
            return null;
        }
        return switch (direction.toUpperCase()) {
            case "INCOMING", "INBOUND" -> "INBOUND";
            case "OUTBOUND-DIAL", "OUTBOUND" -> "OUTBOUND";
            default -> direction.toUpperCase();
        };
    }

    private LocalDateTime parseTimestamp(MultiValueMap<String, String> formData) {
        String timestampStr = formData.getFirst("Timestamp");
        if (timestampStr != null && !timestampStr.isBlank()) {
            try {
                return LocalDateTime.parse(timestampStr);
            } catch (Exception e) {
                log.warn("Failed to parse timestamp: {}", timestampStr);
            }
        }
        
        String created = formData.getFirst("Created");
        if (created != null && !created.isBlank()) {
            try {
                return LocalDateTime.parse(created);
            } catch (Exception e) {
                log.warn("Failed to parse created timestamp: {}", created);
            }
        }
        
        return LocalDateTime.now();
    }
}

