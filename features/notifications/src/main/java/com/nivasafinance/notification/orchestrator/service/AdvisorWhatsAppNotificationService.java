package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import com.nivasafinance.notification.orchestrator.dto.WatiWebhookPayload;
import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.enums.WhatsAppMessageStatus;
import com.nivasafinance.notification.orchestrator.repository.AdvisorWhatsAppNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing advisor WhatsApp notification tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvisorWhatsAppNotificationService {

    private final AdvisorWhatsAppNotificationRepository repository;

    /**
     * Creates a notification record from WATI send template response.
     * Extracts advisorIdentifier from notification receipt details or message payload.
     */
    @Transactional
    public AdvisorWhatsAppNotification createFromWatiResponse(
            NotificationReceipt receipt,
            WatiSendTemplateResponse watiResponse,
            String templateName) {
        
        if (watiResponse == null || watiResponse.getReceivers() == null || watiResponse.getReceivers().isEmpty()) {
            throw new IllegalStateException("WATI response is empty or has no receivers");
        }
        
        WatiSendTemplateResponse.Receiver receiver = watiResponse.getReceivers().get(0);
        
        // Extract advisorIdentifier (UUID) from receipt details or message payload
        UUID advisorIdentifier = extractAdvisorId(receipt);
        if (advisorIdentifier == null) {
            throw new IllegalStateException("AdvisorIdentifier not found in receipt details or message payload");
        }
        
        AdvisorWhatsAppNotification notification = AdvisorWhatsAppNotification.builder()
                .advisorIdentifier(advisorIdentifier)
                .receiptId(receipt.getId())
                .notificationRecordId(receipt.getNotificationRecordId())
                .templateName(templateName != null ? templateName : watiResponse.getTemplateName())
                .templateId(templateName) // Using template name as template ID
                .localMessageId(receiver.getLocalMessageId())
                .waId(receiver.getWaId())
                .status(WhatsAppMessageStatus.SENT)
                .isReplied(false)
                .isValidWhatsAppNumber(receiver.getIsValidWhatsAppNumber())
                .sentTimestamp(Instant.now())
                .build();
        
        notification.setCreatedBy("system");
        notification.setUpdatedBy("system");
        
        AdvisorWhatsAppNotification saved = repository.save(notification);
        log.info("Created advisor WhatsApp notification record: id={}, advisorIdentifier={}, localMessageId={}",
                saved.getId(), saved.getAdvisorIdentifier(), saved.getLocalMessageId());
        
        return saved;
    }
    
    /**
     * Creates a notification record from Gallabox send template response.
     * Extracts advisorIdentifier from notification receipt details or message payload.
     */
    @Transactional
    public AdvisorWhatsAppNotification createFromGallaboxResponse(
            NotificationReceipt receipt,
            String messageId,
            String status,
            String templateName,
            String phoneNumber) {
        
        // Extract advisorIdentifier (UUID) from receipt details or message payload
        UUID advisorIdentifier = extractAdvisorId(receipt);
        if (advisorIdentifier == null) {
            throw new IllegalStateException("AdvisorIdentifier not found in receipt details or message payload");
        }
        
        // Use messageId as localMessageId for Gallabox (or generate one if not available)
        String localMessageId = messageId != null ? messageId : java.util.UUID.randomUUID().toString();
        
        AdvisorWhatsAppNotification notification = AdvisorWhatsAppNotification.builder()
                .advisorIdentifier(advisorIdentifier)
                .receiptId(receipt.getId())
                .notificationRecordId(receipt.getNotificationRecordId())
                .templateName(templateName)
                .templateId(templateName) // Using template name as template ID
                .localMessageId(localMessageId)
                .whatsappMessageId(messageId) // Gallabox messageId goes here
                .waId(phoneNumber != null ? phoneNumber.replace("+", "") : null)
                .status("sent".equalsIgnoreCase(status) ? WhatsAppMessageStatus.SENT : WhatsAppMessageStatus.FAILED)
                .isReplied(false)
                .sentTimestamp(Instant.now())
                .build();
        
        notification.setCreatedBy("system");
        notification.setUpdatedBy("system");
        
        AdvisorWhatsAppNotification saved = repository.save(notification);
        log.info("Created advisor WhatsApp notification record from Gallabox: id={}, advisorIdentifier={}, messageId={}",
                saved.getId(), saved.getAdvisorIdentifier(), saved.getWhatsappMessageId());
        
        return saved;
    }
    
    /**
     * Updates notification status from WATI webhook.
     */
    @Transactional
    public void updateFromWebhook(WatiWebhookPayload webhook) {
        if (webhook.getLocalMessageId() == null) {
            log.warn("Webhook missing localMessageId, cannot update notification");
            return;
        }
        
        Optional<AdvisorWhatsAppNotification> notificationOpt = repository.findByLocalMessageId(webhook.getLocalMessageId());
        if (notificationOpt.isEmpty()) {
            log.warn("No advisor WhatsApp notification found for localMessageId: {}", webhook.getLocalMessageId());
            return;
        }
        
        AdvisorWhatsAppNotification notification = notificationOpt.get();
        boolean updated = false;
        
        // Update status based on event type
        String eventType = webhook.getEventType();
        if (eventType != null) {
            if (eventType.contains("DELIVERED")) {
                notification.setStatus(WhatsAppMessageStatus.DELIVERED);
                notification.setDeliveredTimestamp(parseTimestamp(webhook.getTimestamp()));
                updated = true;
            } else if (eventType.contains("REPLIED")) {
                notification.setStatus(WhatsAppMessageStatus.REPLIED);
                notification.setIsReplied(true);
                notification.setReplyTimestamp(parseTimestamp(webhook.getTimestamp()));
                // Reply text will be updated from the message event
                updated = true;
            } else if (eventType.equals("message") && webhook.getReplyContextId() != null) {
                // This is the actual reply message
                // Check if this message is a reply to our template
                if (notification.getWhatsappMessageId() != null && 
                    webhook.getReplyContextId().equals(notification.getWhatsappMessageId())) {
                    notification.setReplyText(webhook.getText());
                    notification.setReplyTimestamp(parseTimestamp(webhook.getTimestamp()));
                    updated = true;
                }
            }
        }
        
        // Update WhatsApp message ID if available
        if (webhook.getWhatsappMessageId() != null && notification.getWhatsappMessageId() == null) {
            notification.setWhatsappMessageId(webhook.getWhatsappMessageId());
            updated = true;
        }
        
        // Update conversation and ticket IDs
        if (webhook.getConversationId() != null) {
            notification.setConversationId(webhook.getConversationId());
            updated = true;
        }
        if (webhook.getTicketId() != null) {
            notification.setTicketId(webhook.getTicketId());
            updated = true;
        }
        
        if (updated) {
            notification.setUpdatedBy("system");
            repository.save(notification);
            log.info("Updated advisor WhatsApp notification: id={}, status={}, localMessageId={}",
                    notification.getId(), notification.getStatus(), notification.getLocalMessageId());
        }
    }
    
    private UUID extractAdvisorId(NotificationReceipt receipt) {
        // Try to get from details first (case-insensitive)
        Map<String, Object> details = receipt.getDetails();
        if (details != null) {
            Object entityId = getCaseInsensitive(details, "entity_id");
            if (entityId != null) {
                try {
                    return UUID.fromString(entityId.toString());
                } catch (IllegalArgumentException e) {
                    log.debug("Entity ID in details is not a valid UUID (might be numeric): {}", entityId);
                }
            }
        }
        
        // Try to get from message payload (case-insensitive)
        Map<String, Object> payload = receipt.getMessagePayload();
        if (payload != null) {
            // Try various key names (case-insensitive)
            Object advisorIdentifier = getCaseInsensitive(payload, "advisorIdentifier");
            if (advisorIdentifier == null) {
                advisorIdentifier = getCaseInsensitive(payload, "advisoridentifier");
            }
            if (advisorIdentifier == null) {
                advisorIdentifier = getCaseInsensitive(payload, "advisorId");
            }
            if (advisorIdentifier == null) {
                advisorIdentifier = getCaseInsensitive(payload, "advisor_id");
            }
            
            if (advisorIdentifier != null) {
                try {
                    return UUID.fromString(advisorIdentifier.toString());
                } catch (IllegalArgumentException e) {
                    log.warn("AdvisorIdentifier in payload is not a valid UUID: {}", advisorIdentifier);
                }
            } else {
                log.warn("AdvisorIdentifier not found in payload. Available keys: {}", payload.keySet());
            }
        } else {
            log.warn("Message payload is null for receipt {}", receipt.getId());
        }
        
        return null;
    }
    
    /**
     * Case-insensitive key lookup in a map.
     */
    private Object getCaseInsensitive(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        // First try exact match
        if (map.containsKey(key)) {
            return map.get(key);
        }
        // Then try case-insensitive match
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    private Instant parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.isBlank()) {
            return Instant.now();
        }
        try {
            // WATI timestamp is Unix timestamp in seconds
            long seconds = Long.parseLong(timestampStr);
            return Instant.ofEpochSecond(seconds);
        } catch (NumberFormatException e) {
            log.warn("Invalid timestamp format: {}, using current time", timestampStr);
            return Instant.now();
        }
    }
}

