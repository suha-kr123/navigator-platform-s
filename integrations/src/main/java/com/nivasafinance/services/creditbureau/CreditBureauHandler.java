package com.nivasafinance.services.creditbureau;

import com.nivasafinance.integrations.framework.ThirdPartyHandler;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.exception.ServiceInvocationException;
import com.nivasafinance.integrations.framework.runner.ServiceRunner;
import com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.services.creditbureau.dto.CreditBureauProviderResponse;
import com.nivasafinance.services.creditbureau.dto.PullEnquiryRequest;
import com.nivasafinance.services.creditbureau.provider.CreditBureauProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CreditBureauHandler extends ThirdPartyHandler {

    private final Map<String, CreditBureauProvider> servicesMap = new HashMap<>();

    @Autowired
    public CreditBureauHandler(Set<CreditBureauProvider> services) {
        for (CreditBureauProvider provider : services) {
            servicesMap.put(provider.getKey().getProvideName(), provider);
        }
    }

    public CreditBureauProviderResponse initiateEnquiry(
            CreditBureauEnquiryRequest request,
            BusinessContext businessContext) {
        CreditBureauProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new ServiceInvocationException("Error fetching " + getKey().getServiceName());
        }
        CreditBureauProvider fallbackProvider = null;
        if (runConfig.getFallbackConfig() != null) {
            fallbackProvider = servicesMap.get(runConfig.getFallbackConfig().getProvider());
        }
        
        ServiceRunner<CreditBureauProvider, CreditBureauEnquiryRequest> runner = 
            new ServiceRunner<>(primaryProvider, fallbackProvider, runConfig.getRetries());
        
        try {
            return (CreditBureauProviderResponse) runner.invokeService(
                "initiateEnquiry",
                request,
                runConfig,
                businessContext
            );
        } catch (Exception e) {
            throw new CreditBureauHandlerException("Error initiating credit bureau enquiry: " + e.getMessage(), e);
        }
    }

    public CreditBureauProviderResponse getEnquiryStatus(
            String enquiryId,
            BusinessContext businessContext) {
        CreditBureauProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new ServiceInvocationException("Error fetching " + getKey().getServiceName());
        }
        CreditBureauProvider fallbackProvider = null;
        if (runConfig.getFallbackConfig() != null) {
            fallbackProvider = servicesMap.get(runConfig.getFallbackConfig().getProvider());
        }
        
        ServiceRunner<CreditBureauProvider, String> runner = 
            new ServiceRunner<>(primaryProvider, fallbackProvider, runConfig.getRetries());
        
        try {
            return (CreditBureauProviderResponse) runner.invokeService(
                "getEnquiryStatus",
                enquiryId,
                runConfig,
                businessContext
            );
        } catch (Exception e) {
            throw new CreditBureauHandlerException("Error getting credit bureau enquiry status: " + e.getMessage(), e);
        }
    }

    public CreditBureauProviderResponse pullEnquiry(
            PullEnquiryRequest request,
            BusinessContext businessContext) {
        return pullEnquiry(request, businessContext, null);
    }

    public CreditBureauProviderResponse pullEnquiry(
            PullEnquiryRequest request,
            BusinessContext businessContext,
            String providerName) {
        CreditBureauProvider primaryProvider;
        CreditBureauProvider fallbackProvider = null;
        int retries;
        com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig primaryConfig;
        com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig fallbackConfig = null;

        if (providerName != null && !providerName.trim().isEmpty()) {
            primaryProvider = servicesMap.get(providerName);
            if (primaryProvider == null) {
                throw new ServiceInvocationException("Provider not found: " + providerName);
            }
            primaryConfig = runConfig.getPrimaryConfig();
            retries = runConfig.getRetries();
        } else {
            primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
            if (primaryProvider == null) {
                throw new ServiceInvocationException("Error fetching " + getKey().getServiceName());
            }
            if (runConfig.getFallbackConfig() != null) {
                fallbackProvider = servicesMap.get(runConfig.getFallbackConfig().getProvider());
                fallbackConfig = runConfig.getFallbackConfig();
            }
            primaryConfig = runConfig.getPrimaryConfig();
            retries = runConfig.getRetries();
        }
        
        ServiceRunner<CreditBureauProvider, PullEnquiryRequest> runner = 
            new ServiceRunner<>(primaryProvider, fallbackProvider, retries);
        
        com.nivasafinance.integrations.framework.core.data.RunConfig configForRunner = 
            new com.nivasafinance.integrations.framework.core.data.RunConfig(primaryConfig, fallbackConfig, retries);
        
        try {
            return (CreditBureauProviderResponse) runner.invokeService(
                "pullEnquiry",
                request,
                configForRunner,
                businessContext
            );
        } catch (Exception e) {
            throw new CreditBureauHandlerException("Error pulling credit bureau enquiry: " + e.getMessage(), e);
        }
    }

    @Override
    public ThirdPartyServiceList getKey() {
        return ThirdPartyServiceList.CREDIT_BUREAU;
    }
}

