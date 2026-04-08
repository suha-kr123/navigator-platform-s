package com.nivasafinance.features.campaign.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.campaign.dto.CampaignConfigDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignResponse;
import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.enums.CampaignDocumentStatus;
import com.nivasafinance.features.campaign.enums.CampaignStatus;
import com.nivasafinance.features.campaign.repository.CampaignRepositoryWrapper;
import com.nivasafinance.features.campaign.service.CampaignConfigReadService;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignReadServiceImplTest {

    @Mock
    private CampaignRepositoryWrapper campaignRepositoryWrapper;
    @Mock
    private CampaignConfigReadService campaignConfigReadService;
    @Mock
    private DocumentReadService documentReadService;

    @InjectMocks
    private CampaignReadServiceImpl service;

    @Test
    void getAllCampaigns_mapsResponses() {
        PaginationRequest request = new PaginationRequest();
        Campaign c1 = sampleCampaign(1L, CampaignStatus.DRAFT);
        Campaign c2 = sampleCampaign(2L, CampaignStatus.SUBMITTED);
        PaginatedResponse<Campaign> repoResponse = new PaginatedResponse<>(
                List.of(c1, c2),
                new PaginationInfo());
        when(campaignRepositoryWrapper.findAllWithException(request)).thenReturn(repoResponse);

        PaginatedResponse<CampaignResponse> response = service.getAllCampaigns(request);

        assertEquals(2, response.getContent().size());
        verify(campaignRepositoryWrapper).findAllWithException(request);
    }

    @Test
    void getCampaignByIdentifier_fetchesConfigAndDocument() {
        Campaign campaign = sampleCampaign(10L, CampaignStatus.DRAFT);
        campaign.setDocumentDetails(Campaign.DocumentDetails.builder()
                .documentId(99L)
                .documentStatus(CampaignDocumentStatus.GENERATED)
                .build());
        when(campaignRepositoryWrapper.findByIdentifierWithException(campaign.getIdentifier()))
                .thenReturn(campaign);
        CampaignConfigDetailedResponse configResponse = CampaignConfigDetailedResponse.builder()
                .identifier("config")
                .build();
        when(campaignConfigReadService.getCampaignConfigById(campaign.getConfigId())).thenReturn(configResponse);
        DocumentResponse documentResponse = DocumentResponse.builder().id(99L).build();
        when(documentReadService.getDocumentById(99L)).thenReturn(documentResponse);

        CampaignDetailedResponse response = service.getCampaignByIdentifier(campaign.getIdentifier());

        assertEquals(campaign.getIdentifier().toString(), response.getIdentifier());
        assertEquals(99L, response.getDocument().getId());
    }

    @Test
    void getCampaignByIdentifier_withoutDocument_skipsFetch() {
        Campaign campaign = sampleCampaign(11L, CampaignStatus.SUBMITTED);
        campaign.setDocumentDetails(null);
        when(campaignRepositoryWrapper.findByIdentifierWithException(campaign.getIdentifier()))
                .thenReturn(campaign);
        when(campaignConfigReadService.getCampaignConfigById(campaign.getConfigId()))
                .thenReturn(CampaignConfigDetailedResponse.builder().build());

        CampaignDetailedResponse response = service.getCampaignByIdentifier(campaign.getIdentifier());

        assertNull(response.getDocument());
        verify(documentReadService, never()).getDocumentById(anyLong());
    }

    @Test
    void getCampaignByProviderId_fetchesDependencies() {
        Campaign campaign = sampleCampaign(12L, CampaignStatus.DRAFT);
        when(campaignRepositoryWrapper.findByProviderIdWithException("provider-1"))
                .thenReturn(campaign);
        when(campaignConfigReadService.getCampaignConfigById(campaign.getConfigId()))
                .thenReturn(CampaignConfigDetailedResponse.builder().build());

        CampaignDetailedResponse response = service.getCampaignByProviderId("provider-1");

        assertEquals(campaign.getIdentifier().toString(), response.getIdentifier());
        verify(campaignConfigReadService).getCampaignConfigById(campaign.getConfigId());
    }

    private Campaign sampleCampaign(Long id, CampaignStatus status) {
        Campaign campaign = new Campaign();
        campaign.setId(id);
        campaign.setIdentifier(UUID.randomUUID());
        campaign.setConfigId(5L);
        campaign.setStatus(status);
        campaign.setName("campaign-" + id);
        Campaign.DocumentDetails documentDetails = Campaign.DocumentDetails.builder()
                .documentStatus(CampaignDocumentStatus.GENERATION_IN_PROGRESS)
                .build();
        campaign.setDocumentDetails(documentDetails);
        return campaign;
    }
}
