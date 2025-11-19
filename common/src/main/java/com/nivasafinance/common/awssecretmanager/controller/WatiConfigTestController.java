package com.nivasafinance.common.awssecretmanager.controller;

import com.nivasafinance.common.awssecretmanager.config.WatiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test/wati-config")
@RequiredArgsConstructor
public class WatiConfigTestController {

    private final WatiConfig watiConfig;

    @GetMapping
    public ResponseEntity<Map<String, String>> testWatiConfig() {
        Map<String, String> config = new HashMap<>();
        
        try {
            String apiKey = watiConfig.getApiKey();
            config.put("apiKey", apiKey != null ? apiKey : "null");
            System.out.println("getApiKey() = " + apiKey);
        } catch (Exception e) {
            config.put("apiKey", "ERROR: " + e.getMessage());
            System.out.println("getApiKey() ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            String baseUrl = watiConfig.getBaseUrl();
            config.put("baseUrl", baseUrl != null ? baseUrl : "null");
            System.out.println("getBaseUrl() = " + baseUrl);
        } catch (Exception e) {
            config.put("baseUrl", "ERROR: " + e.getMessage());
            System.out.println("getBaseUrl() ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            String senderId = watiConfig.getSenderId();
            config.put("senderId", senderId != null ? senderId : "null");
            System.out.println("getSenderId() = " + senderId);
        } catch (Exception e) {
            config.put("senderId", "ERROR: " + e.getMessage());
            System.out.println("getSenderId() ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(config);
    }
}

