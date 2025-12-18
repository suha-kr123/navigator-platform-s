package com.nivasafinance.notification.executor.impl;

import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides WATI configuration based on recipient type.
 * Handles different WATI accounts for customer (LEAD) vs advisor (ADVISOR).
 * Supports both AWS Secrets Manager (production) and local configuration (development).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WatiConfigProvider {

    private final SecretManagerService secretManagerService;
    
    // Local configuration for development/testing
    @Value("${wati.customer.api-key:}")
    private String localCustomerApiKey;
    
    @Value("${wati.customer.base-url:}")
    private String localCustomerBaseUrl;
    
    @Value("${wati.customer.sender-id:}")
    private String localCustomerSenderId;
    
    @Value("${wati.advisor.api-key:}")
    private String localAdvisorApiKey;
    
    @Value("${wati.advisor.base-url:}")
    private String localAdvisorBaseUrl;
    
    @Value("${wati.advisor.sender-id:}")
    private String localAdvisorSenderId;
    
    // AWS Secrets Manager secret names
    private static final String CUSTOMER_WATI_SECRET = "CUSTOMER_WATI";
    private static final String ADVISOR_WATI_SECRET = "ADVISOR_WATI"; // Adjust if different
    
    // Cache for secrets (loaded once per application startup)
    private Map<String, Object> customerWatiSecrets;
    private Map<String, Object> advisorWatiSecrets;
    private boolean secretsLoaded = false;

    /**
     * Gets WATI configuration for the specified recipient type.
     * 
     * @param recipientType "LEAD" for customer, "ADVISOR" for advisor
     * @return ThirdPartyConfig with WATI credentials
     */
    public ThirdPartyConfig getConfigForRecipient(String recipientType) {
        boolean isCustomer = "LEAD".equalsIgnoreCase(recipientType);
        
        Map<String, String> configMap = new HashMap<>();
        
        // Debug: Log local config values
        log.debug("Local WATI config check - Customer API Key: '{}', Base URL: '{}'", 
                localCustomerApiKey, localCustomerBaseUrl);
        
        // Check if we're in local/dev mode (local config provided)
        if (isLocalMode()) {
            log.info("Using local WATI configuration for recipient type: {}", recipientType);
            
            if (isCustomer) {
                configMap.put("access_token", localCustomerApiKey);
                configMap.put("api_endpoint", localCustomerBaseUrl);
                configMap.put("client_id", localCustomerSenderId);
            } else {
                configMap.put("access_token", localAdvisorApiKey);
                configMap.put("api_endpoint", localAdvisorBaseUrl);
                configMap.put("client_id", localAdvisorSenderId);
            }
        } else {
            // Production: Load from AWS Secrets Manager
            log.info("Loading WATI configuration from AWS Secrets Manager for recipient type: {}", recipientType);
            
            Map<String, Object> secrets = loadSecretsForRecipient(recipientType);
            
            configMap.put("access_token", secrets.get("api_key").toString());
            configMap.put("api_endpoint", secrets.get("base_url").toString());
            configMap.put("client_id", secrets.get("sender_id").toString());
        }
        
        // Validate required fields
        if (configMap.get("access_token") == null || configMap.get("access_token").isEmpty()) {
            throw new IllegalStateException(
                    String.format("WATI access_token not configured for recipient type: %s", recipientType));
        }
        if (configMap.get("api_endpoint") == null || configMap.get("api_endpoint").isEmpty()) {
            throw new IllegalStateException(
                    String.format("WATI api_endpoint not configured for recipient type: %s", recipientType));
        }
        
        return new ThirdPartyConfig(
                null, // config ID (not needed for direct config)
                isCustomer ? "Customer WATI Config" : "Advisor WATI Config",
                "WATI",
                configMap
        );
    }

    /**
     * Checks if local configuration is available (development mode).
     * A valid local config must have BOTH api-key AND base-url.
     */
    private boolean isLocalMode() {
        boolean hasCustomerConfig = localCustomerApiKey != null && !localCustomerApiKey.trim().isEmpty() &&
                                     localCustomerBaseUrl != null && !localCustomerBaseUrl.trim().isEmpty();
        boolean hasAdvisorConfig = localAdvisorApiKey != null && !localAdvisorApiKey.trim().isEmpty() &&
                                    localAdvisorBaseUrl != null && !localAdvisorBaseUrl.trim().isEmpty();
        
        boolean isLocal = hasCustomerConfig || hasAdvisorConfig;
        log.info("isLocalMode check: hasCustomerConfig={}, hasAdvisorConfig={}, isLocal={}", 
                hasCustomerConfig, hasAdvisorConfig, isLocal);
        return isLocal;
    }

    /**
     * Loads secrets from AWS Secrets Manager for the specified recipient type.
     * Caches the results to avoid repeated API calls.
     */
    private Map<String, Object> loadSecretsForRecipient(String recipientType) {
        boolean isCustomer = "LEAD".equalsIgnoreCase(recipientType);
        
        // Load secrets once and cache them
        if (!secretsLoaded) {
            try {
                customerWatiSecrets = secretManagerService.getSecret(CUSTOMER_WATI_SECRET);
                log.info("Loaded customer WATI secrets from AWS Secrets Manager");
            } catch (Exception ex) {
                log.error("Failed to load customer WATI secrets from AWS Secrets Manager", ex);
                throw new IllegalStateException("Failed to load customer WATI secrets", ex);
            }
            
            try {
                advisorWatiSecrets = secretManagerService.getSecret(ADVISOR_WATI_SECRET);
                log.info("Loaded advisor WATI secrets from AWS Secrets Manager");
            } catch (Exception ex) {
                log.warn("Failed to load advisor WATI secrets from AWS Secrets Manager. " +
                        "Will use customer WATI config as fallback.", ex);
                // Fallback to customer config if advisor config not found
                advisorWatiSecrets = customerWatiSecrets;
            }
            
            secretsLoaded = true;
        }
        
        return isCustomer ? customerWatiSecrets : advisorWatiSecrets;
    }
}

