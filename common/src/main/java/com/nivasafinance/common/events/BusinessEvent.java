package com.nivasafinance.common.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumerates business events published by the platform.
 */
@Getter
@RequiredArgsConstructor
public enum BusinessEvent {

    /**
     * Raised when a new lead is created in the system.
     */
    LEAD_CREATED("LEAD_CREATED");

    private final String code;

    @Override
    public String toString() {
        return code;
    }
}


