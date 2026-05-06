package com.nivasafinance.features.otp.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum OtpChannel {
    WHATSAPP,
    SMS,
    EMAIL;

    @JsonCreator
    public static OtpChannel fromConfigValue(String value) {
        return OtpChannel.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @JsonValue
    public String configValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
