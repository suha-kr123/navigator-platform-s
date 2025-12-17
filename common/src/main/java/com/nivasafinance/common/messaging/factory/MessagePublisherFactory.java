package com.nivasafinance.common.messaging.factory;

import com.nivasafinance.common.messaging.config.MessagingProperties;
import com.nivasafinance.common.messaging.enums.MessageProvider;
import com.nivasafinance.common.messaging.publisher.MessagePublisher;
import com.nivasafinance.common.messaging.publisher.impl.LocalMessagePublisher;
import com.nivasafinance.common.messaging.publisher.impl.SqsMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePublisherFactory {

    private final MessagingProperties messagingProperties;
    private final ObjectProvider<SqsMessagePublisher> sqsMessagePublisher;
    private final LocalMessagePublisher localMessagePublisher;

    /**
     * Returns the default publisher based on messaging.provider configuration.
     * This is the existing behavior - one provider for the entire application.
     */
    public MessagePublisher getPublisher() {
        return getPublisher(messagingProperties.getProvider());
    }

    /**
     * Returns a publisher for a specific provider type.
     * This allows selecting different providers programmatically.
     */
    public MessagePublisher getPublisher(MessageProvider provider) {
        return switch (provider) {
            case SQS -> {
                MessagePublisher publisher = sqsMessagePublisher.getIfAvailable();
                if (publisher == null) {
                    // SQS provider is configured but SqsMessagePublisher is not available
                    // This can happen if SqsClient bean creation failed or validation failed
                    // Fall back to LOCAL to prevent application crashes
                    log.warn("SQS provider is configured but SqsMessagePublisher is not available. " +
                            "This may indicate SQS configuration is incomplete or invalid. " +
                            "Falling back to LOCAL publisher. Check startup logs for SQS configuration errors.");
                    yield localMessagePublisher;
                }
                yield publisher;
            }
            case LOCAL -> localMessagePublisher;
            // Future providers (e.g., RABBITMQ) can be added here
        };
    }

    /**
     * Returns a publisher for a specific event type.
     * This allows different events to use different providers.
     * 
     * @param eventType The event type (e.g., "LEAD_CREATED", "LEAD_UPDATED")
     * @return The appropriate publisher for this event
     */
    public MessagePublisher getPublisherForEvent(String eventType) {
        // Check if there's a specific provider mapping for this event
        MessageProvider eventProvider = messagingProperties.getProviderForEvent(eventType);
        return getPublisher(eventProvider);
    }
}


