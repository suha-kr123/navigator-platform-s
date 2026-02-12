package com.nivasafinance.services.whatsapp.provider.gallabox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.dto.TemplateParameter;
import com.nivasafinance.services.whatsapp.provider.gallabox.data.GallaboxConfiguration;

import java.util.HashMap;
import java.util.Map;

public class GallaboxHttpClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GallaboxHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public WhatsAppTemplateResponse sendTemplate(GallaboxConfiguration config, WhatsAppTemplateRequest request) {
        ResponseEntity<String> response = null;
        String responseBody = null;
        
        try {
            // Gallabox expects phone number without + sign
            String phoneNumber = request.getPhoneNumber().replace("+", "");
            
            String url = config.getApiEndpoint();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apiSecret", config.getApiSecret());
            headers.set("apiKey", config.getApiKey());

            // Build request body for Gallabox
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("channelId", config.getChannelId());
            requestBody.put("channelType", "whatsapp");
            
            // Recipient phone number
            Map<String, String> recipient = new HashMap<>();
            recipient.put("phone", phoneNumber);
            requestBody.put("recipient", recipient);
            
            // WhatsApp template structure
            Map<String, Object> whatsapp = new HashMap<>();
            whatsapp.put("type", "template");
            
            Map<String, Object> template = new HashMap<>();
            template.put("templateName", request.getTemplateName());
            
            // Convert parameters to bodyValues format
            Map<String, String> bodyValues = new HashMap<>();
            if (request.getParameters() != null && !request.getParameters().isEmpty()) {
                for (TemplateParameter param : request.getParameters()) {
                    bodyValues.put(param.getName(), param.getValue());
                }
            }
            template.put("bodyValues", bodyValues);

            if (request.getButtonValues() != null && !request.getButtonValues().isEmpty()) {
                template.put("buttonValues", request.getButtonValues());
            }

            whatsapp.put("template", template);
            requestBody.put("whatsapp", whatsapp);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Check HTTP status code first
            boolean httpError = response.getStatusCode().isError();
            responseBody = response.getBody();
            
            // If HTTP error, return error response immediately
            if (httpError) {
                String errorMsg = "HTTP " + response.getStatusCode().value();
                if (responseBody != null) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        if (jsonNode.has("message")) {
                            errorMsg = jsonNode.get("message").asText();
                        } else if (jsonNode.has("error")) {
                            errorMsg = jsonNode.get("error").asText();
                        }
                    } catch (Exception e) {
                        // Use HTTP error code if JSON parsing fails
                    }
                }
                
                return WhatsAppTemplateResponse.builder()
                        .messageId(null)
                        .status("failed")
                        .phoneNumber(request.getPhoneNumber())
                        .templateName(request.getTemplateName())
                        .errorMessage(errorMsg)
                        .rawResponseBody(responseBody)
                        .build();
            }

            // Parse response body
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // Gallabox response formats:
            // Success: {"id": "...", "status": "ACCEPTED", "message": "...", "warnings": []}
            // Error: {"status": "UNPROCESSABLE_ENTITY", "message": "Template bodyValues contains invalid values"}
            String messageId = jsonNode.has("id") ? jsonNode.get("id").asText() : null;
            String status = jsonNode.has("status") ? jsonNode.get("status").asText() : null;
            String message = jsonNode.has("message") ? jsonNode.get("message").asText() : null;
            
            // Determine if this is a success or error response
            boolean isSuccess = "ACCEPTED".equalsIgnoreCase(status);
            boolean isError = status != null && 
                             (status.toUpperCase().contains("ERROR") || 
                              status.toUpperCase().contains("FAIL") ||
                              status.toUpperCase().contains("UNPROCESSABLE") ||
                              status.toUpperCase().contains("BAD_REQUEST") ||
                              status.toUpperCase().contains("NOT_FOUND"));
            
            // Map Gallabox status to our internal status
            String mappedStatus;
            String errorMessage;
            
            if (isSuccess) {
                mappedStatus = "ACCEPTED";
                errorMessage = null;
            } else if (isError) {
                mappedStatus = "failed";
                errorMessage = message != null ? message : ("Gallabox returned status: " + status);
            } else {
                // Unknown status - treat as failed for safety
                mappedStatus = "failed";
                errorMessage = message != null ? message : ("Unknown Gallabox status: " + status);
            }

            return WhatsAppTemplateResponse.builder()
                    .messageId(messageId)
                    .status(mappedStatus)
                    .phoneNumber(request.getPhoneNumber())
                    .templateName(request.getTemplateName())
                    .errorMessage(errorMessage)
                    .rawResponseBody(responseBody)
                    .build();

        } catch (Exception e) {
            // Try to capture response body even on error for debugging
            if (responseBody == null && response != null && response.getBody() != null) {
                responseBody = response.getBody();
            }
            
            return WhatsAppTemplateResponse.builder()
                    .messageId(null)
                    .status("failed")
                    .phoneNumber(request.getPhoneNumber())
                    .templateName(request.getTemplateName())
                    .errorMessage(e.getMessage())
                    .rawResponseBody(responseBody)
                    .build();
        }
    }
}
