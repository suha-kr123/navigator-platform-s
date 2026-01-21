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
            
            whatsapp.put("template", template);
            requestBody.put("whatsapp", whatsapp);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parse response
            responseBody = response.getBody();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // Gallabox response structure may vary, adjust based on actual API response
            boolean result = jsonNode.has("success") && jsonNode.get("success").asBoolean();
            String messageId = jsonNode.has("messageId") ? jsonNode.get("messageId").asText() : 
                             (jsonNode.has("id") ? jsonNode.get("id").asText() : null);
            String errorMessage = jsonNode.has("error") ? jsonNode.get("error").asText() : 
                                (jsonNode.has("message") ? jsonNode.get("message").asText() : null);

            return WhatsAppTemplateResponse.builder()
                    .messageId(messageId)
                    .status(result ? "sent" : "failed")
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
