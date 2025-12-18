package com.nivasafinance.webhooks.call.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.webhooks.call.service.CallWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiConstants.V1 + "/webhooks")
@RequiredArgsConstructor
public class CallWebhookController {

    private final CallWebhookService webhookService;

    @PostMapping(value = "/call-events", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> handleCallEvent(
            @RequestParam MultiValueMap<String, String> formData) {
        
        Map<String, Object> response = webhookService.handleWebhook(formData);
        
        if ("error".equals(response.get("status"))) {
            if (response.containsKey("errors")) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}

