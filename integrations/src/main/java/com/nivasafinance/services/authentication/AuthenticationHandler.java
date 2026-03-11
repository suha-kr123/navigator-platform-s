package com.nivasafinance.services.authentication;

import com.nivasafinance.integrations.framework.ThirdPartyHandler;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.runner.ServiceRunner;
import com.nivasafinance.services.authentication.dto.AuthCreateUserRequest;
import com.nivasafinance.services.authentication.dto.AuthSendOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpRequest;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpResponse;
import com.nivasafinance.services.authentication.provider.AuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AuthenticationHandler extends ThirdPartyHandler {

    private final Map<String, AuthenticationProvider> servicesMap = new HashMap<>();

    @Autowired
    public AuthenticationHandler(Set<AuthenticationProvider> services) {
        for (AuthenticationProvider provider : services) {
            servicesMap.put(provider.getKey().getProvideName(), provider);
        }
    }

    public void sendOtp(AuthSendOtpRequest request, BusinessContext businessContext) {
        AuthenticationProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new AuthenticationHandlerException("Error fetching " + getKey().getServiceName());
        }
        AuthenticationProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;

        ServiceRunner<AuthenticationProvider, AuthSendOtpRequest> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());

        runner.invokeService("sendOtp", request, runConfig, businessContext);
    }

    public AuthVerifyOtpResponse verifyOtp(AuthVerifyOtpRequest request, BusinessContext businessContext) {
        AuthenticationProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new AuthenticationHandlerException("Error fetching " + getKey().getServiceName());
        }
        AuthenticationProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;

        ServiceRunner<AuthenticationProvider, AuthVerifyOtpRequest> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());

        return (AuthVerifyOtpResponse) runner.invokeService("verifyOtp", request, runConfig, businessContext);
    }

    public void createUser(AuthCreateUserRequest request, BusinessContext businessContext) {
        AuthenticationProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new AuthenticationHandlerException("Error fetching " + getKey().getServiceName());
        }
        AuthenticationProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;
        ServiceRunner<AuthenticationProvider, AuthCreateUserRequest> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());
        runner.invokeService("createUser", request, runConfig, businessContext);
    }

    @Override
    public ThirdPartyServiceList getKey() {
        return ThirdPartyServiceList.AUTHENTICATION;
    }
}
