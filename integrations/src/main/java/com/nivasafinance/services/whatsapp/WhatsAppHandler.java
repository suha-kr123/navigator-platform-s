package com.nivasafinance.services.whatsapp;

import com.nivasafinance.integrations.framework.ThirdPartyHandler;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.exception.ServiceInvocationException;
import com.nivasafinance.integrations.framework.runner.ServiceRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.WhatsAppProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class WhatsAppHandler extends ThirdPartyHandler {
    
    private final Map<String, WhatsAppProvider<?>> servicesMap = new HashMap<>();

    @Autowired
    public WhatsAppHandler(Set<WhatsAppProvider<?>> services) {
        for (WhatsAppProvider<?> provider : services) {
            servicesMap.put(provider.getKey().getProvideName(), provider);
        }
    }
    
    public WhatsAppTemplateResponse sendTemplate(WhatsAppTemplateRequest request, BusinessContext businessContext) {
        WhatsAppProvider<?> primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new ServiceInvocationException("Error fetching " + getKey().getServiceName());
        }
        WhatsAppProvider<?> fallbackProvider = null;
        if (runConfig.getFallbackConfig() != null) {
            fallbackProvider = servicesMap.get(runConfig.getFallbackConfig().getProvider());
        }
        ServiceRunner<WhatsAppProvider<?>, WhatsAppTemplateRequest> runner = 
            new ServiceRunner<>(primaryProvider, fallbackProvider, runConfig.getRetries());
        return (WhatsAppTemplateResponse) runner.invokeService(
            "sendTemplate",
            request,
            runConfig,
            businessContext
        );
    }
    
    public WhatsAppTemplateResponse getTemplateStatus(String phoneNumber, BusinessContext businessContext) {
        WhatsAppProvider<?> primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new ServiceInvocationException("Error fetching " + getKey().getServiceName());
        }
        WhatsAppProvider<?> fallbackProvider = null;
        if (runConfig.getFallbackConfig() != null) {
            fallbackProvider = servicesMap.get(runConfig.getFallbackConfig().getProvider());
        }
        ServiceRunner<WhatsAppProvider<?>, String> runner = 
            new ServiceRunner<>(primaryProvider, fallbackProvider, runConfig.getRetries());
        return (WhatsAppTemplateResponse) runner.invokeService(
            "getTemplateStatus",
            phoneNumber,
            runConfig,
            businessContext
        );
    }
    
    @Override
    public ThirdPartyServiceList getKey() {
        return ThirdPartyServiceList.WHATSAPP;
    }
}

