package com.nivasafinance.notification.orchestrator.payload;

import com.nivasafinance.common.events.BusinessEvent;

import java.util.Map;

/**
 * Builder interface for creating notification payloads based on business events.
 * Each event type should have its own implementation.
 */
public interface NotificationPayloadBuilder {

    /**
     * Checks if this builder supports the given event type.
     */
    boolean supports(BusinessEvent eventType);

    /**
     * Builds a notification payload map from the event payload.
     * The returned map will be stored in the notification record.
     */
    Map<String, Object> build(Object eventPayload);
}

