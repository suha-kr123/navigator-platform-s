package com.nivasafinance.webhooks.call.service.impl;

import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.webhooks.call.service.CallNotificationWebSocketService;
import com.nivasafinance.webhooks.call.dto.CallNotificationResponse;
import com.nivasafinance.webhooks.call.repository.CallNotificationRedisRepository;
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
    private final CallNotificationWebSocketService webSocketService;
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
            userReadService.findUsersByPersonPhoneNumber(userPhone).stream()
                    .map(user -> user.getUsername())
                    .forEach(username -> {
                        try {
                            webSocketService.sendNotificationToUser(notification, username);
                        } catch (Exception e) {
                            log.error("Failed to send WebSocket notification to user: {}", username, e);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to process async notification for phone: {}", userPhone, e);
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
        
        notificationRepository.save(notification);
        
        String userPhone = direction != null && "INBOUND".equals(direction)
                ? callFrom
                : callTo;
        
        if (userPhone != null && !userPhone.isBlank()) {
            getSelf().sendNotificationAsync(notification, userPhone);
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

