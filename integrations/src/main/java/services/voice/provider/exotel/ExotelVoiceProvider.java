package services.voice.provider.exotel;

import framework.config.BusinessContext;
import framework.config.ThirdPartyProviderList;
import framework.core.data.ThirdPartyConfig;
import org.springframework.stereotype.Component;
import services.voice.dto.VoiceCallRequest;
import services.voice.dto.VoiceCallResponse;
import services.voice.provider.VoiceProvider;
import services.voice.provider.exotel.data.ExotelConfiguration;

import java.util.Map;

@Component
public class ExotelVoiceProvider implements VoiceProvider {

    private final ExotelApiClient apiClient = new ExotelApiClient();

    @Override
    public ThirdPartyProviderList getKey() {
        return ThirdPartyProviderList.EXOTEL;
    }

    @Override
    public ExotelConfiguration setupConfiguration(Map<String, String> map) {
        String subdomain = map.get("subdomain");
        if (subdomain == null || subdomain.isEmpty()) {
            subdomain = "api.exotel.com";
        }
        
        String recordingEnabledStr = map.get("recordingEnabled");
        boolean recordingEnabled = true;
        if (recordingEnabledStr != null) {
            try {
                recordingEnabled = Boolean.parseBoolean(recordingEnabledStr);
            } catch (Exception ignored) {
                // Default to true
            }
        }
        
        int maxCallDuration = 3600;
        String maxCallDurationStr = map.get("maxCallDuration");
        if (maxCallDurationStr != null) {
            try {
                maxCallDuration = Integer.parseInt(maxCallDurationStr);
            } catch (NumberFormatException ignored) {
                // Default to 3600
            }
        }
        
        int retryAttempts = 3;
        String retryAttemptsStr = map.get("retryAttempts");
        if (retryAttemptsStr != null) {
            try {
                retryAttempts = Integer.parseInt(retryAttemptsStr);
            } catch (NumberFormatException ignored) {
                // Default to 3
            }
        }
        
        int timeout = 30;
        String timeoutStr = map.get("timeout");
        if (timeoutStr != null) {
            try {
                timeout = Integer.parseInt(timeoutStr);
            } catch (NumberFormatException ignored) {
                // Default to 30
            }
        }
        
        return new ExotelConfiguration(
                map.getOrDefault("accountSid", ""),
                map.getOrDefault("authToken", ""),
                subdomain,
                map.getOrDefault("callerId", ""),
                map.getOrDefault("webhookUrl", ""),
                map.getOrDefault("apiKey", ""),
                map.getOrDefault("apiToken", ""),
                recordingEnabled,
                maxCallDuration,
                retryAttempts,
                timeout
        );
    }

    @Override
    public VoiceCallResponse makeCall(
            VoiceCallRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        ExotelConfiguration exotelConfig = setupConfiguration(config.getConfigurations());

        // Validate phone numbers
        if (request.getFromNumber() == null || request.getFromNumber().isBlank()) {
            throw new IllegalArgumentException("From number cannot be empty");
        }
        if (request.getToNumber() == null || request.getToNumber().isBlank()) {
            throw new IllegalArgumentException("To number cannot be empty");
        }

        return apiClient.makeCall(exotelConfig, request);
    }

    @Override
    public VoiceCallResponse getCallStatus(
            String callSid,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        ExotelConfiguration exotelConfig = setupConfiguration(config.getConfigurations());
        return apiClient.getCallStatus(exotelConfig, callSid);
    }
}

