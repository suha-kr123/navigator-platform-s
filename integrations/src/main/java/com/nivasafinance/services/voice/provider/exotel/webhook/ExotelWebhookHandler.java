package com.nivasafinance.services.voice.provider.exotel.webhook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.usermanagement.repository.UserRepositoryWrapper;
import com.nivasafinance.services.voice.webhook.CallNotificationWebSocketService;
import com.nivasafinance.services.voice.webhook.VoiceWebhookHandler;
import com.nivasafinance.services.voice.webhook.dto.CallNotificationResponse;
import com.nivasafinance.services.voice.provider.exotel.webhook.ExotelWebhookPayload;
import com.nivasafinance.services.voice.webhook.repository.CallNotificationRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExotelWebhookHandler implements VoiceWebhookHandler {

    private final CallNotificationRedisRepository notificationRepository;
    private final CallNotificationWebSocketService webSocketService;
    private final UserRepositoryWrapper userRepositoryWrapper;
    private final ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    private ExotelWebhookHandler getSelf() {
        return applicationContext.getBean(ExotelWebhookHandler.class);
    }

    private static final DateTimeFormatter EXOTEL_DATE_FORMAT = 
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss");
    private static final DateTimeFormatter EXOTEL_TIME_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getProviderName() {
        return "EXOTEL";
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
            ExotelWebhookPayload payload = mapFormDataToPayload(formData);
            processWebhook(payload);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Webhook received");
            return response;
        } catch (Exception ex) {
            log.error("Error processing Exotel webhook", ex);
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
            userRepositoryWrapper.findByPersonPhoneNumber(userPhone).stream()
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

    private void processWebhook(ExotelWebhookPayload payload) {
        if (payload.getCallSid() == null || payload.getCallSid().isBlank()) {
            log.warn("Webhook payload missing CallSid, skipping processing");
            return;
        }
        
        String normalizedDirection = normalizeDirection(payload.getDirection());
        String agentEmail = extractAgentEmail(payload);
        
        String eventType = payload.getEventType() != null && !payload.getEventType().isBlank()
                ? payload.getEventType()
                : (payload.getCallStatus() != null && !payload.getCallStatus().isBlank()
                        ? payload.getCallStatus()
                        : "UNKNOWN");
        
        if (notificationRepository.existsByCallSidAndEventType(payload.getCallSid(), eventType)) {
            return;
        }
        
        LocalDateTime webhookTimestamp = parseTimestamp(payload);
        LocalDateTime now = LocalDateTime.now();
        
        CallNotificationResponse notification = CallNotificationResponse.builder()
                .callSid(payload.getCallSid())
                .callFrom(payload.getCallFrom() != null ? payload.getCallFrom() : payload.getFrom())
                .callTo(payload.getCallTo() != null ? payload.getCallTo() : payload.getTo())
                .callStatus(payload.getCallStatus() != null ? payload.getCallStatus() : payload.getStatus())
                .direction(normalizedDirection)
                .eventType(eventType)
                .agentEmail(agentEmail)
                .timestamp(webhookTimestamp)
                .createdAt(now)
                .build();
        
        notificationRepository.save(notification);
        
        String userPhone = normalizedDirection != null && "INBOUND".equals(normalizedDirection)
                ? (payload.getCallFrom() != null ? payload.getCallFrom() : payload.getFrom())
                : (payload.getCallTo() != null ? payload.getCallTo() : payload.getTo());
        
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

    private ExotelWebhookPayload mapFormDataToPayload(MultiValueMap<String, String> formData) {
        return ExotelWebhookPayload.builder()
                .callSid(formData.getFirst("CallSid"))
                .callFrom(formData.getFirst("CallFrom"))
                .callTo(formData.getFirst("CallTo"))
                .callStatus(formData.getFirst("CallStatus"))
                .status(formData.getFirst("Status"))
                .direction(formData.getFirst("Direction"))
                .created(formData.getFirst("Created"))
                .from(formData.getFirst("From"))
                .to(formData.getFirst("To"))
                .currentTime(formData.getFirst("CurrentTime"))
                .dialWhomNumber(formData.getFirst("DialWhomNumber"))
                .eventType(formData.getFirst("EventType"))
                .customField(formData.getFirst("CustomField"))
                .agentEmail(formData.getFirst("AgentEmail"))
                .build();
    }

    private String normalizeDirection(String direction) {
        if (direction == null) {
            return null;
        }
        return switch (direction.toLowerCase()) {
            case "incoming" -> "INBOUND";
            case "outbound-dial", "outbound" -> "OUTBOUND";
            default -> direction.toUpperCase();
        };
    }

    private String extractAgentEmail(ExotelWebhookPayload payload) {
        if (payload.getAgentEmail() != null && !payload.getAgentEmail().isBlank()) {
            return payload.getAgentEmail();
        }
        
        if (payload.getCustomField() != null && !payload.getCustomField().isBlank()) {
            try {
                Map<String, Object> customData = objectMapper.readValue(
                        payload.getCustomField(), 
                        new TypeReference<Map<String, Object>>() {});
                if (customData.containsKey("AgentEmail")) {
                    return customData.get("AgentEmail").toString();
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        return null;
    }

    private LocalDateTime parseTimestamp(ExotelWebhookPayload payload) {
        if (payload.getCreated() != null && !payload.getCreated().isBlank()) {
            try {
                return LocalDateTime.parse(payload.getCreated(), EXOTEL_DATE_FORMAT);
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        if (payload.getCurrentTime() != null && !payload.getCurrentTime().isBlank()) {
            try {
                return LocalDateTime.parse(payload.getCurrentTime(), EXOTEL_TIME_FORMAT);
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        return LocalDateTime.now();
    }
}

