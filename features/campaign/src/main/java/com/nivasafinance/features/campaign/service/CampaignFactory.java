package com.nivasafinance.features.campaign.service;

import com.nivasafinance.features.campaign.dto.CampaignDraftRequest;
import com.nivasafinance.features.campaign.dto.UpdateCampaignDraftRequest;
import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignType;
import com.nivasafinance.features.campaign.exception.CampaignExceptionFactory;
import com.nivasafinance.features.campaign.service.impl.VoiceCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CampaignFactory {

    private final VoiceCampaignService voiceCampaignService;
    private final MessageSource messageSource;

    public Campaign draftCampaign(CampaignDraftRequest request, CampaignConfig config) {
        // Validate configs exist
        if (config.getConfigs() == null) {
            throw CampaignExceptionFactory.configsRequired(messageSource);
        }

        CampaignType campaignType = config.getConfigs().getCampaignType();
        
        if (campaignType == CampaignType.VOICE) {
            return voiceCampaignService.createDraftCampaign(request, config);
        }
        
        throw CampaignExceptionFactory.unsupportedCampaignType(
                campaignType != null ? campaignType.name() : "null",
                messageSource
        );
    }

    public void updateCampaignDraft(Campaign campaign, UpdateCampaignDraftRequest request, CampaignConfig config) {
        // Validate configs exist
        if (config.getConfigs() == null) {
            throw CampaignExceptionFactory.configsRequired(messageSource);
        }

        CampaignType campaignType = config.getConfigs().getCampaignType();
        
        if (campaignType == CampaignType.VOICE) {
            voiceCampaignService.updateDraftCampaign(campaign, request, config);
            return;
        }
        
        throw CampaignExceptionFactory.unsupportedCampaignType(
                campaignType != null ? campaignType.name() : "null",
                messageSource
        );
    }



    public void refreshCampaign(Campaign campaign, CampaignConfig config) {
        // Validate configs exist
        if (config.getConfigs() == null) {
            throw CampaignExceptionFactory.configsRequired(messageSource);
        }

        CampaignType campaignType = config.getConfigs().getCampaignType();
        
        if (campaignType == CampaignType.VOICE) {
            voiceCampaignService.refreshCampaign(campaign, config);
            return;
        }
        
        throw CampaignExceptionFactory.unsupportedCampaignType(
                campaignType != null ? campaignType.name() : "null",
                messageSource
        );
    }

    public CompletableFuture<Void> submitCampaign(Campaign campaign, CampaignConfig config) {
        // Validate configs exist
        if (config.getConfigs() == null) {
            throw CampaignExceptionFactory.configsRequired(messageSource);
        }

        CampaignType campaignType = config.getConfigs().getCampaignType();
        
        if (campaignType == CampaignType.VOICE) {
            return voiceCampaignService.submitCampaign(campaign, config);
        }
        
        throw CampaignExceptionFactory.unsupportedCampaignType(
                campaignType != null ? campaignType.name() : "null",
                messageSource
        );
    }
}
