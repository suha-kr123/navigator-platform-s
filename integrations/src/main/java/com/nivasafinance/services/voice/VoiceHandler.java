package com.nivasafinance.services.voice;

import com.nivasafinance.integrations.framework.ThirdPartyHandler;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.runner.ServiceRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nivasafinance.services.voice.dto.*;
import com.nivasafinance.services.voice.provider.VoiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class VoiceHandler extends ThirdPartyHandler {

    private final Map<String, VoiceProvider> servicesMap = new HashMap<>();

    @Autowired
    public VoiceHandler(Set<VoiceProvider> services) {
        for (VoiceProvider provider : services) {
            servicesMap.put(provider.getKey().getProvideName(), provider);
        }
    }

    public VoiceCallResponse makeCall(VoiceCallRequest voiceCallRequest, BusinessContext businessContext) {
        VoiceProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new VoiceHandlerException("Error fetching " + getKey().getServiceName());
        }
        VoiceProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;
        
        ServiceRunner<VoiceProvider, VoiceCallRequest> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());
        
        try {
            return (VoiceCallResponse) runner.invokeService("makeCall", voiceCallRequest, runConfig, businessContext);
        } catch (Exception e) {
            throw new VoiceHandlerException("Error making call: " + e.getMessage());
        }
    }

    public VoiceCallResponse getCallStatus(String callSid, BusinessContext businessContext) {
        VoiceProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new VoiceHandlerException("Error fetching " + getKey().getServiceName());
        }
        VoiceProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;
        
        ServiceRunner<VoiceProvider, String> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());
        
        try {
            return (VoiceCallResponse) runner.invokeService("getCallStatus", callSid, runConfig, businessContext);
        } catch (Exception e) {
            throw new VoiceHandlerException("Error getting call status: " + e.getMessage());
        }
    }

    public VoiceCreateListResponse uploadCSVList(VoiceCreateListRequest request, BusinessContext businessContext) {
        VoiceProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new VoiceHandlerException("Error fetching " + getKey().getServiceName());
        }
        VoiceProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;
        
        ServiceRunner<VoiceProvider, VoiceCreateListRequest> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());
        
        try {
            return (VoiceCreateListResponse) runner.invokeService("uploadCSVList", request, runConfig, businessContext);
        } catch (Exception e) {
            throw new VoiceHandlerException("Error uploading CSV list: " + e.getMessage());
        }
    }

    public VoiceCSVUploadStatusResponse getCSVUploadStatus(VoiceCSVUploadStatusRequest request, BusinessContext businessContext) {
        VoiceProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new VoiceHandlerException("Error fetching " + getKey().getServiceName());
        }
        VoiceProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;
        
        ServiceRunner<VoiceProvider, VoiceCSVUploadStatusRequest> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());
        
        try {
            return (VoiceCSVUploadStatusResponse) runner.invokeService("getCSVUploadStatus", request, runConfig, businessContext);
        } catch (Exception e) {
            throw new VoiceHandlerException("Error getting CSV upload status: " + e.getMessage());
        }
    }

    public VoiceCampaignResponse createCampaign(VoiceCampaignRequest request, BusinessContext businessContext) {
        VoiceProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new VoiceHandlerException("Error fetching " + getKey().getServiceName());
        }
        VoiceProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;
        
        ServiceRunner<VoiceProvider, VoiceCampaignRequest> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());
        
        try {
            return (VoiceCampaignResponse) runner.invokeService("createCampaign", request, runConfig, businessContext);
        } catch (Exception e) {
            throw new VoiceHandlerException("Error creating campaign: " + e.getMessage());
        }
    }

    public VoiceCampaignResponse getCampaignDetails(VoiceGetCampaignDetailsRequest request, BusinessContext businessContext) {
        VoiceProvider primaryProvider = servicesMap.get(runConfig.getPrimaryConfig().getProvider());
        if (primaryProvider == null) {
            throw new VoiceHandlerException("Error fetching " + getKey().getServiceName());
        }
        VoiceProvider fallbackProvider = runConfig.getFallbackConfig() != null
                ? servicesMap.get(runConfig.getFallbackConfig().getProvider())
                : null;
        
        ServiceRunner<VoiceProvider, VoiceGetCampaignDetailsRequest> runner = new ServiceRunner<>(
                primaryProvider, fallbackProvider, runConfig.getRetries());
        
        try {
            return (VoiceCampaignResponse) runner.invokeService("getCampaignDetails", request, runConfig, businessContext);
        } catch (Exception e) {
            throw new VoiceHandlerException("Error getting campaign details: " + e.getMessage());
        }
    }

    @Override
    public ThirdPartyServiceList getKey() {
        return ThirdPartyServiceList.VOICE;
    }
}

