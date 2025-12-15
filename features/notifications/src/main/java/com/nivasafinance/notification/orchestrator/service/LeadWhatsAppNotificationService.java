package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import com.nivasafinance.notification.orchestrator.dto.WatiWebhookPayload;
import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.enums.WhatsAppMessageStatus;
import com.nivasafinance.notification.orchestrator.repository.LeadWhatsAppNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing lead WhatsApp notification tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeadWhatsAppNotificationService {

    private final LeadWhatsAppNotificationRepository repository;

    /**
     * Creates a notification record from WATI send template response.
     * Extracts leadIdentifier from notification receipt details or message payload.
     */
    @Transactional
    public LeadWhatsAppNotification createFromWatiResponse(
            NotificationReceipt receipt,
            WatiSendTemplateResponse watiResponse,
            String templateName) {
        
        if (watiResponse == null || watiResponse.getReceivers() == null || watiResponse.getReceivers().isEmpty()) {
            throw new IllegalStateException("WATI response is empty or has no receivers");
        }
        
        WatiSendTemplateResponse.Receiver receiver = watiResponse.getReceivers().get(0);
        
        // Extract leadIdentifier (UUID) from receipt details or message payload
        UUID leadIdentifier = extractLeadId(receipt);
        if (leadIdentifier == null) {
            throw new IllegalStateException("LeadIdentifier not found in receipt details or message payload");
        }
        
        LeadWhatsAppNotification notification = LeadWhatsAppNotification.builder()
                .leadIdentifier(leadIdentifier)
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
        
        LeadWhatsAppNotification saved = repository.save(notification);
        log.info("Created lead WhatsApp notification record: id={}, leadIdentifier={}, localMessageId={}",
                saved.getId(), saved.getLeadIdentifier(), saved.getLocalMessageId());
        
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
        
        Optional<LeadWhatsAppNotification> notificationOpt = repository.findByLocalMessageId(webhook.getLocalMessageId());
        if (notificationOpt.isEmpty()) {
            log.warn("No lead WhatsApp notification found for localMessageId: {}", webhook.getLocalMessageId());
            return;
        }
        
        LeadWhatsAppNotification notification = notificationOpt.get();
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
            log.info("Updated lead WhatsApp notification: id={}, status={}, localMessageId={}",
                    notification.getId(), notification.getStatus(), notification.getLocalMessageId());
        }
    }
    
    private UUID extractLeadId(NotificationReceipt receipt) {
        // Try to get from details first
        Map<String, Object> details = receipt.getDetails();
        if (details != null) {
            Object entityId = details.get("entity_id");
            if (entityId != null) {
                try {
                    return UUID.fromString(entityId.toString());
                } catch (IllegalArgumentException e) {
                    log.warn("Entity ID in details is not a valid UUID: {}", entityId);
                }
            }
        }
        
        // Try to get from message payload
        Map<String, Object> payload = receipt.getMessagePayload();
        if (payload != null) {
            Object leadId = payload.get("leadId");
            if (leadId == null) {
                leadId = payload.get("lead_id");
            }
            if (leadId == null) {
                leadId = payload.get("leadIdentifier");
            }
            if (leadId != null) {
                try {
                    return UUID.fromString(leadId.toString());
                } catch (IllegalArgumentException e) {
                    log.warn("LeadId in payload is not a valid UUID: {}", leadId);
                }
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

