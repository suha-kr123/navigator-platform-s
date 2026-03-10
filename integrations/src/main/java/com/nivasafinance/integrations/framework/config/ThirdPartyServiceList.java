package com.nivasafinance.integrations.framework.config;

import lombok.Getter;

@Getter
public enum ThirdPartyServiceList {
    VOICE("voice"),
    WHATSAPP("whatsapp"),
    CREDIT_BUREAU("credit_bureau"),
    AUTHENTICATION("authentication");

    private final String serviceName;

    ThirdPartyServiceList(String serviceName) {
        this.serviceName = serviceName;
    }

}
