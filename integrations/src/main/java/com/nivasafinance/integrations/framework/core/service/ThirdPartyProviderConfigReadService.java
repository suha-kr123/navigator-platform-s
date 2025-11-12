package com.nivasafinance.integrations.framework.core.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.ServiceConfigurationException;
import com.nivasafinance.integrations.framework.core.repository.ThirdPartyProviderConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ThirdPartyProviderConfigReadService {

    private final ThirdPartyProviderConfigRepository thirdPartyProviderConfigRepository;
    private final Gson gson = new Gson();

    @Autowired
    public ThirdPartyProviderConfigReadService(ThirdPartyProviderConfigRepository thirdPartyProviderConfigRepository) {
        this.thirdPartyProviderConfigRepository = thirdPartyProviderConfigRepository;
    }

    public ThirdPartyConfig getProviderConfigById(UUID id) {
        var configEntity = thirdPartyProviderConfigRepository.findById(id)
                .orElseThrow(() -> new ServiceConfigurationException("Config not found: " + id));
        
        if (!configEntity.getActive()) {
            throw new ServiceConfigurationException("Config " + id + " is not active");
        }
        
        Type type = new TypeToken<HashMap<String, String>>() {}.getType();
        Map<String, String> configurations = gson.fromJson(configEntity.getConfigs(), type);
        
        return new ThirdPartyConfig(
                configEntity.getId(),
                configEntity.getName(),
                configEntity.getProvider(),
                configurations
        );
    }
}

