package com.nivasafinance.common.events;

import lombok.Getter;
import lombok.ToString;

/**
 * Generic system event published within the platform.
 *
 * @param <T> payload type associated with the event
 */
@Getter
@ToString
public class SystemEvent<T> {

    /**
     * Logical event type identifier (e.g. {@code LEAD_CREATED}).
     */
    private final String eventType;

    /**
     * Event payload containing business data needed by listeners.
     */
    private final T payload;

    /**
     * Username of the user who triggered the event.
     * Captured at event creation time to preserve context for async handlers.
     */
    private final String username;

    public SystemEvent(String eventType, T payload) {
        this(eventType, payload, null);
    }

    public SystemEvent(String eventType, T payload, String username) {
        this.eventType = eventType;
        this.payload = payload;
        this.username = username;
    }
}


