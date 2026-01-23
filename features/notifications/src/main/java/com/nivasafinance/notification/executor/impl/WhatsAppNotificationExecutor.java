package com.nivasafinance.notification.executor.impl;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.notification.executor.NotificationExecutor;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.entity.NotificationTemplate;
import com.nivasafinance.notification.orchestrator.repository.NotificationRecordRepository;
import com.nivasafinance.notification.orchestrator.repository.NotificationTemplateRepository;
import com.nivasafinance.notification.orchestrator.service.WhatsAppNotificationTrackingService;
import com.nivasafinance.services.whatsapp.dto.TemplateParameter;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.WhatsAppProvider;
import com.nivasafinance.services.whatsapp.provider.wati.WatiWhatsAppProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executor for sending WhatsApp notifications via WATI provider.
 * Handles different WATI accounts for customer (LEAD) vs advisor (ADVISOR).
 * 
 * Note: Template parameter extraction is WhatsApp-specific (uses TemplateParameter DTO),
 * but the logic of extracting parameters based on template.variables is generic and
 * can be adapted for other messaging providers (SMS, Email, etc.).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WhatsAppNotificationExecutor implements NotificationExecutor {

    private final WatiWhatsAppProvider watiWhatsAppProvider;
    private final WatiConfigProvider watiConfigProvider;
    private final NotificationRecordRepository notificationRecordRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final WhatsAppNotificationTrackingService trackingService;

    @Override
    public void send(NotificationReceipt receipt, String renderedMessage) throws Exception {
        String recipientContact = receipt.getRecipientContact();
        String templateIdentifier = receipt.getTemplateIdentifier();
        Map<String, Object> details = receipt.getDetails();
        
        // Determine recipient type (LEAD = customer, ADVISOR = advisor)
        String recipientType = details != null ? (String) details.get("recipient_type") : null;
        if (recipientType == null || recipientType.isBlank()) {
            throw new IllegalStateException("Recipient type not found in receipt details");
        }

        log.info("Sending WhatsApp notification via WATI. Recipient: {}, Type: {}, Template: {}",
                recipientContact, recipientType, templateIdentifier);
        
        // Log message payload keys for debugging
        log.info("Message payload keys available: {}", receipt.getMessagePayload() != null ? receipt.getMessagePayload().keySet() : "null");
        if (receipt.getMessagePayload() != null) {
            receipt.getMessagePayload().forEach((key, value) -> 
                log.debug("  Payload key: '{}' = '{}' (type: {})", key, value, value != null ? value.getClass().getSimpleName() : "null"));
        }

        // Load template to get variables definition
        // Try case-insensitive lookup first, then exact match
        NotificationTemplate template = notificationTemplateRepository.findByIdentifierIgnoreCase(templateIdentifier)
                .orElseGet(() -> notificationTemplateRepository.findByIdentifier(templateIdentifier)
                        .orElseThrow(() -> {
                            log.error("NotificationTemplate not found: '{}'. Please check if the template exists in n_notification_template table with this identifier.", templateIdentifier);
                            return new IllegalStateException("NotificationTemplate not found: " + templateIdentifier + ". Please ensure the template is created in the database.");
                        }));

        // Load notification record to get idempotency key
        NotificationRecord record = notificationRecordRepository.findById(receipt.getNotificationRecordId())
                .orElseThrow(() -> new IllegalStateException("NotificationRecord not found: " + receipt.getNotificationRecordId()));
        
        // Use idempotency key as broadcastName for WATI idempotency
        String broadcastName = record.getIdempotencyKey() != null 
                ? record.getIdempotencyKey() 
                : "notification_" + receipt.getId().toString();

        // Get WATI config based on recipient type
        ThirdPartyConfig watiConfig = watiConfigProvider.getConfigForRecipient(recipientType);
        
        // Build WhatsApp template request
        // Template parameters are extracted based on template.variables definition
        WhatsAppTemplateRequest request = WhatsAppTemplateRequest.builder()
                .phoneNumber(recipientContact)
                .templateName(templateIdentifier)
                .broadcastName(broadcastName) // Use idempotency key for WATI idempotency
                .parameters(buildTemplateParameters(template, receipt.getMessagePayload()))
                .build();

        // Create business context for auditing/logging third-party API calls
        // This is used by the integrations framework to log requests/responses in n_third_party_response_log table
        // entityName: The type of entity (for notifications, it's the receipt)
        // entityId: The ID of the entity (receipt ID converted to Long)
        // businessPurpose: Description of why this API call is being made
        BusinessContext businessContext = new BusinessContext(
                "NOTIFICATION_RECEIPT", // Entity type for logging
                receipt.getId().getMostSignificantBits() & Long.MAX_VALUE, // Use receipt UUID's MSB as Long (better than hashCode)
                "Send WhatsApp notification via WATI for receipt " + receipt.getId()
        );

        // Log the request being sent to WATI for debugging
        log.debug("Sending WATI request - Template: {}, Phone: {}, Parameters: {}", 
                request.getTemplateName(), request.getPhoneNumber(), request.getParameters().size());
        
        // Send via WATI provider and check response
        WhatsAppTemplateResponse response = 
                watiWhatsAppProvider.sendTemplate(request, watiConfig, businessContext);
        
        // Check if send was successful
        if (response == null || !"sent".equalsIgnoreCase(response.getStatus())) {
            String errorMsg = "Unknown error from WATI";
            String status = response != null ? response.getStatus() : "null";
            String messageId = response != null ? response.getMessageId() : null;
            
            // Try to extract more detailed error information
            if (response != null) {
                if (response.getErrorMessage() != null && !response.getErrorMessage().isEmpty()) {
                    errorMsg = response.getErrorMessage();
                } else if (status != null && !status.isEmpty() && !"failed".equalsIgnoreCase(status)) {
                    errorMsg = "WATI returned status: " + status;
                }
            }
            
            log.error("Failed to send WhatsApp notification via WATI for receipt {}. " +
                    "Status: {}, Error: {}, MessageId: {}, Template: {}, Phone: {}, Parameters sent: {}", 
                    receipt.getId(), status, errorMsg, messageId, 
                    request.getTemplateName(), request.getPhoneNumber(), request.getParameters().size());
            
            // Log parameter details for debugging
            if (!request.getParameters().isEmpty()) {
                log.debug("Parameters sent to WATI: {}", 
                        request.getParameters().stream()
                                .map(p -> p.getName() + "=" + p.getValue())
                                .collect(Collectors.joining(", ")));
            }
            
            throw new IllegalStateException("WATI send failed: " + errorMsg + " (Status: " + status + ")");
        }
        
        log.info("Successfully sent WhatsApp notification via WATI for receipt {}. Message ID: {}, Template: {}", 
                receipt.getId(), response.getMessageId(), request.getTemplateName());
        
        // Save tracking record for WhatsApp notification using raw response body directly
        try {
            String rawResponseBody = response.getRawResponseBody();
            log.debug("Raw response body for receipt {}: {}", receipt.getId(), 
                    rawResponseBody != null ? (rawResponseBody.length() > 200 ? rawResponseBody.substring(0, 200) + "..." : rawResponseBody) : "null");
            
            if (rawResponseBody != null && !rawResponseBody.isBlank()) {
                trackingService.saveNotificationTracking(receipt, request.getTemplateName(), rawResponseBody);
            } else {
                log.warn("Raw response body not available for receipt {}, skipping tracking. Response status: {}, messageId: {}", 
                        receipt.getId(), response.getStatus(), response.getMessageId());
            }
        } catch (Exception ex) {
            log.error("Failed to save WhatsApp notification tracking for receipt {}", receipt.getId(), ex);
            // Don't throw - tracking failure shouldn't fail the notification send
        }
    }

    /**
     * Builds template parameters from message payload based on template variables definition.
     * 
     * This method extracts parameters based on the template.variables field, which defines
     * which variables the template expects. This ensures only relevant parameters are sent.
     * 
     * Note: TemplateParameter is WhatsApp-specific (WATI DTO), but the extraction logic
     * (using template.variables to determine which fields to extract) is generic and can
     * be adapted for other messaging providers (SMS, Email, etc.).
     * 
     * @param template The notification template containing variables definition
     * @param messagePayload The message payload containing all available data
     * @return List of TemplateParameter objects for WATI
     */
    private List<TemplateParameter> buildTemplateParameters(NotificationTemplate template, Map<String, Object> messagePayload) {
        List<TemplateParameter> parameters = new ArrayList<>();
        
        if (messagePayload == null || messagePayload.isEmpty()) {
            return parameters;
        }

        // Get template variables definition - this tells us which parameters the template expects
        // IMPORTANT: The parameter name must match EXACTLY what's in template.variables (case-sensitive)
        // Example: If template.variables = {"LeadName": "string"}, we send parameter name "LeadName" (exact)
        Map<String, Object> templateVariables = template.getVariables();
        
        log.info("Building parameters for template '{}'. Template variables (exact names): {}", 
                template.getIdentifier(), templateVariables != null ? templateVariables.keySet() : "null");
        
        // Log all keys in messagePayload to help debug why values aren't found
        log.info("Message payload keys available: {}", messagePayload != null ? messagePayload.keySet() : "null");
        if (messagePayload != null && log.isDebugEnabled()) {
            messagePayload.forEach((key, value) -> 
                log.debug("  Payload key: '{}' = '{}' (type: {})", 
                        key, value, value != null ? value.getClass().getSimpleName() : "null"));
        }
        
        if (templateVariables != null && !templateVariables.isEmpty()) {
            // Extract only the parameters defined in template.variables
            // CRITICAL: Use the EXACT variable name from template.variables as the parameter name
            // We use case-insensitive matching to FIND the value in messagePayload,
            // but the parameter NAME sent to WATI must match template.variables exactly
            for (Map.Entry<String, Object> variableEntry : templateVariables.entrySet()) {
                String variableName = variableEntry.getKey(); // This is the EXACT name WATI expects (e.g., "LeadName")
                Object variableValue = findValueCaseInsensitive(messagePayload, variableName);
                
                String valueStr;
                if (variableValue != null) {
                    valueStr = variableValue.toString();
                    log.debug("Found value for '{}': '{}' (original type: {})", 
                            variableName, valueStr, variableValue.getClass().getSimpleName());
                } else {
                    // Value not found - log detailed error
                    log.error("Template variable '{}' not found in message payload (case-insensitive search). " +
                            "Available keys: {}. " +
                            "Using whitespace as fallback. " +
                            "Please check: 1) Data provider query returns this field, 2) Field name matches (case-insensitive).",
                            variableName, messagePayload.keySet());
                    // As safety measure, use whitespace instead of empty string
                    valueStr = " ";
                }
                
                // Check if value is empty after trimming
                if (valueStr.trim().isEmpty()) {
                    log.warn("Template variable '{}' found in messagePayload but value is empty/whitespace (value: '{}'). " +
                            "Using whitespace as fallback. " +
                            "Please ensure the data provider query uses COALESCE to provide a default value if the field is NULL. " +
                            "Example: COALESCE(lead_person.display_name, 'Customer') AS LeadName",
                            variableName, valueStr);
                    // As safety measure, use whitespace instead of empty string
                    valueStr = " ";
                }
                
                TemplateParameter param = TemplateParameter.builder()
                        .name(variableName) // Use EXACT name from template.variables (case-sensitive match required by WATI)
                        .value(valueStr)
                        .build();
                parameters.add(param);
                log.info("Added parameter: name='{}' (exact from template), value='{}' (length: {})", 
                        param.getName(), param.getValue(), param.getValue().length());
            }
            
            log.info("Total parameters built: {}. Parameter names being sent to WATI: {}", 
                    parameters.size(),
                    parameters.stream().map(TemplateParameter::getName).collect(java.util.stream.Collectors.toList()));
        } else {
            // Fallback: if template.variables is not defined, use all payload fields
            // (excluding internal/system fields)
            log.warn("Template variables not defined for template '{}'. Using all payload fields as fallback.", 
                    template.getIdentifier());
            for (Map.Entry<String, Object> entry : messagePayload.entrySet()) {
                // Skip internal fields that shouldn't be sent as template parameters
                if (entry.getKey().startsWith("_") || 
                    entry.getKey().equals("templateIdentifier") ||
                    entry.getKey().equals("recipientContact")) {
                    continue;
                }
                
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                parameters.add(TemplateParameter.builder()
                        .name(entry.getKey())
                        .value(value)
                        .build());
            }
        }
        
        return parameters;
    }

    /**
     * Finds a value in the map using case-insensitive key matching.
     * This handles variations like "LeadName" vs "Lead_name" or "loan_amount" vs "LoanAmount".
     */
    private Object findValueCaseInsensitive(Map<String, Object> map, String key) {
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
        
        // Try matching after removing underscores and converting to same case
        String normalizedKey = key.replace("_", "").toLowerCase();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String normalizedEntryKey = entry.getKey().replace("_", "").toLowerCase();
            if (normalizedEntryKey.equals(normalizedKey)) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    @Override
    public String getMode() {
        return "WATI";
    }

    @Override
    public String getChannelType() {
        return "WHATSAPP";
    }
}

