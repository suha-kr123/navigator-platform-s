package com.nivasafinance.features.otp.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OtpChannel {
    WHATSAPP("whatsapp"),
    SMS("sms");

    private final String jsonValue;

    OtpChannel(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static OtpChannel fromValue(String value) {
        for (OtpChannel channel : values()) {
            if (channel.jsonValue.equalsIgnoreCase(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unsupported OTP channel: " + value);
    }
}
