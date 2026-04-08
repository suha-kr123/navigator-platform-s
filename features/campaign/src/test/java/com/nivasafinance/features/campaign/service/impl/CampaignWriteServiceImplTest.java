package com.nivasafinance.features.campaign.service.impl;

import com.nivasafinance.features.campaign.dto.CampaignDraftRequest;
import com.nivasafinance.features.campaign.dto.CampaignDraftResponse;
import com.nivasafinance.features.campaign.dto.CampaignGenerateDocumentRequest;
import com.nivasafinance.features.campaign.dto.UpdateCampaignDraftRequest;
import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import com.nivasafinance.features.campaign.enums.CampaignDocumentStatus;
import com.nivasafinance.features.campaign.enums.CampaignStatus;
import com.nivasafinance.features.campaign.repository.CampaignConfigRepositoryWrapper;
import com.nivasafinance.features.campaign.repository.CampaignRepositoryWrapper;
import com.nivasafinance.features.campaign.service.CampaignFactory;
import com.nivasafinance.features.campaign.service.CampaignReadService;
import com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.redash.dto.FileType;
import com.nivasafinance.redash.dto.RedashReportRequest;
import com.nivasafinance.redash.service.RedashService;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignWriteServiceImplTest {

    @Mock
    private CampaignConfigRepositoryWrapper campaignConfigRepositoryWrapper;
    @Mock
    private CampaignRepositoryWrapper campaignRepositoryWrapper;
    @Mock
    private CampaignFactory campaignFactory;
    @Mock
    private CampaignReadService campaignReadService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private RedashService redashService;
    @Mock
    private DocumentWriteService documentWriteService;

    @InjectMocks
    private CampaignWriteServiceImpl service;

    @Test
    void createCampaignDraft_configNotActive_throws() {
        CampaignConfig config = configWithStatus(CampaignConfigStatus.INACTIVE);
        CampaignDraftRequest request = CampaignDraftRequest.builder()
                .configIdentifier(config.getIdentifier())
                .name("c1")
                .build();
        when(campaignConfigRepositoryWrapper.findByIdentifierWithException(config.getIdentifier())).thenReturn(config);

        assertThrows(RuntimeException.class, () -> service.createCampaignDraft(request));
    }

    @Test
    void createCampaignDraft_duplicateName_throws() {
        CampaignConfig config = configWithStatus(CampaignConfigStatus.ACTIVE);
        CampaignDraftRequest request = CampaignDraftRequest.builder()
                .configIdentifier(config.getIdentifier())
                .name("duplicate")
                .build();
        when(campaignConfigRepositoryWrapper.findByIdentifierWithException(config.getIdentifier())).thenReturn(config);
        when(campaignRepositoryWrapper.existsByName("duplicate")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.createCampaignDraft(request));
    }

    @Test
    void createCampaignDraft_success_returnsIdentifier() {
        CampaignConfig config = configWithStatus(CampaignConfigStatus.ACTIVE);
        CampaignDraftRequest request = CampaignDraftRequest.builder()
                .configIdentifier(config.getIdentifier())
                .name("new-campaign")
                .build();
        Campaign draft = new Campaign();
        draft.setIdentifier(UUID.randomUUID());
        when(campaignConfigRepositoryWrapper.findByIdentifierWithException(config.getIdentifier())).thenReturn(config);
        when(campaignRepositoryWrapper.existsByName("new-campaign")).thenReturn(false);
        when(campaignFactory.draftCampaign(request, config)).thenReturn(draft);
        when(campaignRepositoryWrapper.saveWithException(draft)).thenReturn(draft);

        CampaignDraftResponse response = service.createCampaignDraft(request);

        assertEquals(draft.getIdentifier().toString(), response.getIdentifier());
    }

    @Test
    void updateCampaignDraft_notDraft_throws() {
        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.SUBMITTED);
        when(campaignRepositoryWrapper.findByIdentifierWithException(any())).thenReturn(campaign);

        assertThrows(RuntimeException.class, () -> service.updateCampaignDraft(UUID.randomUUID(),
                UpdateCampaignDraftRequest.builder().name("name").build()));
    }

    @Test
    void cancelCampaign_notDraft_throws() {
        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.SUBMITTED);
        when(campaignRepositoryWrapper.findByIdentifierWithException(any())).thenReturn(campaign);

        assertThrows(RuntimeException.class, () -> service.cancelCampaign(UUID.randomUUID()));
    }

    @Test
    void submitCampaign_missingDocument_throws() {
        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.DRAFT);
        when(campaignRepositoryWrapper.findByIdentifierWithException(any())).thenReturn(campaign);

        assertThrows(RuntimeException.class, () -> service.submitCampaign(UUID.randomUUID()));
    }

    @Test
    void submitCampaign_setsSubmissionInProgress_andCallsFactory() {
        UUID identifier = UUID.randomUUID();
        Campaign campaign = new Campaign();
        campaign.setIdentifier(identifier);
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign.setConfigId(1L);
        campaign.setDocumentDetails(Campaign.DocumentDetails.builder()
                .documentId(5L)
                .documentStatus(CampaignDocumentStatus.GENERATED)
                .build());

        when(campaignRepositoryWrapper.findByIdentifierWithException(identifier))
                .thenReturn(campaign)
                .thenReturn(campaign);
        when(campaignConfigRepositoryWrapper.findByIdWithException(1L)).thenReturn(new CampaignConfig());
        when(campaignFactory.submitCampaign(any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        service.submitCampaign(identifier);

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepositoryWrapper, atLeastOnce()).saveWithException(captor.capture());
        Campaign firstSave = captor.getAllValues().get(0);
        assertEquals(CampaignStatus.SUBMITTED, firstSave.getStatus());
        verify(campaignFactory).submitCampaign(any(), any());
    }

    @Test
    void refreshCampaign_terminalStatus_throws() {
        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.COMPLETED);
        when(campaignRepositoryWrapper.findByIdentifierWithException(any())).thenReturn(campaign);

        assertThrows(RuntimeException.class, () -> service.refreshCampaign(UUID.randomUUID()));
    }

    @Test
    void refreshCampaign_canRefresh_callsFactoryAndSaves() {
        UUID identifier = UUID.randomUUID();
        Campaign campaign = new Campaign();
        campaign.setIdentifier(identifier);
        campaign.setStatus(CampaignStatus.SUBMITTED);
        campaign.setConfigId(2L);
        when(campaignRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(campaign);
        when(campaignConfigRepositoryWrapper.findByIdWithException(2L)).thenReturn(new CampaignConfig());
        when(campaignReadService.getCampaignByIdentifier(identifier)).thenReturn(
                com.nivasafinance.features.campaign.dto.CampaignDetailedResponse.builder().identifier(identifier.toString()).build());

        service.refreshCampaign(identifier);

        verify(campaignFactory).refreshCampaign(any(), any());
        verify(campaignRepositoryWrapper).saveWithException(campaign);
    }

    @Test
    void generateDocument_nonDraft_throws() {
        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.SUBMITTED);
        when(campaignRepositoryWrapper.findByIdentifierWithException(any())).thenReturn(campaign);

        assertThrows(RuntimeException.class, () -> service.generateDocument(UUID.randomUUID(), new CampaignGenerateDocumentRequest()));
    }

    @Test
    void generateDocument_failure_setsFailedStatus() {
        UUID identifier = UUID.randomUUID();
        Campaign campaign = new Campaign();
        campaign.setId(10L);
        campaign.setIdentifier(identifier);
        campaign.setStatus(CampaignStatus.DRAFT);
        CampaignConfig config = new CampaignConfig();
        CampaignConfig.Configs configs = CampaignConfig.Configs.builder()
                .dataProviderId(1L)
                .fileType(FileType.CSV)
                .build();
        config.setConfigs(configs);
        campaign.setDocumentDetails(Campaign.DocumentDetails.builder().build());

        when(campaignRepositoryWrapper.findByIdentifierWithException(identifier))
                .thenReturn(campaign)
                .thenReturn(campaign);
        when(campaignConfigRepositoryWrapper.findByIdWithException(any())).thenReturn(config);
        when(campaignRepositoryWrapper.saveWithException(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(redashService.generateReport(any(RedashReportRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("fail")));

        service.generateDocument(identifier, new CampaignGenerateDocumentRequest());

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepositoryWrapper, atLeast(2)).saveWithException(captor.capture());
        Campaign lastSave = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(CampaignDocumentStatus.FAILED, lastSave.getDocumentDetails().getDocumentStatus());
    }

    @Test
    void generateDocument_success_savesAndUploads() {
        UUID identifier = UUID.randomUUID();
        Campaign campaign = new Campaign();
        campaign.setId(20L);
        campaign.setIdentifier(identifier);
        campaign.setName("camp");
        campaign.setStatus(CampaignStatus.DRAFT);
        CampaignConfig config = new CampaignConfig();
        CampaignConfig.Configs configs = CampaignConfig.Configs.builder()
                .dataProviderId(1L)
                .fileType(FileType.CSV)
                .build();
        config.setConfigs(configs);
        campaign.setDocumentDetails(Campaign.DocumentDetails.builder().build());

        when(campaignRepositoryWrapper.findByIdentifierWithException(identifier))
                .thenReturn(campaign)
                .thenReturn(campaign);
        when(campaignConfigRepositoryWrapper.findByIdWithException(any())).thenReturn(config);
        when(campaignRepositoryWrapper.saveWithException(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        byte[] bytes = "data".getBytes();
        Response response = Response.builder()
                .status(200)
                .reason("OK")
                .headers(Collections.emptyMap())
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "/test",
                        Collections.emptyMap(),
                        bytes,
                        StandardCharsets.UTF_8,
                        null))
                .body(bytes)
                .build();
        when(redashService.generateReport(any(RedashReportRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));
        DocumentCreateResponse docResponse = DocumentCreateResponse.builder().id(123L).build();
        when(documentWriteService.createDocument(any(DocumentCreateRequestInputStream.class))).thenReturn(docResponse);

        service.generateDocument(identifier, new CampaignGenerateDocumentRequest());

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepositoryWrapper, atLeast(2)).saveWithException(captor.capture());
        boolean generatedSaved = captor.getAllValues().stream()
                .anyMatch(c -> c.getDocumentDetails() != null
                        && c.getDocumentDetails().getDocumentStatus() == CampaignDocumentStatus.GENERATED);
        assertTrue(generatedSaved);
        verify(documentWriteService, atLeastOnce()).createDocument(any(DocumentCreateRequestInputStream.class));
    }

    private CampaignConfig configWithStatus(CampaignConfigStatus status) {
        CampaignConfig config = new CampaignConfig();
        config.setIdentifier(UUID.randomUUID());
        config.setStatus(status);
        CampaignConfig.Configs configs = CampaignConfig.Configs.builder()
                .fileType(FileType.CSV)
                .dataProviderId(1L)
                .build();
        config.setConfigs(configs);
        return config;
    }
}
