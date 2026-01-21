package com.nivasafinance.common.messaging.config;

import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.common.messaging.enums.QueueType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Loads SQS configuration from AWS Secrets Manager at application startup.
 * 
 * <p>This component automatically detects SQS by attempting to load the QUEUE_SECRET from Secrets Manager.
 * If the secret exists and loads successfully, the provider is automatically set to SQS.
 * If the secret doesn't exist or fails to load, the provider remains LOCAL (default).</p>
 * 
 * <p>The secret name defaults to "QUEUE_SECRET" but can be overridden via {@code messaging.sqs-secret-name} property.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessagingSecretsLoader {

    /**
     * Default SQS secret name in AWS Secrets Manager.
     * Can be overridden via messaging.sqs-secret-name property.
     */
    private static final String DEFAULT_SQS_SECRET_NAME = "QUEUE_SECRET";

    private final MessagingProperties messagingProperties;
    private final SecretManagerService secretManagerService;

    @PostConstruct
    public void loadSqsSecrets() {
        // Check if auto-detection is disabled
        if (!messagingProperties.isAutoDetectSqs()) {
            log.info("SQS auto-detection is disabled. Using explicitly configured provider: {}", 
                    messagingProperties.getProvider());
            return;
        }

        // If provider is explicitly set to SQS via property, use that
        // Otherwise, try to auto-detect by loading QUEUE_SECRET
        if (messagingProperties.getProvider() == com.nivasafinance.common.messaging.enums.MessageProvider.SQS) {
            log.info("SQS provider explicitly configured via property, loading from Secrets Manager");
            loadSqsConfiguration();
        } else {
            // Auto-detect: Try to load QUEUE_SECRET
            // If successful, automatically enable SQS provider
            log.debug("SQS provider not explicitly set, attempting auto-detection via QUEUE_SECRET");
            if (tryLoadSqsConfiguration()) {
                log.info("Successfully auto-detected SQS configuration from Secrets Manager. Provider set to SQS.");
                messagingProperties.setProvider(com.nivasafinance.common.messaging.enums.MessageProvider.SQS);
            } else {
                log.debug("QUEUE_SECRET not found or failed to load. Using LOCAL provider (default).");
            }
        }
    }

    /**
     * Attempts to load SQS configuration from Secrets Manager.
     * Returns true if successful, false otherwise (doesn't throw exceptions).
     */
    private boolean tryLoadSqsConfiguration() {
        String secretName = getSecretName();
        
        try {
            log.info("Attempting to load SQS configuration from AWS Secrets Manager: {}", secretName);
            Map<String, Object> secretMap = secretManagerService.getSecret(secretName);
            
            if (secretMap == null || secretMap.isEmpty()) {
                log.warn("Secret '{}' loaded but is null or empty", secretName);
                return false;
            }
            
            log.info("Secret '{}' loaded successfully. Keys in secret: {}", secretName, secretMap.keySet());
            log.debug("Secret '{}' full content: {}", secretName, secretMap);
            
            // Validate secret structure before loading
            if (!secretMap.containsKey("region")) {
                log.warn("Secret '{}' missing required 'region' field", secretName);
                return false;
            }
            if (!secretMap.containsKey("queues")) {
                log.warn("Secret '{}' missing required 'queues' field", secretName);
                return false;
            }
            
            messagingProperties.getSqs().loadFromSecretsManager(secretMap);
            
            // Verify configuration was loaded correctly
            String region = messagingProperties.getSqs().getRegion();
            Map<QueueType, String> queues = messagingProperties.getSqs().getQueues();
            
            if (region == null || region.isBlank()) {
                log.warn("Region not set after loading secret '{}'", secretName);
                return false;
            }
            if (queues.isEmpty()) {
                log.warn("No queues loaded from secret '{}'", secretName);
                return false;
            }
            
            log.info("Successfully loaded SQS configuration from Secrets Manager. Region: {}, Queues: {}", 
                    region, queues.keySet());
            return true;
        } catch (Exception ex) {
            log.warn("Failed to load SQS configuration from AWS Secrets Manager: {}. Error: {}. Using LOCAL provider (default).", 
                    secretName, ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Loads SQS configuration from Secrets Manager (throws exception on failure).
     * Used when provider is explicitly set to SQS.
     */
    private void loadSqsConfiguration() {
        String secretName = getSecretName();

        try {
            log.info("Loading SQS configuration from AWS Secrets Manager: {}", secretName);
            Map<String, Object> secretMap = secretManagerService.getSecret(secretName);
            messagingProperties.getSqs().loadFromSecretsManager(secretMap);
            log.info("Successfully loaded SQS configuration from Secrets Manager. Region: {}, Queues: {}", 
                    messagingProperties.getSqs().getRegion(), 
                    messagingProperties.getSqs().getQueues().keySet());
        } catch (Exception ex) {
            log.error("Failed to load SQS configuration from AWS Secrets Manager: {}", secretName, ex);
            throw new IllegalStateException("Failed to load SQS configuration from Secrets Manager: " + secretName, ex);
        }
    }

    private String getSecretName() {
        String secretName = messagingProperties.getSqsSecretName();
        if (secretName == null || secretName.isBlank()) {
            secretName = DEFAULT_SQS_SECRET_NAME;
        }
        return secretName;
    }
}

