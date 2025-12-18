package com.nivasafinance.services.whatsapp.provider.wati;

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
import com.nivasafinance.services.whatsapp.provider.wati.data.WatiConfiguration;

import java.util.HashMap;
import java.util.Map;

public class WatiHttpClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WatiHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public WhatsAppTemplateResponse sendTemplate(WatiConfiguration config, WhatsAppTemplateRequest request) {
        ResponseEntity<String> response = null;
        String responseBody = null;
        
        try {
            // WATI expects phone number without + sign
            String phoneNumber = request.getPhoneNumber().replace("+", "");
            
            // Phone number goes as query parameter in URL!
            String url = config.getApiEndpoint() + "/api/v2/sendTemplateMessage?whatsappNumber=" + phoneNumber;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getAccessToken());

            // Build request body - phone number NOT in body, it's in URL!
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("template_name", request.getTemplateName());
            
            // BroadcastName is required by WATI
            String broadcastName = request.getBroadcastName();
            if (broadcastName == null || broadcastName.isEmpty()) {
                broadcastName = "order_update";
            }
            requestBody.put("broadcast_name", broadcastName);

            // Parameters - convert to list format for WATI
            java.util.List<Map<String, String>> paramsList = new java.util.ArrayList<>();
            if (request.getParameters() != null && !request.getParameters().isEmpty()) {
                for (TemplateParameter param : request.getParameters()) {
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("name", param.getName());
                    paramMap.put("value", param.getValue());
                    paramsList.add(paramMap);
                }
            }
            requestBody.put("parameters", paramsList);

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

            boolean result = jsonNode.has("result") && jsonNode.get("result").asBoolean();
            String messageId = jsonNode.has("id") ? jsonNode.get("id").asText() : null;
            String errorMessage = jsonNode.has("error") ? jsonNode.get("error").asText() : null;

            return WhatsAppTemplateResponse.builder()
                    .messageId(messageId)
                    .status(result ? "sent" : "failed")
                    .phoneNumber(request.getPhoneNumber())
                    .templateName(request.getTemplateName())
                    .errorMessage(errorMessage)
                    .rawResponseBody(responseBody) // Store raw response for full parsing
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
                    .rawResponseBody(responseBody) // Capture response even on error
                    .build();
        }
    }

    public WhatsAppTemplateResponse getTemplateStatus(WatiConfiguration config, String phoneNumber) {
        try {
            // WATI expects phone number without + sign
            String cleanPhoneNumber = phoneNumber.replace("+", "");
            
            String url = config.getApiEndpoint() + "/api/v1/getMessages/" + cleanPhoneNumber
                    + "?pageSize=10&pageNumber=1";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(config.getAccessToken());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Parse response
            String responseBody = response.getBody();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            String result = jsonNode.path("result").asText("unknown");
            JsonNode messagesNode = jsonNode.path("messages").path("items");

            String messageId = null;
            String templateName = null;
            String deliveredAt = null;
            String readAt = null;
            String statusString = result;

            if (messagesNode.isArray() && messagesNode.size() > 0) {
                JsonNode latestMessage = messagesNode.get(0);
                messageId = latestMessage.path("id").asText(null);
                templateName = latestMessage.path("templateId").asText(null);
                if (latestMessage.has("created")) {
                    deliveredAt = latestMessage.get("created").asText();
                }
                if (latestMessage.has("statusString")) {
                    statusString = latestMessage.get("statusString").asText(result);
                }
            }

            String errorMessage = null;
            if (!"success".equalsIgnoreCase(result)) {
                errorMessage = jsonNode.path("error").asText("Unknown error from WATI");
            }

            return WhatsAppTemplateResponse.builder()
                    .phoneNumber(phoneNumber)
                    .status(statusString)
                    .messageId(messageId)
                    .templateName(templateName)
                    .deliveredAt(deliveredAt)
                    .readAt(readAt)
                    .errorMessage(errorMessage)
                    .build();

        } catch (Exception e) {
            return WhatsAppTemplateResponse.builder()
                    .phoneNumber(phoneNumber)
                    .status("failed")
                    .messageId(null)
                    .templateName(null)
                    .deliveredAt(null)
                    .readAt(null)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}

