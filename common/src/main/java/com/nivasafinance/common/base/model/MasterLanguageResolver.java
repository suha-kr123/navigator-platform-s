package com.nivasafinance.common.base.model;

import java.util.Map;

import com.nivasafinance.common.enums.SupportedLocale;

import org.springframework.context.i18n.LocaleContextHolder;

public final class MasterLanguageResolver {

    private MasterLanguageResolver() {
    }

    public static String getDisplayValue(MasterLanguageData data) {
        if (data == null) {
            return "";
        }
        SupportedLocale locale = SupportedLocale.from(
                LocaleContextHolder.getLocale() != null ? LocaleContextHolder.getLocale().getLanguage() : null);
        String value = resolveValue(data, locale);
        return value != null ? value : "";
    }

    public static Map<String, String> getDisplayMap(MasterLanguageData data) {
        if (data == null) {
            return Map.of();
        }
        SupportedLocale locale = SupportedLocale.from(
                LocaleContextHolder.getLocale() != null ? LocaleContextHolder.getLocale().getLanguage() : null);
        String value = resolveValue(data, locale);
        if (value == null) {
            return data.toMap();
        }
        return Map.of(locale.getCode(), value);
    }

    private static String resolveValue(MasterLanguageData data, SupportedLocale locale) {
        if (locale == SupportedLocale.KN && data.getKn() != null && !data.getKn().isBlank()) {
            return data.getKn();
        }
        return data.getDefaultValue();
    }
}
