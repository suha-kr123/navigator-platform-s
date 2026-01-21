package com.nivasafinance.integrations.framework.config;

public enum ThirdPartyProviderList {
    EXOTEL("exotel"),
    WATI("wati"),
    GALLABOX("gallabox");


    private final String provideName;

    ThirdPartyProviderList(String provideName) {
        this.provideName = provideName;
    }

    public String getProvideName() {
        return provideName;
    }
}
