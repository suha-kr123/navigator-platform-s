package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum AvailabilityStatus {

    AVAILABLE("AVAILABLE"),
    NOT_AVAILABLE("NOT_AVAILABLE");

    private final String key;

    AvailabilityStatus(String key) {
        this.key = key;
    }

    public static AvailabilityStatus fromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalized = key.trim().toUpperCase();
        for (AvailabilityStatus value : values()) {
            if (value.key.equals(normalized)) {
                return value;
            }
        }
        return null;
    }
}
