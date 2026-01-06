package com.nivasafinance.features.campaign.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.campaign.dto.CampaignDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignResponse;

import java.util.UUID;

public interface CampaignReadService {
    PaginatedResponse<CampaignResponse> getAllCampaigns(PaginationRequest paginationRequest);
    
    CampaignDetailedResponse getCampaignByIdentifier(UUID identifier);
    
    CampaignDetailedResponse getCampaignByProviderId(String providerId);
}
