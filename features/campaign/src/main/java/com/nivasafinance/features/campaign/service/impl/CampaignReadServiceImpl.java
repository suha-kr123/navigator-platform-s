package com.nivasafinance.features.campaign.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.campaign.dto.CampaignConfigDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignResponse;
import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.repository.CampaignRepositoryWrapper;
import com.nivasafinance.features.campaign.service.CampaignConfigReadService;
import com.nivasafinance.features.campaign.service.CampaignReadService;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignReadServiceImpl implements CampaignReadService {

    private final CampaignRepositoryWrapper campaignRepositoryWrapper;
    private final CampaignConfigReadService campaignConfigReadService;
    private final DocumentReadService documentReadService;

    @Override
    public PaginatedResponse<CampaignResponse> getAllCampaigns(PaginationRequest paginationRequest) {
        PaginatedResponse<Campaign> paginatedEntities = 
                campaignRepositoryWrapper.findAllWithException(paginationRequest);

        List<CampaignResponse> responses = paginatedEntities.getContent().stream()
                .map(CampaignResponse::from)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responses, paginatedEntities.getPagination());
    }

    @Override
    public CampaignDetailedResponse getCampaignByIdentifier(UUID identifier) {
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(identifier);

        // Fetch config using service
        CampaignConfigDetailedResponse configResponse =
                campaignConfigReadService.getCampaignConfigById(campaign.getConfigId());

        // Fetch document if exists
        DocumentResponse document = null;
        if (campaign.getDocumentDetails() != null && campaign.getDocumentDetails().getDocumentId() != null) {
            document = documentReadService.getDocumentById(campaign.getDocumentDetails().getDocumentId());
        }

        return CampaignDetailedResponse.from(campaign, configResponse, document);
    }
}
