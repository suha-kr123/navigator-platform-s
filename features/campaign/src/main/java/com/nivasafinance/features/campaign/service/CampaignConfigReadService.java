package com.nivasafinance.features.campaign.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.campaign.dto.CampaignConfigDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignConfigResponse;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.UUID;

public interface CampaignConfigReadService {
    PaginatedResponse<CampaignConfigResponse> getAllCampaignConfigs(
            @Nullable List<CampaignConfigStatus> statuses,
            PaginationRequest paginationRequest);
    
    CampaignConfigDetailedResponse getCampaignConfigByIdentifier(UUID identifier);
    
    CampaignConfigDetailedResponse getCampaignConfigById(Long id);
}
