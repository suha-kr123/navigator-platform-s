package com.nivasafinance.notification.executor.impl;

import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides Gallabox configuration based on recipient type.
 * Handles different Gallabox accounts for customer (LEAD) vs advisor (ADVISOR).
 * Supports both AWS Secrets Manager (production) and local configuration (development).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GallaboxConfigProvider {

    private final SecretManagerService secretManagerService;
    
    // Local configuration for development/testing
    @Value("${gallabox.customer.api-key:}")
    private String localCustomerApiKey;
    
    @Value("${gallabox.customer.api-secret:}")
    private String localCustomerApiSecret;
    
    @Value("${gallabox.customer.channel-id:}")
    private String localCustomerChannelId;
    
    @Value("${gallabox.advisor.api-key:}")
    private String localAdvisorApiKey;
    
    @Value("${gallabox.advisor.api-secret:}")
    private String localAdvisorApiSecret;
    
    @Value("${gallabox.advisor.channel-id:}")
    private String localAdvisorChannelId;
    
    // AWS Secrets Manager secret names
    private static final String CUSTOMER_GALLABOX_SECRET = "CUSTOMER_GALLABOX";
    private static final String ADVISOR_GALLABOX_SECRET = "ADVISOR_GALLABOX";
    
    // Cache for secrets (loaded once per application startup)
    private Map<String, Object> customerGallaboxSecrets;
    private Map<String, Object> advisorGallaboxSecrets;
    private boolean secretsLoaded = false;

    /**
     * Gets Gallabox configuration for the specified recipient type.
     * 
     * @param recipientType "LEAD" for customer, "ADVISOR" for advisor
     * @return ThirdPartyConfig with Gallabox credentials
     */
    public ThirdPartyConfig getConfigForRecipient(String recipientType) {
        boolean isCustomer = "LEAD".equalsIgnoreCase(recipientType);
        
        Map<String, String> configMap = new HashMap<>();
        
        // Debug: Log local config values
        log.debug("Local Gallabox config check - Customer API Key: '{}', Channel ID: '{}'", 
                localCustomerApiKey, localCustomerChannelId);
        
        // Check if we're in local/dev mode (local config provided)
        if (isLocalMode()) {
            log.info("Using local Gallabox configuration for recipient type: {}", recipientType);
            
            if (isCustomer) {
                configMap.put("api_key", localCustomerApiKey);
                configMap.put("api_secret", localCustomerApiSecret);
                configMap.put("channel_id", localCustomerChannelId);
            } else {
                configMap.put("api_key", localAdvisorApiKey);
                configMap.put("api_secret", localAdvisorApiSecret);
                configMap.put("channel_id", localAdvisorChannelId);
            }
        } else {
            // Production: Load from AWS Secrets Manager
            log.info("Loading Gallabox configuration from AWS Secrets Manager for recipient type: {}", recipientType);
            
            Map<String, Object> secrets = loadSecretsForRecipient(recipientType);
            
            configMap.put("api_key", secrets.get("apiKey").toString());
            configMap.put("api_secret", secrets.get("apiSecret").toString());
            configMap.put("channel_id", secrets.get("channelId").toString());
        }
        
        // Validate required fields
        if (configMap.get("api_key") == null || configMap.get("api_key").isEmpty()) {
            throw new IllegalStateException(
                    String.format("Gallabox API key not configured for recipient type: %s", recipientType));
        }
        if (configMap.get("api_secret") == null || configMap.get("api_secret").isEmpty()) {
            throw new IllegalStateException(
                    String.format("Gallabox API secret not configured for recipient type: %s", recipientType));
        }
        if (configMap.get("channel_id") == null || configMap.get("channel_id").isEmpty()) {
            throw new IllegalStateException(
                    String.format("Gallabox channel ID not configured for recipient type: %s", recipientType));
        }
        
        return new ThirdPartyConfig(
                null, // config ID (not needed for direct config)
                isCustomer ? "Customer Gallabox Config" : "Advisor Gallabox Config",
                "GALLABOX",
                configMap
        );
    }

    /**
     * Checks if local configuration is available (development mode).
     * A valid local config must have api-key, api-secret, and channel-id.
     */
    private boolean isLocalMode() {
        boolean hasCustomerConfig = localCustomerApiKey != null && !localCustomerApiKey.trim().isEmpty() &&
                                     localCustomerApiSecret != null && !localCustomerApiSecret.trim().isEmpty() &&
                                     localCustomerChannelId != null && !localCustomerChannelId.trim().isEmpty();
        boolean hasAdvisorConfig = localAdvisorApiKey != null && !localAdvisorApiKey.trim().isEmpty() &&
                                    localAdvisorApiSecret != null && !localAdvisorApiSecret.trim().isEmpty() &&
                                    localAdvisorChannelId != null && !localAdvisorChannelId.trim().isEmpty();
        
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
                customerGallaboxSecrets = secretManagerService.getSecret(CUSTOMER_GALLABOX_SECRET);
                log.info("Loaded customer Gallabox secrets from AWS Secrets Manager");
            } catch (Exception ex) {
                log.error("Failed to load customer Gallabox secrets from AWS Secrets Manager", ex);
                throw new IllegalStateException("Failed to load customer Gallabox secrets", ex);
            }
            
            try {
                advisorGallaboxSecrets = secretManagerService.getSecret(ADVISOR_GALLABOX_SECRET);
                log.info("Loaded advisor Gallabox secrets from AWS Secrets Manager");
            } catch (Exception ex) {
                log.warn("Failed to load advisor Gallabox secrets from AWS Secrets Manager. " +
                        "Will use customer Gallabox config as fallback.", ex);
                // Fallback to customer config if advisor config not found
                advisorGallaboxSecrets = customerGallaboxSecrets;
            }
            
            secretsLoaded = true;
        }
        
        return isCustomer ? customerGallaboxSecrets : advisorGallaboxSecrets;
    }
}
