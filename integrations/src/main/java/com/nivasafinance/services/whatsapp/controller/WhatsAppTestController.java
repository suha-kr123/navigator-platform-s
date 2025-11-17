package com.nivasafinance.services.whatsapp.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.dto.TemplateLogRequest;
import com.nivasafinance.services.whatsapp.provider.wati.WatiApiClient;
import com.nivasafinance.services.whatsapp.provider.wati.data.WatiConfiguration;
import com.nivasafinance.services.whatsapp.service.WhatsAppLogService;

/**
 * TEMPORARY TEST CONTROLLER - For testing WATI API directly without database
 * This bypasses the full service architecture for quick testing
 */
@RestController
@RequestMapping("/api/v1/whatsapp/test")
public class WhatsAppTestController {

    private static final String DEFAULT_SECRET_NAME = "WATI";
    private static final String DEFAULT_ENV_PREFIX = "WATI";

    private final WhatsAppLogService whatsAppLogService;

    public WhatsAppTestController(WhatsAppLogService whatsAppLogService) {
        this.whatsAppLogService = whatsAppLogService;
    }

    @PostMapping("/send-direct")
    public ResponseEntity<WhatsAppTemplateResponse> sendTemplateDirect(
            @RequestBody WhatsAppTemplateRequest request,
            @RequestParam(required = false) String secretName) {

        WatiConfiguration config = buildConfigFromEnvironment(secretName);

        WatiApiClient apiClient = new WatiApiClient();
        WhatsAppTemplateResponse response = apiClient.sendTemplate(config, request);

        String logMessageId = response.getMessageId();
        if (logMessageId == null || logMessageId.isBlank()) {
            logMessageId = java.util.UUID.randomUUID().toString();
        }

        TemplateLogRequest logRequest = TemplateLogRequest.builder()
                        .messageId(logMessageId)
                        .phoneNumber(response.getPhoneNumber() != null ? response.getPhoneNumber() : request.getPhoneNumber())
                        .templateName(response.getTemplateName() != null ? response.getTemplateName() : request.getTemplateName())
                        .broadcastName(request.getBroadcastName())
                        .status(response.getStatus() != null ? response.getStatus() : "unknown")
                        .build();
        whatsAppLogService.createTemplateLog(logRequest);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status-direct/{phoneNumber}")
    public ResponseEntity<WhatsAppTemplateResponse> getStatusDirect(
            @PathVariable String phoneNumber,
            @RequestParam(required = false) String secretName) {

        WatiConfiguration config = buildConfigFromEnvironment(secretName);

        WatiApiClient apiClient = new WatiApiClient();
        WhatsAppTemplateResponse response = apiClient.getTemplateStatus(config, phoneNumber);

        if (response.getStatus() != null) {
            if (response.getMessageId() != null) {
                whatsAppLogService.updateTemplateStatus(
                        response.getMessageId(),
                        response.getStatus(),
                        response.getDeliveredAt()
                );
            } else {
                whatsAppLogService.updateTemplateStatusByPhoneNumber(
                        phoneNumber,
                        response.getStatus(),
                        response.getDeliveredAt()
                );
            }
        }

        return ResponseEntity.ok(response);
    }

    private WatiConfiguration buildConfigFromEnvironment(String requestPrefix) {
        String prefix = resolveEnvPrefix(requestPrefix);

        String apiEndpoint = getRequiredEnvValue(
                prefix,
                "BASE_URL",
                "WATI_BASE_URL",
                "BASE_WATI_URL",
                "BASEURL"
        );
        String accessToken = getRequiredEnvValue(
                prefix,
                "ACCESS_TOKEN",
                "WATI_ACCESS_TOKEN",
                "BEARER_TOKEN",
                "TOKEN"
        );
        String clientId = getOptionalEnvValue(
                prefix,
                "CLIENT_ID",
                "WATI_CLIENT_ID",
                "CLIENTID"
        );

        int timeout = getOptionalIntEnvValue(prefix, 30, "TIMEOUT", "WATI_TIMEOUT");
        int retryAttempts = getOptionalIntEnvValue(prefix, 3, "RETRY_ATTEMPTS", "WATI_RETRY_ATTEMPTS");

        return WatiConfiguration.builder()
                .apiEndpoint(apiEndpoint)
                .accessToken(accessToken)
                .clientId(clientId)
                .timeout(timeout)
                .retryAttempts(retryAttempts)
                .build();
    }

    private String resolveEnvPrefix(String requestPrefix) {
        if (requestPrefix != null && !requestPrefix.isBlank()) {
            return requestPrefix;
        }
        return DEFAULT_ENV_PREFIX;
    }

    private String getRequiredEnvValue(String prefix, String defaultKey, String... fallbackKeys) {
        String value = getOptionalEnvValue(prefix, defaultKey, fallbackKeys);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing environment variable for keys: " + String.join(", ", buildCandidateKeys(prefix, defaultKey, fallbackKeys))
            );
        }
        return value;
    }

    private int getOptionalIntEnvValue(String prefix, int defaultValue, String defaultKey, String... fallbackKeys) {
        String value = getOptionalEnvValue(prefix, defaultKey, fallbackKeys);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private String getOptionalEnvValue(String prefix, String defaultKey, String... fallbackKeys) {
        for (String candidate : buildCandidateKeys(prefix, defaultKey, fallbackKeys)) {
            String value = System.getenv(candidate);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private List<String> buildCandidateKeys(String prefix, String defaultKey, String... fallbackKeys) {
        List<String> candidates = new ArrayList<>();
        String normalizedPrefix = normalizeKey(prefix);

        addCandidateKeys(candidates, normalizedPrefix, defaultKey);
        if (fallbackKeys != null) {
            for (String key : fallbackKeys) {
                addCandidateKeys(candidates, normalizedPrefix, key);
            }
        }
        return candidates;
    }

    private void addCandidateKeys(List<String> candidates, String prefix, String key) {
        String normalizedKey = normalizeKey(key);
        if (normalizedKey == null || normalizedKey.isBlank()) {
            return;
        }
        if (prefix != null && !prefix.isBlank()) {
            candidates.add(prefix + "_" + normalizedKey);
        }
        candidates.add(normalizedKey);
    }

    private String normalizeKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return key.replaceAll("[^A-Za-z0-9]", "_").toUpperCase();
    }
}

