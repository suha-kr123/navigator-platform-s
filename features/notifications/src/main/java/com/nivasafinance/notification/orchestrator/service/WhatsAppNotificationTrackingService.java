package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to track WhatsApp notifications by saving records after WATI sends messages.
 * Uses raw response body directly from WATI API response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppNotificationTrackingService {

    private final WatiResponseParser watiResponseParser;
    private final LeadWhatsAppNotificationService leadWhatsAppNotificationService;
    private final AdvisorWhatsAppNotificationService advisorWhatsAppNotificationService;

    /**
     * Saves WhatsApp notification tracking record after message is sent.
     * Uses raw response body directly from WATI API response.
     * 
     * @param receipt The notification receipt
     * @param templateName The template name used
     * @param rawResponseBody The raw response body from WATI API
     */
    @Transactional
    public void saveNotificationTracking(NotificationReceipt receipt, String templateName, String rawResponseBody) {
        try {
            log.debug("Saving notification tracking for receipt: {}, template: {}, rawResponseBody length: {}", 
                    receipt.getId(), templateName, rawResponseBody != null ? rawResponseBody.length() : 0);
            
            if (rawResponseBody == null || rawResponseBody.isBlank()) {
                log.warn("Raw response body is empty for receipt {}, cannot save tracking record", receipt.getId());
                return;
            }
            
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
            
            // Determine recipient type and save accordingly
            String recipientType = receipt.getDetails() != null ? 
                    (String) receipt.getDetails().get("recipient_type") : null;
            
            if ("LEAD".equalsIgnoreCase(recipientType) || "lead".equalsIgnoreCase(recipientType)) {
                leadWhatsAppNotificationService.createFromWatiResponse(receipt, watiResponse, templateName);
            } else if ("ADVISOR".equalsIgnoreCase(recipientType) || "advisor".equalsIgnoreCase(recipientType)) {
                advisorWhatsAppNotificationService.createFromWatiResponse(receipt, watiResponse, templateName);
            } else {
                log.warn("Unknown recipient type '{}' for receipt {}, cannot save tracking record", 
                        recipientType, receipt.getId());
            }
            
        } catch (Exception ex) {
            log.error("Failed to save WhatsApp notification tracking for receipt {}", receipt.getId(), ex);
            // Don't throw - we don't want to fail the notification send if tracking fails
        }
    }
}

