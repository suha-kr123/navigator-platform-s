package com.nivasafinance.services.voice.webhook.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.services.voice.webhook.VoiceWebhookHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(ApiConstants.V1 + "/webhooks")
@RequiredArgsConstructor
public class VoiceWebhookController {

    private final Set<VoiceWebhookHandler> webhookHandlers;

    @PostMapping(value = "/call-events", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> handleCallEvent(
            @RequestParam MultiValueMap<String, String> formData) {
        
        VoiceWebhookHandler handler = findHandler("EXOTEL");
        if (handler == null) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "error");
            response.put("message", "No webhook handler found for EXOTEL");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        
        Map<String, Object> response = handler.handleWebhook(formData);
        
        if ("error".equals(response.get("status"))) {
            if (response.containsKey("errors")) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    private VoiceWebhookHandler findHandler(String provider) {
        String providerUpper = provider.toUpperCase();
        return webhookHandlers.stream()
                .filter(h -> h.getProviderName().equalsIgnoreCase(providerUpper))
                .findFirst()
                .orElse(null);
    }
}

