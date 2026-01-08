package com.nivasafinance.features.campaign.service.impl;

import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.features.campaign.dto.CampaignDraftRequest;
import com.nivasafinance.features.campaign.dto.UpdateCampaignDraftRequest;
import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignProviderDocumentStatus;
import com.nivasafinance.features.campaign.enums.CampaignStatus;
import com.nivasafinance.features.campaign.exception.CampaignExceptionFactory;
import com.nivasafinance.features.campaign.repository.CampaignRepositoryWrapper;
import com.nivasafinance.features.document.dto.DocumentFileResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.voice.VoiceHandler;
import com.nivasafinance.services.voice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceCampaignService {

    private final MessageSource messageSource;
    private final ServiceFactory<VoiceHandler> serviceFactory;
    private final DocumentReadService documentReadService;
    private final CampaignRepositoryWrapper campaignRepositoryWrapper;

    private static final int POLLING_INTERVAL_SECONDS = 2;
    private static final int MAX_TIMEOUT_SECONDS = 120;

    public Campaign createDraftCampaign(CampaignDraftRequest request, CampaignConfig config) {
        // Merge voice details (request overrides config if present)
        Campaign.VoiceDetails voiceDetails = mergeVoiceDetails(
                request.getVoiceDetails(),
                config.getConfigs().getVoiceConfigs()
        );

        // Build Campaign entity
        return Campaign.builder()
                .identifier(UUID.randomUUID())
                .name(request.getName())
                .status(CampaignStatus.DRAFT)
                .configId(config.getId())
                .providerDetails(Campaign.ProviderDetails.builder()
                        .voiceDetails(voiceDetails)
                        .build())
                .build();
    }

    private Campaign.VoiceDetails mergeVoiceDetails(
            CampaignDraftRequest.VoiceDetails requestVoiceDetails,
            CampaignConfig.VoiceConfigs configVoiceConfigs) {

        if (configVoiceConfigs == null) {
            throw CampaignExceptionFactory.voiceConfigsRequired(messageSource);
        }

        Campaign.VoiceDetails.VoiceDetailsBuilder builder = Campaign.VoiceDetails.builder()
                .callerId(configVoiceConfigs.getCallerId())
                .appFlowId(configVoiceConfigs.getAppFlowId())
                .noOfRetries(configVoiceConfigs.getDefaultNoOfRetries())
                .retryInterval(configVoiceConfigs.getDefaultRetryInterval())
                .listId(null)
                .documentUploadId(null);

        // Override CPM if provided in request
        if (requestVoiceDetails != null && requestVoiceDetails.getCpm() != null) {
            builder.cpm(requestVoiceDetails.getCpm());
        } else {
            builder.cpm(configVoiceConfigs.getDefaultCpm());
        }

        return builder.build();
    }

    public void updateDraftCampaign(Campaign campaign, UpdateCampaignDraftRequest request, CampaignConfig config) {
        // Update campaign name
        campaign.setName(request.getName());

        // Get existing voice details
        Campaign.ProviderDetails providerDetails = campaign.getProviderDetails();
        if (providerDetails == null) {
            providerDetails = Campaign.ProviderDetails.builder().build();
            campaign.setProviderDetails(providerDetails);
        }

        Campaign.VoiceDetails existingVoiceDetails = providerDetails.getVoiceDetails();

        // Update voice details (merge request with existing)
        Campaign.VoiceDetails updatedVoiceDetails = updateVoiceDetails(
                request.getVoiceDetails(),
                existingVoiceDetails
        );

        providerDetails.setVoiceDetails(updatedVoiceDetails);
    }

    private Campaign.VoiceDetails updateVoiceDetails(
            UpdateCampaignDraftRequest.VoiceDetails requestVoiceDetails,
            Campaign.VoiceDetails existingVoiceDetails) {

        if (existingVoiceDetails == null) {
            // If no existing voice details, return null or create empty details
            // This shouldn't happen for a draft campaign, but handle gracefully
            return null;
        }

        Campaign.VoiceDetails.VoiceDetailsBuilder builder = Campaign.VoiceDetails.builder()
                .callerId(existingVoiceDetails.getCallerId())
                .appFlowId(existingVoiceDetails.getAppFlowId())
                .noOfRetries(existingVoiceDetails.getNoOfRetries())
                .retryInterval(existingVoiceDetails.getRetryInterval())
                .listId(existingVoiceDetails.getListId())
                .documentUploadId(existingVoiceDetails.getDocumentUploadId())
                .documentStatus(existingVoiceDetails.getDocumentStatus());

        // Override CPM if provided in request, otherwise keep existing value
        if (requestVoiceDetails != null && requestVoiceDetails.getCpm() != null) {
            builder.cpm(requestVoiceDetails.getCpm());
        } else {
            builder.cpm(existingVoiceDetails.getCpm());
        }

        return builder.build();
    }


    private void updateCampaignFromResponse(Campaign campaign, VoiceCampaignResponse response) {
        if (response == null) {
            return;
        }

        // Update provider and providerId
        campaign.setProvider(response.getProviderKey());
        campaign.setProviderId(response.getCampaignId());

        // Map and update status from voice campaign status
        if (response.getStatus() != null) {
            CampaignStatus mappedStatus = CampaignStatus.fromVoiceCampaignStatus(response.getStatus());
            campaign.setStatus(mappedStatus);
        }

        // Build and set summary from response
        if (response.getSummary() != null) {
            Campaign.Summary summary = Campaign.Summary.builder()
                    .reportUrl(response.getReportUrl())
                    .scheduled(response.getSummary().getScheduled())
                    .initialized(response.getSummary().getInitialized())
                    .completed(response.getSummary().getCompleted())
                    .failed(response.getSummary().getFailed())
                    .inProgress(response.getSummary().getInProgress())
                    .build();
            campaign.setSummary(summary);
        }
    }

    public void refreshCampaign(Campaign campaign, CampaignConfig config) {
        // Get VoiceHandler
        VoiceHandler handler = serviceFactory.getHandler(ThirdPartyServiceList.VOICE);

        // Build VoiceGetCampaignDetailsRequest
        VoiceGetCampaignDetailsRequest voiceGetCampaignDetailsRequest = new VoiceGetCampaignDetailsRequest(
                campaign.getProviderId()
        );

        // Create BusinessContext
        BusinessContext businessContext = new BusinessContext(
                SystemEntities.CAMPAIGN.name(),
                campaign.getId(),
                "REFRESH CAMPAIGN"
        );

        // Call VoiceHandler to get campaign details
        VoiceCampaignResponse response = handler.getCampaignDetails(
                voiceGetCampaignDetailsRequest,
                businessContext
        );

        // Update campaign from response
        updateCampaignFromResponse(campaign, response);
    }

    public CompletableFuture<Void> submitCampaign(Campaign campaign, CampaignConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get VoiceHandler
                VoiceHandler handler = serviceFactory.getHandler(ThirdPartyServiceList.VOICE);

                // Get document file
                Campaign.DocumentDetails documentDetails = campaign.getDocumentDetails();
                if (documentDetails == null || documentDetails.getDocumentId() == null) {
                    throw new RuntimeException("Document ID is required for campaign submission");
                }

                log.info("Fetching document file for campaign: {}", campaign.getId());
                DocumentFileResponse documentFile = documentReadService.getDocumentFile(documentDetails.getDocumentId());

                // Upload CSV
                log.info("Uploading CSV for campaign: {}", campaign.getId());
                VoiceCreateListRequest createListRequest = new VoiceCreateListRequest(
                        campaign.getName() + "_list_" + System.currentTimeMillis(),
                        documentFile.getFile()
                );

                BusinessContext uploadContext = new BusinessContext(
                        SystemEntities.CAMPAIGN.name(),
                        campaign.getId(),
                        "UPLOAD CSV FOR CAMPAIGN"
                );

                VoiceCreateListResponse createListResponse = handler.uploadCSVList(createListRequest, uploadContext);
                String requestId = createListResponse.getRequestId();
                String listId = createListResponse.getListId();

                // Update campaign with listId and documentUploadId
                Campaign.VoiceDetails voiceDetails = campaign.getProviderDetails() != null
                        ? campaign.getProviderDetails().getVoiceDetails()
                        : null;
                if (voiceDetails == null) {
                    throw new RuntimeException("Voice details are required for campaign submission");
                }

                // Poll CSV upload status
                log.info("Polling CSV upload status for requestId: {}", requestId);
                VoiceCSVUploadStatus uploadStatus = pollCSVUploadStatus(handler, requestId, campaign.getId());

                if (uploadStatus != VoiceCSVUploadStatus.COMPLETED) {
                    throw new RuntimeException("CSV upload failed with status: " + uploadStatus);
                }

                // Update voice details with listId and documentUploadId
                Campaign.VoiceDetails updatedVoiceDetails = Campaign.VoiceDetails.builder()
                        .callerId(voiceDetails.getCallerId())
                        .appFlowId(voiceDetails.getAppFlowId())
                        .noOfRetries(voiceDetails.getNoOfRetries())
                        .retryInterval(voiceDetails.getRetryInterval())
                        .cpm(voiceDetails.getCpm())
                        .listId(listId)
                        .documentUploadId(requestId)
                        .documentStatus(CampaignProviderDocumentStatus.UPLOADED)
                        .build();

                if (campaign.getProviderDetails() == null) {
                    campaign.setProviderDetails(Campaign.ProviderDetails.builder().build());
                }
                campaign.getProviderDetails().setVoiceDetails(updatedVoiceDetails);

                // Submit campaign
                log.info("Submitting campaign: {}", campaign.getId());
                VoiceCampaignRequest campaignRequest = new VoiceCampaignRequest(
                        campaign.getName(),
                        listId,
                        voiceDetails.getCallerId(),
                        voiceDetails.getAppFlowId(),
                        voiceDetails.getNoOfRetries(),
                        voiceDetails.getRetryInterval(),
                        voiceDetails.getCpm(),
                        null, // file is null since we're using listId
                        new VoiceCampaignRequest.CallBackData(SystemEntities.CAMPAIGN, campaign.getIdentifier().toString())  // callBackData is null for now
                );

                BusinessContext submitContext = new BusinessContext(
                        SystemEntities.CAMPAIGN.name(),
                        campaign.getId(),
                        "SUBMIT CAMPAIGN"
                );

                VoiceCampaignResponse campaignResponse = handler.createCampaign(campaignRequest, submitContext);

                // Update campaign from response
                updateCampaignFromResponse(campaign, campaignResponse);

                // Save campaign with all updates (voice details, provider info, status)
                campaignRepositoryWrapper.saveWithException(campaign);

                log.info("Campaign submitted successfully: {}", campaign.getId());
                return null;
            } catch (Exception e) {
                log.error("Error submitting campaign: {}", campaign.getId(), e);
                throw new RuntimeException("Failed to submit campaign: " + e.getMessage(), e);
            }
        });
    }

    private VoiceCSVUploadStatus pollCSVUploadStatus(VoiceHandler handler, String requestId, Long campaignId)
            throws InterruptedException, TimeoutException {
        long startTime = System.currentTimeMillis();
        VoiceCSVUploadStatus status;

        do {
            // Check timeout
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsedTime >= MAX_TIMEOUT_SECONDS) {
                throw new TimeoutException("CSV upload status polling timed out after " + MAX_TIMEOUT_SECONDS + " seconds");
            }

            // Poll status
            Thread.sleep(POLLING_INTERVAL_SECONDS * 1000);
            log.debug("Polling CSV upload status for requestId: {}", requestId);

            VoiceCSVUploadStatusRequest statusRequest = new VoiceCSVUploadStatusRequest(requestId);

            BusinessContext statusContext = new BusinessContext(
                    SystemEntities.CAMPAIGN.name(),
                    campaignId,
                    "CHECK CSV UPLOAD STATUS"
            );

            VoiceCSVUploadStatusResponse statusResponse = handler.getCSVUploadStatus(statusRequest, statusContext);
            status = statusResponse.getStatus();

            if (status == VoiceCSVUploadStatus.FAILED) {
                throw new RuntimeException("CSV upload failed");
            }

            if (status == VoiceCSVUploadStatus.COMPLETED) {
                log.info("CSV upload completed for requestId: {}", requestId);
                break;
            }

            // Status is IN_PROGRESS - continue polling
            log.debug("CSV upload status: IN_PROGRESS, continuing to poll...");
        } while (status == VoiceCSVUploadStatus.IN_PROGRESS);

        return status;
    }
}
