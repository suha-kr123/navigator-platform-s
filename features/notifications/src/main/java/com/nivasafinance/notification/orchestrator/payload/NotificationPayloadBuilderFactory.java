package com.nivasafinance.notification.orchestrator.payload;

import com.nivasafinance.common.events.BusinessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Factory for building notification payloads based on business events.
 */
@Component
@RequiredArgsConstructor
public class NotificationPayloadBuilderFactory {

    private final List<NotificationPayloadBuilder> builders;

    /**
     * Builds a notification payload map for the given event type and payload.
     */
    public Map<String, Object> build(BusinessEvent eventType, Object eventPayload) {
        return builders.stream()
                .filter(builder -> builder.supports(eventType))
                .findFirst()
                .map(builder -> builder.build(eventPayload))
                .orElseThrow(() -> new IllegalStateException("No notification payload builder registered for event: " + eventType));
    }
}

