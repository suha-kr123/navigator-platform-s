package com.nivasafinance.common.enums;

import lombok.Getter;

@Getter
public enum SupportedLocale {
    DEFAULT("default"),
    KN("kn");

    private final String code;

    SupportedLocale(String code) {
        this.code = code;
    }

    public static SupportedLocale from(String requestLanguage) {
        if (requestLanguage == null || requestLanguage.isBlank()) {
            return DEFAULT;
        }
        String normalized = requestLanguage.trim().toLowerCase();
        if ("kn".equals(normalized)) {
            return KN;
        }
        return DEFAULT;
    }
}
