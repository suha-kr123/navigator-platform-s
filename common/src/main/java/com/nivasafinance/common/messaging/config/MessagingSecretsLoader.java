package com.nivasafinance.common.messaging.config;

import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Loads SQS configuration from AWS Secrets Manager at application startup.
 * 
 * <p>This component is only active when {@code messaging.sqs-secret-name} is configured.
 * If the secret name is not provided, the application will use values from
 * application properties files.</p>
 * 
 * <p>The secret in AWS Secrets Manager should be a JSON object with the following structure:
 * <pre>
 * {
 *   "region": "us-east-1",
 *   "accessKey": "AKIAIOSFODNN7EXAMPLE",
 *   "secretKey": "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
 *   "queues": {
 *     "NOTIFICATION": "https://sqs.us-east-1.amazonaws.com/123456789012/notification-queue",
 *     "NOTIFICATION_EXECUTOR": "https://sqs.us-east-1.amazonaws.com/123456789012/executor-queue",
 *     "AUDIT": "https://sqs.us-east-1.amazonaws.com/123456789012/audit-queue"
 *   }
 * }
 * </pre>
 * </p>
 */
@Component
@ConditionalOnProperty(name = "messaging.sqs-secret-name")
@RequiredArgsConstructor
@Slf4j
public class MessagingSecretsLoader {

    private final MessagingProperties messagingProperties;
    private final SecretManagerService secretManagerService;

    @PostConstruct
    public void loadSqsSecrets() {
        String secretName = messagingProperties.getSqsSecretName();
        if (secretName == null || secretName.isBlank()) {
            log.warn("SQS secret name not configured, using properties file values");
            return;
        }

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
}

