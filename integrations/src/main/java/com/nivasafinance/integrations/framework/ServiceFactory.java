package com.nivasafinance.integrations.framework;

import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.exception.ServiceFactoryException;
import com.nivasafinance.integrations.framework.core.service.ThirdPartyServiceConfigReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class ServiceFactory<H extends ThirdPartyHandler> {

    private final Set<ThirdPartyHandler> services;
    private final ThirdPartyServiceConfigReadService thirdPartyServiceConfigReadService;
    private final Map<String, ThirdPartyHandler> servicesMap = new HashMap<>();

    @Autowired
    public ServiceFactory(
            Set<ThirdPartyHandler> services,
            ThirdPartyServiceConfigReadService thirdPartyServiceConfigReadService) {
        this.services = services;
        this.thirdPartyServiceConfigReadService = thirdPartyServiceConfigReadService;
        initializeServicesMap();
    }

    private void initializeServicesMap() {
        for (ThirdPartyHandler handler : services) {
            servicesMap.put(handler.getKey().getServiceName(), handler);
        }
    }

    @SuppressWarnings("unchecked")
    public H getHandler(ThirdPartyServiceList service) {
        var serviceConfig = thirdPartyServiceConfigReadService.findByService(service);
        var handler = servicesMap.get(service.getServiceName());
        if (handler == null) {
            throw new ServiceFactoryException("Error fetching " + service);
        }
        handler.setupConfig(serviceConfig);
        return (H) handler;
    }
}

