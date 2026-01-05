package com.nivasafinance.features.campaign.service;

import com.nivasafinance.features.campaign.dto.CampaignDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignDraftRequest;
import com.nivasafinance.features.campaign.dto.CampaignDraftResponse;
import com.nivasafinance.features.campaign.dto.CampaignGenerateDocumentRequest;
import com.nivasafinance.features.campaign.dto.UpdateCampaignDraftRequest;

import java.util.UUID;

public interface CampaignWriteService {
    CampaignDraftResponse createCampaignDraft(CampaignDraftRequest request);
    
    void updateCampaignDraft(UUID identifier, UpdateCampaignDraftRequest request);
    
    void cancelCampaign(UUID identifier);
    
    void submitCampaign(UUID identifier);
    
    CampaignDetailedResponse refreshCampaign(UUID identifier);
    
    void generateDocument(UUID campaignIdentifier, CampaignGenerateDocumentRequest request);
}
