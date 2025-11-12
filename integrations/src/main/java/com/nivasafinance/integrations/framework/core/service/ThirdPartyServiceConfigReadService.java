package com.nivasafinance.integrations.framework.core.service;

import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.exception.ServiceConfigurationException;
import com.nivasafinance.integrations.framework.core.repository.ThirdPartyServiceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ThirdPartyServiceConfigReadService {

    private final ThirdPartyServiceConfigRepository thirdPartyServiceConfigRepository;
    private final ThirdPartyProviderConfigReadService thirdPartyProviderConfigReadService;

    @Autowired
    public ThirdPartyServiceConfigReadService(
            ThirdPartyServiceConfigRepository thirdPartyServiceConfigRepository,
            ThirdPartyProviderConfigReadService thirdPartyProviderConfigReadService) {
        this.thirdPartyServiceConfigRepository = thirdPartyServiceConfigRepository;
        this.thirdPartyProviderConfigReadService = thirdPartyProviderConfigReadService;
    }

    public RunConfig findByService(ThirdPartyServiceList service) {
        var serviceEntity = thirdPartyServiceConfigRepository.findByServiceAndIsActiveTrue(service.getServiceName())
                .orElseThrow(() -> new ServiceConfigurationException("Error fetching " + service));

        var primaryConfig = thirdPartyProviderConfigReadService.getProviderConfigById(serviceEntity.getPrimaryConfigKey());
        var fallbackConfig = serviceEntity.getFallbackConfigKey() != null
                ? thirdPartyProviderConfigReadService.getProviderConfigById(serviceEntity.getFallbackConfigKey())
                : null;
        
        return new RunConfig(primaryConfig, fallbackConfig, serviceEntity.getRetryCount());
    }
}

