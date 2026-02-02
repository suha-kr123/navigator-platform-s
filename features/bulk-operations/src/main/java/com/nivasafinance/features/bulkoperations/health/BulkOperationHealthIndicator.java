package com.nivasafinance.features.bulkoperations.health;

import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.enums.QueueType;
import com.nivasafinance.features.document.storage.ContentRepositoryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

/**
 * Health indicator for bulk operations: SQS queues (when provider is SQS) and storage (ContentRepository).
 */
@Component
@RequiredArgsConstructor
public class BulkOperationHealthIndicator implements HealthIndicator {

    private static final String BULK_OPERATION = "bulkOperation";

    private final MessagingProperties messagingProperties;
    private final ContentRepositoryFactory contentRepositoryFactory;

    @Value("${document.storage.provider:LOCAL}")
    private String storageProvider;

    private final org.springframework.beans.factory.ObjectProvider<SqsClient> sqsClientProvider;

    @Override
    public Health health() {
        java.util.Map<String, Object> details = new java.util.LinkedHashMap<>();
        boolean queueUp = true;
        boolean storageUp = true;

        if (messagingProperties.getProvider() == MessageProvider.SQS) {
            queueUp = checkQueueHealth(details);
        }

        storageUp = checkStorageHealth(details);

        Health.Builder builder = (queueUp && storageUp) ? Health.up() : Health.down();
        details.forEach(builder::withDetail);
        return builder.build();
    }

    private boolean checkQueueHealth(java.util.Map<String, Object> details) {
        SqsClient sqsClient = sqsClientProvider.getIfAvailable();
        if (sqsClient == null) {
            details.put(BULK_OPERATION, "SQS client not available");
            return false;
        }
        try {
            for (QueueType queueType : new QueueType[]{
                    QueueType.BULK_OPERATION_VALIDATION,
                    QueueType.BULK_OPERATION_PROCESSING
            }) {
                String queueUrl = messagingProperties.getSqs().resolveQueueUrl(queueType);
                sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
                        .build());
            }
            details.put(BULK_OPERATION, "SQS queues reachable");
            return true;
        } catch (Exception ex) {
            details.put(BULK_OPERATION, "SQS error: " + ex.getMessage());
            details.put("error", ex.getMessage());
            return false;
        }
    }

    private boolean checkStorageHealth(java.util.Map<String, Object> details) {
        try {
            contentRepositoryFactory.getRepository(storageProvider);
            details.put("storage", "ContentRepository available");
            return true;
        } catch (Exception ex) {
            details.put("storage", "ContentRepository error: " + ex.getMessage());
            details.put("storageError", ex.getMessage());
            return false;
        }
    }
}
