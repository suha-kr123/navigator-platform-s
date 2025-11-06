package framework;

import framework.config.ThirdPartyProviderList;

import java.util.Map;

public interface ThirdPartyProvider<T> {
    ThirdPartyProviderList getKey();
    T setupConfiguration(Map<String, String> map);
}

