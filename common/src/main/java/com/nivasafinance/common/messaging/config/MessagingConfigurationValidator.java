package com.nivasafinance.common.messaging.config;

import com.nivasafinance.common.messaging.enums.QueueType;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagingConfigurationValidator {

    private final MessagingProperties messagingProperties;

    public void validateSqsConfiguration() {
        MessagingProperties.SqsProperties sqs = messagingProperties.getSqs();
        if (!StringUtils.hasText(sqs.getRegion())) {
            throw new IllegalStateException("messaging.sqs.region must be configured");
        }

        // Only validate required queues for notifications
        // AUDIT queue is optional and not required
        QueueType[] requiredQueues = {QueueType.NOTIFICATION, QueueType.NOTIFICATION_EXECUTOR};
        
        for (QueueType queueType : requiredQueues) {
            String queueUrl = sqs.getQueues().get(queueType);
            if (!StringUtils.hasText(queueUrl)) {
                throw new IllegalStateException("Queue URL missing for type: " + queueType);
            }
        }
    }
}


