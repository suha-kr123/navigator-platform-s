package com.nivasafinance.features.lead.enums;

import lombok.Getter;

@Getter
public enum EkhataType {

    A_KHATA("A_KHATA"),
    B_KHATA("B_KHATA"),
    FORM_3("FORM_3"),
    UNKNOWN("UNKNOWN");

    private final String key;

    EkhataType(String key) {
        this.key = key;
    }

    public static EkhataType fromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalized = key.trim().toUpperCase();
        for (EkhataType value : values()) {
            if (value.key.equals(normalized)) {
                return value;
            }
        }
        return null;
    }
}
