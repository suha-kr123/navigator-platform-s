package com.nivasafinance.notification.orchestrator.controller;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.whatsapp.dto.TemplateParameter;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.gallabox.GallaboxWhatsAppProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test controller for Gallabox WhatsApp provider.
 * Allows testing the Gallabox API with manual credentials from local environment.
 */
@RestController
@RequestMapping("/api/notifications/test/gallabox")
@RequiredArgsConstructor
@Slf4j
public class GallaboxTestController {

    private final GallaboxWhatsAppProvider gallaboxWhatsAppProvider;

    /**
     * Test endpoint to send a WhatsApp template message via Gallabox.
     * 
     * This endpoint allows you to test the Gallabox integration with manual credentials.
     * 
     * @param request Test request containing credentials and message details
     * @return Response with the result of the API call
     */
    @PostMapping("/send-template")
    public ResponseEntity<Map<String, Object>> testSendTemplate(@RequestBody GallaboxTestRequest request) {
        try {
            log.info("Testing Gallabox send template - Phone: {}, Template: {}", 
                    request.getPhoneNumber(), request.getTemplateName());

            // Validate required fields
            if (request.getApiKey() == null || request.getApiKey().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "apiKey is required"
                ));
            }
            if (request.getApiSecret() == null || request.getApiSecret().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "apiSecret is required"
                ));
            }
            if (request.getChannelId() == null || request.getChannelId().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "channelId is required"
                ));
            }
            if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "phoneNumber is required"
                ));
            }
            if (request.getTemplateName() == null || request.getTemplateName().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "templateName is required"
                ));
            }

            // Build ThirdPartyConfig from request
            Map<String, String> configMap = new HashMap<>();
            configMap.put("api_key", request.getApiKey());
            configMap.put("api_secret", request.getApiSecret());
            configMap.put("channel_id", request.getChannelId());
            if (request.getApiEndpoint() != null && !request.getApiEndpoint().isEmpty()) {
                configMap.put("api_endpoint", request.getApiEndpoint());
            }

            ThirdPartyConfig config = new ThirdPartyConfig(
                    null,
                    "Test Gallabox Config",
                    "GALLABOX",
                    configMap
            );

            // Build WhatsAppTemplateRequest
            List<TemplateParameter> parameters = new ArrayList<>();
            if (request.getParameters() != null) {
                for (Map.Entry<String, String> param : request.getParameters().entrySet()) {
                    parameters.add(TemplateParameter.builder()
                            .name(param.getKey())
                            .value(param.getValue())
                            .build());
                }
            }

            WhatsAppTemplateRequest templateRequest = WhatsAppTemplateRequest.builder()
                    .phoneNumber(request.getPhoneNumber())
                    .templateName(request.getTemplateName())
                    .parameters(parameters)
                    .build();

            // Create business context
            BusinessContext businessContext = new BusinessContext(
                    "GALLABOX_TEST",
                    0L,
                    "Test Gallabox template send"
            );

            // Send via Gallabox
            WhatsAppTemplateResponse response = gallaboxWhatsAppProvider.sendTemplate(
                    templateRequest,
                    config,
                    businessContext
            );

            // Build response
            Map<String, Object> result = new HashMap<>();
            result.put("success", "sent".equalsIgnoreCase(response.getStatus()));
            result.put("status", response.getStatus());
            result.put("messageId", response.getMessageId());
            result.put("phoneNumber", response.getPhoneNumber());
            result.put("templateName", response.getTemplateName());
            result.put("errorMessage", response.getErrorMessage());
            result.put("rawResponseBody", response.getRawResponseBody());

            if ("sent".equalsIgnoreCase(response.getStatus())) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }

        } catch (Exception ex) {
            log.error("Error testing Gallabox send template", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", ex.getMessage(),
                    "errorType", ex.getClass().getSimpleName()
            ));
        }
    }

    @Data
    public static class GallaboxTestRequest {
        /**
         * Gallabox API Key (from AWS Secrets Manager: apiKey)
         */
        private String apiKey;

        /**
         * Gallabox API Secret (from AWS Secrets Manager: apiSecret)
         */
        private String apiSecret;

        /**
         * Gallabox Channel ID (from AWS Secrets Manager: channelId)
         * - Customer: 6961ee06fada8aac97d1dc62
         * - Advisor: 696628bd52ce35f989e8ebb0
         */
        private String channelId;

        /**
         * Optional: API endpoint (defaults to https://server.gallabox.com/devapi/messages/whatsapp)
         */
        private String apiEndpoint;

        /**
         * Phone number (with or without + prefix)
         */
        private String phoneNumber;

        /**
         * Template name registered in Gallabox
         */
        private String templateName;

        /**
         * Template parameters as key-value pairs
         * Example: {"leadName": "John Doe", "staffNumber": "12345"}
         */
        private Map<String, String> parameters;
    }
}
