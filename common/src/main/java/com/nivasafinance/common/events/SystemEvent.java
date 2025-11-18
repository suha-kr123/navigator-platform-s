package com.nivasafinance.common.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Generic system event published within the platform.
 *
 * @param <T> payload type associated with the event
 */
@Getter
@ToString
@RequiredArgsConstructor
public class SystemEvent<T> {

    /**
     * Logical event type identifier (e.g. {@code LEAD_CREATED}).
     */
    private final String eventType;

    /**
     * Event payload containing business data needed by listeners.
     */
    private final T payload;
}


