package com.nivasafinance.services.whatsapp.provider.gallabox;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import org.springframework.stereotype.Component;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateRequest;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.WhatsAppProvider;
import com.nivasafinance.services.whatsapp.provider.gallabox.data.GallaboxConfiguration;

@Component
public class GallaboxWhatsAppProvider implements WhatsAppProvider<GallaboxConfiguration> {
    
    private final GallaboxApiClient apiClient;
    
    public GallaboxWhatsAppProvider() {
        this.apiClient = new GallaboxApiClient();
    }
    
    @Override
    public ThirdPartyProviderList getKey() {
        return ThirdPartyProviderList.GALLABOX;
    }
    
    @Override
    public GallaboxConfiguration setupConfiguration(java.util.Map<String, String> map) {
        String apiEndpoint = map.getOrDefault("api_endpoint", "https://server.gallabox.com/devapi/messages/whatsapp");
        String apiKey = map.get("api_key");
        String apiSecret = map.get("api_secret");
        String channelId = map.get("channel_id");
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Gallabox API key is required");
        }
        if (apiSecret == null || apiSecret.isEmpty()) {
            throw new IllegalArgumentException("Gallabox API secret is required");
        }
        if (channelId == null || channelId.isEmpty()) {
            throw new IllegalArgumentException("Gallabox channel ID is required");
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

        return GallaboxConfiguration.builder()
                .apiEndpoint(apiEndpoint)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .channelId(channelId)
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
        GallaboxConfiguration gallaboxConfig = setupConfiguration(config.getConfigurations());
        
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (request.getTemplateName() == null || request.getTemplateName().isBlank()) {
            throw new IllegalArgumentException("Template name cannot be empty");
        }
        
        WhatsAppTemplateResponse gallaboxResponse = apiClient.sendTemplate(gallaboxConfig, request);
        
        return gallaboxResponse;
    }
    
    @Override
    public WhatsAppTemplateResponse getTemplateStatus(
        String phoneNumber,
        ThirdPartyConfig config,
        BusinessContext businessContext
    ) {
        // Gallabox may not have a status endpoint, return a placeholder response
        return WhatsAppTemplateResponse.builder()
                .phoneNumber(phoneNumber)
                .status("unknown")
                .messageId(null)
                .templateName(null)
                .deliveredAt(null)
                .readAt(null)
                .errorMessage("Template status check not implemented for Gallabox")
                .build();
    }
}
