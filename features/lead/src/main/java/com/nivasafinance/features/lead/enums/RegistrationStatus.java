package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum RegistrationStatus {

    AVAILABLE("AVAILABLE"),
    APPLIED("APPLIED"),
    NOT_APPLIED("NOT_APPLIED");

    private final String key;

    RegistrationStatus(String key) {
        this.key = key;
    }

    public static RegistrationStatus fromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalized = key.trim().toUpperCase();
        for (RegistrationStatus value : values()) {
            if (value.key.equals(normalized)) {
                return value;
            }
        }
        return null;
    }
}
