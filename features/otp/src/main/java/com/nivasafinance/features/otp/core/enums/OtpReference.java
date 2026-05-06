package com.nivasafinance.features.otp.core.enums;

public enum OtpReference {
    VERIFY_LEAD_FOR_CB;

    public String getConfigKey() {
        return name();
    }
}
