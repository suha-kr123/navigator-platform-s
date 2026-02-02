package com.nivasafinance.notification.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to track WhatsApp notifications by saving records after messages are sent.
 * Supports both WATI and Gallabox providers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppNotificationTrackingService {

    private final WatiResponseParser watiResponseParser;
    private final LeadWhatsAppNotificationService leadWhatsAppNotificationService;
    private final AdvisorWhatsAppNotificationService advisorWhatsAppNotificationService;
    private final ObjectMapper objectMapper;

    /**
     * Saves WhatsApp notification tracking record after message is sent.
     * Automatically detects provider (WATI or GALLABOX) based on receipt mode.
     * 
     * @param receipt The notification receipt
     * @param templateName The template name used
     * @param rawResponseBody The raw response body from the provider API
     */
    @Transactional
    public void saveNotificationTracking(NotificationReceipt receipt, String templateName, String rawResponseBody) {
        try {
            log.debug("Saving notification tracking for receipt: {}, mode: {}, template: {}, rawResponseBody length: {}", 
                    receipt.getId(), receipt.getMode(), templateName, rawResponseBody != null ? rawResponseBody.length() : 0);
            
            if (rawResponseBody == null || rawResponseBody.isBlank()) {
                log.warn("Raw response body is empty for receipt {}, cannot save tracking record", receipt.getId());
                return;
            }
            
            // Determine provider from receipt mode
            String mode = receipt.getMode();
            if (mode == null || mode.isBlank()) {
                log.warn("Receipt {} has no mode specified, cannot determine provider for tracking", receipt.getId());
                return;
            }
            
            // Determine recipient type
            String recipientType = receipt.getDetails() != null ? 
                    (String) receipt.getDetails().get("recipient_type") : null;
            
            if ("GALLABOX".equalsIgnoreCase(mode)) {
                // Handle Gallabox response
                saveGallaboxTracking(receipt, templateName, rawResponseBody, recipientType);
            } else if ("WATI".equalsIgnoreCase(mode)) {
                // Handle WATI response
                saveWatiTracking(receipt, templateName, rawResponseBody, recipientType);
            } else {
                log.warn("Unknown provider mode '{}' for receipt {}, cannot save tracking record", mode, receipt.getId());
            }
            
        } catch (Exception ex) {
            log.error("Failed to save WhatsApp notification tracking for receipt {}", receipt.getId(), ex);
            // Don't throw - we don't want to fail the notification send if tracking fails
        }
    }
    
    private void saveWatiTracking(NotificationReceipt receipt, String templateName, String rawResponseBody, String recipientType) {
        // Parse the full WATI response
        WatiSendTemplateResponse watiResponse = watiResponseParser.parseSendTemplateResponse(rawResponseBody);
        if (watiResponse == null) {
            log.warn("Failed to parse WATI response for receipt {}. Raw response (first 500 chars): {}", 
                    receipt.getId(), 
                    rawResponseBody.length() > 500 ? rawResponseBody.substring(0, 500) + "..." : rawResponseBody);
            return;
        }
        
        log.debug("Parsed WATI response successfully: result={}, templateName={}, receivers={}", 
                watiResponse.getResult(), watiResponse.getTemplateName(),
                watiResponse.getReceivers() != null ? watiResponse.getReceivers().size() : 0);
        
        if ("LEAD".equalsIgnoreCase(recipientType) || "lead".equalsIgnoreCase(recipientType)) {
            leadWhatsAppNotificationService.createFromWatiResponse(receipt, watiResponse, templateName);
        } else if ("ADVISOR".equalsIgnoreCase(recipientType) || "advisor".equalsIgnoreCase(recipientType)) {
            advisorWhatsAppNotificationService.createFromWatiResponse(receipt, watiResponse, templateName);
        } else {
            log.warn("Unknown recipient type '{}' for receipt {}, cannot save tracking record", 
                    recipientType, receipt.getId());
        }
    }
    
    private void saveGallaboxTracking(NotificationReceipt receipt, String templateName, String rawResponseBody, String recipientType) {
        try {
            // Parse Gallabox response
            // Expected format: {"id": "69730682078eef8d2c8edbb2", "status": "ACCEPTED", "message": "...", "warnings": []}
            JsonNode jsonNode = objectMapper.readTree(rawResponseBody);
            
            // Extract message ID - Gallabox uses "id" field
            String messageId = jsonNode.has("id") ? jsonNode.get("id").asText() : null;
            
            // Extract status - Gallabox returns "ACCEPTED" for successful sends
            String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "UNKNOWN";
            
            // Map Gallabox status to our internal status
            // "ACCEPTED" means message was accepted and is being sent
            String mappedStatus = "ACCEPTED".equalsIgnoreCase(status) ? "sent" : status.toLowerCase();
            
            String phoneNumber = receipt.getRecipientContact();
            
            log.debug("Parsed Gallabox response: id={}, status={}, mappedStatus={}, templateName={}", 
                    messageId, status, mappedStatus, templateName);
            
            // Create tracking record for Gallabox
            if ("LEAD".equalsIgnoreCase(recipientType) || "lead".equalsIgnoreCase(recipientType)) {
                leadWhatsAppNotificationService.createFromGallaboxResponse(receipt, messageId, mappedStatus, templateName, phoneNumber);
            } else if ("ADVISOR".equalsIgnoreCase(recipientType) || "advisor".equalsIgnoreCase(recipientType)) {
                advisorWhatsAppNotificationService.createFromGallaboxResponse(receipt, messageId, mappedStatus, templateName, phoneNumber);
            } else {
                log.warn("Unknown recipient type '{}' for receipt {}, cannot save tracking record", 
                        recipientType, receipt.getId());
            }
        } catch (Exception ex) {
            log.error("Failed to parse Gallabox response for receipt {}. Raw response (first 500 chars): {}", 
                    receipt.getId(), 
                    rawResponseBody.length() > 500 ? rawResponseBody.substring(0, 500) + "..." : rawResponseBody, ex);
        }
    }
}

