package com.nivasafinance.notification.orchestrator.controller;

import com.nivasafinance.notification.orchestrator.dto.WatiWebhookPayload;
import com.nivasafinance.notification.orchestrator.service.AdvisorWhatsAppNotificationService;
import com.nivasafinance.notification.orchestrator.service.LeadWhatsAppNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for receiving WATI webhook events.
 * Handles status updates for WhatsApp messages (DELIVERED, REPLIED, etc.).
 * 
 * Note: In production, this endpoint should be protected and WATI webhooks
 * should be filtered through a Lambda function to only forward template-related events.
 */
@RestController
@RequestMapping("/api/notifications/webhooks/wati")
@RequiredArgsConstructor
@Slf4j
public class WatiWebhookController {

    private final LeadWhatsAppNotificationService leadWhatsAppNotificationService;
    private final AdvisorWhatsAppNotificationService advisorWhatsAppNotificationService;

    /**
     * Receives WATI webhook events for WhatsApp message status updates.
     * 
     * Event types handled:
     * - sentMessageDELIVERED_v2: Message was delivered
     * - sentMessageREPLIED_v2: Template message was replied to
     * - message: Actual reply message content (when replyContextId matches our template)
     * 
     * @param webhook WATI webhook payload
     * @param recipientType Query parameter from Lambda: "LEAD" or "ADVISOR" (optional, for backward compatibility)
     * @return HTTP 200 if processed successfully
     */
    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody WatiWebhookPayload webhook,
            @RequestParam(required = false) String recipientType) {
        
        log.info("Received WATI webhook: eventType={}, localMessageId={}, statusString={}, recipientType={}",
                webhook.getEventType(), webhook.getLocalMessageId(), webhook.getStatusString(), recipientType);
        
        try {
            // Use recipientType parameter from Lambda if provided
            if (recipientType != null && !recipientType.isBlank()) {
                if ("LEAD".equalsIgnoreCase(recipientType) || "lead".equalsIgnoreCase(recipientType)) {
                    leadWhatsAppNotificationService.updateFromWebhook(webhook);
                    log.debug("Processed webhook for lead notification (from parameter)");
                } else if ("ADVISOR".equalsIgnoreCase(recipientType) || "advisor".equalsIgnoreCase(recipientType)) {
                    advisorWhatsAppNotificationService.updateFromWebhook(webhook);
                    log.debug("Processed webhook for advisor notification (from parameter)");
                } else {
                    log.warn("Unknown recipientType parameter: {}, trying both tables", recipientType);
                    // Fall back to trying both if parameter is invalid
                    updateBothNotifications(webhook);
                }
            } else {
                // Backward compatibility: try both tables if no parameter provided
                log.debug("No recipientType parameter provided, trying both tables");
                updateBothNotifications(webhook);
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Webhook processed successfully"
            ));
        } catch (Exception ex) {
            log.error("Error processing WATI webhook", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error processing webhook: " + ex.getMessage()
            ));
        }
    }
    
    /**
     * Fallback method to try updating both lead and advisor notifications.
     * Used when recipientType parameter is not provided (backward compatibility).
     */
    private void updateBothNotifications(WatiWebhookPayload webhook) {
        // Try to update lead notification first
        try {
            leadWhatsAppNotificationService.updateFromWebhook(webhook);
            log.debug("Processed webhook for lead notification");
        } catch (Exception e) {
            log.debug("Not a lead notification, trying advisor: {}", e.getMessage());
        }
        
        // Try to update advisor notification
        try {
            advisorWhatsAppNotificationService.updateFromWebhook(webhook);
            log.debug("Processed webhook for advisor notification");
        } catch (Exception e) {
            log.debug("Not an advisor notification: {}", e.getMessage());
        }
    }
    
    /**
     * Health check endpoint for webhook configuration.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "WATI Webhook Handler"
        ));
    }
}

