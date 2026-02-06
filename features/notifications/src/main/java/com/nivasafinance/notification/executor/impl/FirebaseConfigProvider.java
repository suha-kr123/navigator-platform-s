package com.nivasafinance.notification.executor.impl;

import com.nivasafinance.common.awssecretmanager.exception.AwsSecretManagerException;
import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides Firebase configuration for Cloud Messaging.
 * Supports both AWS Secrets Manager (production) and local configuration (development).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseConfigProvider {

    private final SecretManagerService secretManagerService;
    
    // Local configuration for development/testing
    @Value("${firebase.service-account.path:}")
    private String localServiceAccountPath;
    
    @Value("${firebase.service-account.json:}")
    private String localServiceAccountJson;
    
    // AWS Secrets Manager secret name
    private static final String FIREBASE_SECRET = "FIREBASE_SERVICE_ACCOUNT";
    
    // Cache for secrets (loaded once per application startup)
    private Map<String, Object> firebaseSecrets;
    private boolean secretsLoaded = false;

    /**
     * Gets Firebase configuration.
     * 
     * @return ThirdPartyConfig with Firebase credentials
     */
    public ThirdPartyConfig getConfig() {
        Map<String, String> configMap = new HashMap<>();
        
        // Check if we're in local/dev mode (local config provided)
        if (isLocalMode()) {
            log.info("Using local Firebase configuration");
            
            if (localServiceAccountPath != null && !localServiceAccountPath.trim().isEmpty()) {
                configMap.put("service_account_path", localServiceAccountPath);
            } else if (localServiceAccountJson != null && !localServiceAccountJson.trim().isEmpty()) {
                configMap.put("service_account_json", localServiceAccountJson);
            }
        } else {
            // Production: Load from AWS Secrets Manager
            log.info("Loading Firebase configuration from AWS Secrets Manager");
            
            Map<String, Object> secrets = loadSecrets();
            
            // Firebase service account JSON can be stored as a string in secrets
            if (secrets.containsKey("service_account_json")) {
                configMap.put("service_account_json", secrets.get("service_account_json").toString());
            } else if (secrets.containsKey("service_account_path")) {
                configMap.put("service_account_path", secrets.get("service_account_path").toString());
            } else {
                // If stored as individual fields, reconstruct JSON (or store as single JSON string)
                throw new IllegalStateException(
                        "Firebase service account must be provided as 'service_account_json' or 'service_account_path' in secrets");
            }
        }
        
        // Validate required fields
        if (configMap.get("service_account_json") == null && configMap.get("service_account_path") == null) {
            throw new IllegalStateException("Firebase service account not configured");
        }
        
        return new ThirdPartyConfig(
                null, // config ID (not needed for direct config)
                "Firebase Config",
                "FIREBASE",
                configMap
        );
    }

    /**
     * Checks if local configuration is available (development mode).
     */
    private boolean isLocalMode() {
        boolean hasPath = localServiceAccountPath != null && !localServiceAccountPath.trim().isEmpty();
        boolean hasJson = localServiceAccountJson != null && !localServiceAccountJson.trim().isEmpty();
        
        boolean isLocal = hasPath || hasJson;
        log.info("isLocalMode check: hasPath={}, hasJson={}, isLocal={}", hasPath, hasJson, isLocal);
        return isLocal;
    }

    /**
     * Loads secrets from AWS Secrets Manager.
     * Caches the results to avoid repeated API calls.
     */
    private Map<String, Object> loadSecrets() {
        // Load secrets once and cache them
        if (!secretsLoaded) {
            try {
                firebaseSecrets = secretManagerService.getSecret(FIREBASE_SECRET);
                log.info("Loaded Firebase secrets from AWS Secrets Manager");
            } catch (AwsSecretManagerException ex) {
                log.warn("Firebase secret '{}' not available: {}. Firebase app notifications will be disabled.",
                        FIREBASE_SECRET, ex.getMessage());
                throw new IllegalStateException("Firebase secret not found or unavailable", ex);
            }
            secretsLoaded = true;
        }
        return firebaseSecrets;
    }
}

