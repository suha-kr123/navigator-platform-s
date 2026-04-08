package com.nivasafinance.features.campaign.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.campaign.dto.CampaignConfigDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignConfigResponse;
import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import com.nivasafinance.features.campaign.repository.CampaignConfigRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignConfigReadServiceImplTest {

    @Mock
    private CampaignConfigRepositoryWrapper campaignConfigRepositoryWrapper;

    @InjectMocks
    private CampaignConfigReadServiceImpl service;

    @Test
    void getAllCampaignConfigs_defaultsStatusesWhenNull() {
        PaginationRequest request = new PaginationRequest();
        PaginatedResponse<CampaignConfig> repoResponse = new PaginatedResponse<>(
                List.of(sampleConfig(CampaignConfigStatus.ACTIVE)),
                new PaginationInfo());
        when(campaignConfigRepositoryWrapper.findByStatusInWithException(anyList(), eq(request)))
                .thenReturn(repoResponse);

        PaginatedResponse<CampaignConfigResponse> response = service.getAllCampaignConfigs(null, request);

        assertEquals(1, response.getContent().size());
        ArgumentCaptor<List<CampaignConfigStatus>> captor = ArgumentCaptor.forClass(List.class);
        verify(campaignConfigRepositoryWrapper).findByStatusInWithException(captor.capture(), eq(request));
        assertTrue(captor.getValue().containsAll(List.of(CampaignConfigStatus.ACTIVE, CampaignConfigStatus.INACTIVE)));
    }

    @Test
    void getAllCampaignConfigs_usesProvidedStatuses() {
        PaginationRequest request = new PaginationRequest();
        List<CampaignConfigStatus> statuses = List.of(CampaignConfigStatus.ACTIVE);
        PaginatedResponse<CampaignConfig> repoResponse = new PaginatedResponse<>(
                List.of(sampleConfig(CampaignConfigStatus.ACTIVE)),
                new PaginationInfo());
        when(campaignConfigRepositoryWrapper.findByStatusInWithException(statuses, request))
                .thenReturn(repoResponse);

        PaginatedResponse<CampaignConfigResponse> response = service.getAllCampaignConfigs(statuses, request);

        assertEquals(1, response.getContent().size());
        verify(campaignConfigRepositoryWrapper).findByStatusInWithException(statuses, request);
    }

    @Test
    void getCampaignConfigByIdentifier_returnsResponse() {
        CampaignConfig config = sampleConfig(CampaignConfigStatus.ACTIVE);
        when(campaignConfigRepositoryWrapper.findByIdentifierWithException(config.getIdentifier()))
                .thenReturn(config);

        CampaignConfigDetailedResponse response = service.getCampaignConfigByIdentifier(config.getIdentifier());

        assertEquals(config.getIdentifier().toString(), response.getIdentifier());
    }

    @Test
    void getCampaignConfigById_returnsResponse() {
        CampaignConfig config = sampleConfig(CampaignConfigStatus.INACTIVE);
        when(campaignConfigRepositoryWrapper.findByIdWithException(5L)).thenReturn(config);

        CampaignConfigDetailedResponse response = service.getCampaignConfigById(5L);

        assertEquals(config.getIdentifier().toString(), response.getIdentifier());
    }

    private CampaignConfig sampleConfig(CampaignConfigStatus status) {
        CampaignConfig config = new CampaignConfig();
        config.setId(1L);
        config.setIdentifier(UUID.randomUUID());
        config.setStatus(status);
        CampaignConfig.Configs configs = CampaignConfig.Configs.builder()
                .fileType(com.nivasafinance.redash.dto.FileType.CSV)
                .build();
        config.setConfigs(configs);
        return config;
    }
}
