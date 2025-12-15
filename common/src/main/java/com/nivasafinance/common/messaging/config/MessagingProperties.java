package com.nivasafinance.common.messaging.config;

import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "messaging")
public class MessagingProperties {

    private MessageProvider provider = MessageProvider.LOCAL;

    /**
     * Optional: Map of event types to specific providers.
     * If an event type is not in this map, the default provider is used.
     * Example: LEAD_CREATED -> SQS, LEAD_UPDATED -> RABBITMQ
     */
    private Map<String, MessageProvider> eventProviders = new java.util.HashMap<>();

    /**
     * Optional: Name of the AWS Secrets Manager secret containing SQS configuration.
     * If provided, SQS config will be loaded from Secrets Manager instead of properties files.
     */
    private String sqsSecretName;

    private final SqsProperties sqs = new SqsProperties();

    @Data
    public static class SqsProperties {
        private String region;
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private int waitTimeSeconds = 10;
        private int maxMessages = 5;
        private long pollDelayMs = 1_000L;
        private Map<QueueType, String> queues = new EnumMap<>(QueueType.class);

        public String resolveQueueUrl(QueueType queueType) {
            String queueUrl = queues.get(queueType);
            if (queueUrl == null || queueUrl.isBlank()) {
                throw new IllegalStateException("Queue URL not configured for type: " + queueType);
            }
            return queueUrl;
        }

        /**
         * Loads SQS configuration from AWS Secrets Manager secret map.
         * Expected structure:
         * {
         *   "region": "us-east-1",
         *   "accessKey": "...",
         *   "secretKey": "...",
         *   "queues": {
         *     "NOTIFICATION": "https://sqs.us-east-1.amazonaws.com/123456789012/queue-name",
         *     "NOTIFICATION_EXECUTOR": "...",
         *     "AUDIT": "..."
         *   }
         * }
         */
        @SuppressWarnings("unchecked")
        public void loadFromSecretsManager(Map<String, Object> secretMap) {
            if (secretMap == null || secretMap.isEmpty()) {
                throw new IllegalArgumentException("Secret map is null or empty");
            }

            // Load basic SQS config
            if (secretMap.containsKey("region")) {
                this.region = (String) secretMap.get("region");
            }
            if (secretMap.containsKey("accessKey")) {
                this.accessKey = (String) secretMap.get("accessKey");
            }
            if (secretMap.containsKey("secretKey")) {
                this.secretKey = (String) secretMap.get("secretKey");
            }

            // Load queue URLs and map them to QueueType enum
            if (secretMap.containsKey("queues")) {
                Map<String, String> queuesFromSecret = (Map<String, String>) secretMap.get("queues");
                if (queuesFromSecret != null) {
                    for (Map.Entry<String, String> entry : queuesFromSecret.entrySet()) {
                        try {
                            QueueType queueType = QueueType.valueOf(entry.getKey().toUpperCase());
                            this.queues.put(queueType, entry.getValue());
                        } catch (IllegalArgumentException ex) {
                            // Ignore unknown queue types
                            // Log warning if needed
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the provider for a specific event type.
     * If the event has a specific provider mapping, returns that.
     * Otherwise, returns the default provider.
     */
    public MessageProvider getProviderForEvent(String eventType) {
        return eventProviders.getOrDefault(eventType, provider);
    }
}


