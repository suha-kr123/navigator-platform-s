package com.nivasafinance.features.campaign.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.campaign.dto.CampaignConfigDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignConfigResponse;
import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import com.nivasafinance.features.campaign.repository.CampaignConfigRepositoryWrapper;
import com.nivasafinance.features.campaign.service.CampaignConfigReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignConfigReadServiceImpl implements CampaignConfigReadService {

    private final CampaignConfigRepositoryWrapper campaignConfigRepositoryWrapper;

    @Override
    public PaginatedResponse<CampaignConfigResponse> getAllCampaignConfigs(
            @Nullable List<CampaignConfigStatus> statuses,
            PaginationRequest paginationRequest) {
        
        // Default to ACTIVE if statuses is null or empty
        List<CampaignConfigStatus> effectiveStatuses = CollectionUtils.isEmpty(statuses)
                ? List.of(CampaignConfigStatus.ACTIVE, CampaignConfigStatus.INACTIVE)
                : statuses;

        PaginatedResponse<CampaignConfig> paginatedEntities = 
                campaignConfigRepositoryWrapper.findByStatusInWithException(effectiveStatuses, paginationRequest);

        List<CampaignConfigResponse> responses = paginatedEntities.getContent().stream()
                .map(CampaignConfigResponse::from)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responses, paginatedEntities.getPagination());
    }

    @Override
    public CampaignConfigDetailedResponse getCampaignConfigByIdentifier(UUID identifier) {
        CampaignConfig entity = campaignConfigRepositoryWrapper.findByIdentifierWithException(identifier);
        return CampaignConfigDetailedResponse.from(entity);
    }

    @Override
    public CampaignConfigDetailedResponse getCampaignConfigById(Long id) {
        CampaignConfig entity = campaignConfigRepositoryWrapper.findByIdWithException(id);
        return CampaignConfigDetailedResponse.from(entity);
    }
}
