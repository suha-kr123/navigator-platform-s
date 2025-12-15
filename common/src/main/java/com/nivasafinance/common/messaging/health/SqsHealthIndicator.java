package com.nivasafinance.common.messaging.health;

import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.QueueType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "messaging.provider", havingValue = "SQS")
public class SqsHealthIndicator implements HealthIndicator {

    private final SqsClient sqsClient;
    private final MessagingProperties messagingProperties;

    @Override
    public Health health() {
        try {
            // Only check queues that are configured (AUDIT is optional)
            for (QueueType queueType : QueueType.values()) {
                String queueUrl = messagingProperties.getSqs().getQueues().get(queueType);
                if (queueUrl != null && !queueUrl.isBlank()) {
                sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                        .queueUrl(queueUrl)
                        .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
                        .build());
                }
            }
            return Health.up().build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}


