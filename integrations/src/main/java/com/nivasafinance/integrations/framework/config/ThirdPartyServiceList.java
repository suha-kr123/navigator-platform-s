package com.nivasafinance.integrations.framework.config;

import lombok.Getter;

@Getter
public enum ThirdPartyServiceList {
    VOICE("voice"),
    WHATSAPP("whatsapp");

    private final String serviceName;

    ThirdPartyServiceList(String serviceName) {
        this.serviceName = serviceName;
    }

}
