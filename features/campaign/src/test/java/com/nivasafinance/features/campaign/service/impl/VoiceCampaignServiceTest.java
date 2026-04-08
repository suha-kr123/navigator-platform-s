package com.nivasafinance.features.campaign.service.impl;

import com.nivasafinance.features.campaign.dto.CampaignDraftRequest;
import com.nivasafinance.features.campaign.dto.UpdateCampaignDraftRequest;
import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignStatus;
import com.nivasafinance.features.campaign.repository.CampaignRepositoryWrapper;
import com.nivasafinance.features.document.service.DocumentReadService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.voice.VoiceHandler;
import com.nivasafinance.services.voice.dto.VoiceCampaignResponse;
import com.nivasafinance.services.voice.dto.VoiceCampaignStatus;
import com.nivasafinance.services.voice.dto.VoiceGetCampaignDetailsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoiceCampaignServiceTest {

    @Mock
    private MessageSource messageSource;
    @Mock
    private ServiceFactory<VoiceHandler> serviceFactory;
    @Mock
    private DocumentReadService documentReadService;
    @Mock
    private CampaignRepositoryWrapper campaignRepositoryWrapper;

    @InjectMocks
    private VoiceCampaignService service;

    @Test
    void createDraftCampaign_mergesRequestAndConfigDefaults() {
        CampaignDraftRequest request = CampaignDraftRequest.builder()
                .name("camp")
                .voiceDetails(CampaignDraftRequest.VoiceDetails.builder()
                        .cpm(50)
                        .scheduledAt(LocalDateTime.now())
                        .build())
                .build();
        CampaignConfig config = new CampaignConfig();
        CampaignConfig.VoiceConfigs voiceConfigs = CampaignConfig.VoiceConfigs.builder()
                .callerId("caller")
                .appFlowId("flow")
                .defaultNoOfRetries(3)
                .defaultRetryInterval(5)
                .defaultCpm(10)
                .build();
        config.setConfigs(CampaignConfig.Configs.builder().voiceConfigs(voiceConfigs).build());
        config.setId(1L);

        Campaign result = service.createDraftCampaign(request, config);

        assertEquals(CampaignStatus.DRAFT, result.getStatus());
        assertEquals(1L, result.getConfigId());
        Campaign.VoiceDetails voiceDetails = result.getProviderDetails().getVoiceDetails();
        assertEquals(50, voiceDetails.getCpm());
        assertEquals("caller", voiceDetails.getCallerId());
        assertEquals("flow", voiceDetails.getAppFlowId());
    }

    @Test
    void updateDraftCampaign_mergesVoiceDetails() {
        Campaign campaign = Campaign.builder()
                .providerDetails(Campaign.ProviderDetails.builder()
                        .voiceDetails(Campaign.VoiceDetails.builder()
                                .callerId("caller")
                                .appFlowId("flow")
                                .noOfRetries(2)
                                .retryInterval(3)
                                .cpm(20)
                                .build())
                        .build())
                .build();

        UpdateCampaignDraftRequest request = UpdateCampaignDraftRequest.builder()
                .name("updated")
                .voiceDetails(UpdateCampaignDraftRequest.VoiceDetails.builder()
                        .scheduledAt(LocalDateTime.now())
                        .build())
                .build();

        service.updateDraftCampaign(campaign, request, new CampaignConfig());

        assertEquals("updated", campaign.getName());
        assertNotNull(campaign.getProviderDetails().getVoiceDetails().getScheduledAt());
        assertEquals(20, campaign.getProviderDetails().getVoiceDetails().getCpm());
    }

    @Test
    void refreshCampaign_updatesFromProviderResponse() {
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setProviderId("pid");
        campaign.setStatus(CampaignStatus.SUBMITTED);
        CampaignConfig config = new CampaignConfig();
        VoiceHandler handler = mock(VoiceHandler.class);
        VoiceCampaignResponse response = VoiceCampaignResponse.builder()
                .providerKey("prov")
                .campaignId("cid")
                .status(VoiceCampaignStatus.COMPLETED)
                .summary(VoiceCampaignResponse.Summary.builder().completed(5L).build())
                .build();

        when(serviceFactory.getHandler(ThirdPartyServiceList.VOICE)).thenReturn(handler);
        when(handler.getCampaignDetails(any(VoiceGetCampaignDetailsRequest.class), any())).thenReturn(response);

        service.refreshCampaign(campaign, config);

        assertEquals("prov", campaign.getProvider());
        assertEquals(CampaignStatus.COMPLETED, campaign.getStatus());
        assertEquals(5L, campaign.getSummary().getCompleted());
    }

    @Test
    void submitCampaign_missingDocument_throws() {
        Campaign campaign = new Campaign();
        CampaignConfig config = new CampaignConfig();

        assertThrows(RuntimeException.class, () -> service.submitCampaign(campaign, config).join());
    }
}
