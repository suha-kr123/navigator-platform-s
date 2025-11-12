package com.nivasafinance.integrations.framework;

import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;

import java.util.Map;

public interface ThirdPartyProvider<T> {
    ThirdPartyProviderList getKey();
    T setupConfiguration(Map<String, String> map);
}

