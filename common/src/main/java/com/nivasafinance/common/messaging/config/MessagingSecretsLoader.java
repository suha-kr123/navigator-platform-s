package com.nivasafinance.common.messaging.config;

import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
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
            messagingProperties.getSqs().loadFromSecretsManager(secretMap);
            log.info("Successfully loaded SQS configuration from Secrets Manager. Region: {}, Queues: {}", 
                    messagingProperties.getSqs().getRegion(), 
                    messagingProperties.getSqs().getQueues().keySet());
            return true;
        } catch (Exception ex) {
            log.debug("Failed to load SQS configuration from AWS Secrets Manager: {}. Error: {}", 
                    secretName, ex.getMessage());
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

