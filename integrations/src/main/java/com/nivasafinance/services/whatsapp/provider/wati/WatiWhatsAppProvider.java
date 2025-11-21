package com.nivasafinance.services.whatsapp.provider.wati;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import org.springframework.stereotype.Component;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.WhatsAppProvider;
import com.nivasafinance.services.whatsapp.provider.wati.data.WatiConfiguration;

@Component
public class WatiWhatsAppProvider implements WhatsAppProvider {
    
    private final WatiApiClient apiClient;
    
    public WatiWhatsAppProvider() {
        this.apiClient = new WatiApiClient();
    }
    
    @Override
    public ThirdPartyProviderList getKey() {
        return ThirdPartyProviderList.WATI;
    }
    
    @Override
    public WatiConfiguration setupConfiguration(java.util.Map<String, String> map) {
        String apiEndpoint = map.getOrDefault("api_endpoint", "https://live-server.wati.io");
        String accessToken = map.get("access_token");
        
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("WATI access token is required");
        }
        
        int timeout = 30;
        if (map.containsKey("timeout")) {
            try {
                timeout = Integer.parseInt(map.get("timeout"));
            } catch (NumberFormatException e) {
                timeout = 30;
            }
        }
        
        int retryAttempts = 3;
        if (map.containsKey("retry_attempts")) {
            try {
                retryAttempts = Integer.parseInt(map.get("retry_attempts"));
            } catch (NumberFormatException e) {
                retryAttempts = 3;
            }
        }
        
        String clientId = null;
        if (map.containsKey("client_id")) {
            clientId = map.get("client_id");
        } else if (map.containsKey("clientId")) {
            clientId = map.get("clientId");
        }

        return WatiConfiguration.builder()
                .apiEndpoint(apiEndpoint)
                .accessToken(accessToken)
                .clientId(clientId)
                .timeout(timeout)
                .retryAttempts(retryAttempts)
                .build();
    }
    
    @Override
    public WhatsAppTemplateResponse sendTemplate(
        WhatsAppTemplateRequest request,
        ThirdPartyConfig config,
        BusinessContext businessContext
    ) {
        WatiConfiguration watiConfig = setupConfiguration(config.getConfigurations());
        
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (request.getTemplateName() == null || request.getTemplateName().isBlank()) {
            throw new IllegalArgumentException("Template name cannot be empty");
        }
        
        WhatsAppTemplateResponse watiResponse = apiClient.sendTemplate(watiConfig, request);
        
        return watiResponse;
    }
    
    @Override
    public WhatsAppTemplateResponse getTemplateStatus(
        String phoneNumber,
        ThirdPartyConfig config,
        BusinessContext businessContext
    ) {
        WatiConfiguration watiConfig = setupConfiguration(config.getConfigurations());
        
        WhatsAppTemplateResponse watiResponse = apiClient.getTemplateStatus(watiConfig, phoneNumber);
        
        return watiResponse;
    }
}

