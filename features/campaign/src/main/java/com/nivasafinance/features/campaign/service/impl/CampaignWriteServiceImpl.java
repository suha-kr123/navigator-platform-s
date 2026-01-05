package com.nivasafinance.features.campaign.service.impl;

import com.nivasafinance.common.utils.FeignResponseUtils;
import com.nivasafinance.features.campaign.dto.CampaignDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignDraftRequest;
import com.nivasafinance.features.campaign.dto.CampaignDraftResponse;
import com.nivasafinance.features.campaign.dto.CampaignGenerateDocumentRequest;
import com.nivasafinance.features.campaign.dto.UpdateCampaignDraftRequest;
import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import com.nivasafinance.features.campaign.enums.CampaignDocumentStatus;
import com.nivasafinance.features.campaign.enums.CampaignStatus;
import com.nivasafinance.features.campaign.exception.CampaignExceptionFactory;
import com.nivasafinance.features.campaign.repository.CampaignConfigRepositoryWrapper;
import com.nivasafinance.features.campaign.repository.CampaignRepositoryWrapper;
import com.nivasafinance.features.campaign.service.CampaignFactory;
import com.nivasafinance.features.campaign.service.CampaignReadService;
import com.nivasafinance.features.campaign.service.CampaignWriteService;
import com.nivasafinance.features.campaign.utils.CampaignDocumentUtils;
import com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.redash.dto.FileType;
import com.nivasafinance.redash.dto.RedashReportRequest;
import com.nivasafinance.redash.service.RedashService;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CampaignWriteServiceImpl implements CampaignWriteService {

    private final CampaignConfigRepositoryWrapper campaignConfigRepositoryWrapper;
    private final CampaignRepositoryWrapper campaignRepositoryWrapper;
    private final CampaignFactory campaignFactory;
    private final CampaignReadService campaignReadService;
    private final MessageSource messageSource;
    private final RedashService redashService;
    private final DocumentWriteService documentWriteService;

    @Override
    public CampaignDraftResponse createCampaignDraft(CampaignDraftRequest request) {
        // Validate CampaignConfig
        CampaignConfig config = campaignConfigRepositoryWrapper.findByIdentifierWithException(request.getConfigIdentifier());

        // Validate config status is ACTIVE
        if (config.getStatus() != CampaignConfigStatus.ACTIVE) {
            throw CampaignExceptionFactory.configNotActive(messageSource);
        }

        // Validate campaign name is unique
        if (campaignRepositoryWrapper.existsByName(request.getName())) {
            throw CampaignExceptionFactory.duplicateName(request.getName(), messageSource);
        }

        // Build Campaign entity
        Campaign campaign = campaignFactory.draftCampaign(request, config);

        // Save Campaign
        Campaign savedCampaign = campaignRepositoryWrapper.saveWithException(campaign);

        // Return response
        return CampaignDraftResponse.builder()
                .identifier(savedCampaign.getIdentifier().toString())
                .build();
    }

    @Override
    public void updateCampaignDraft(UUID identifier, UpdateCampaignDraftRequest request) {
        // Find campaign by identifier
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(identifier);

        // Validate campaign status is DRAFT
        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw CampaignExceptionFactory.campaignNotDraft(messageSource);
        }

        // Get CampaignConfig using campaign's configId
        CampaignConfig config = campaignConfigRepositoryWrapper.findByIdWithException(campaign.getConfigId());

        // Validate name uniqueness if name is being changed
        if (!campaign.getName().equals(request.getName())) {
            if (campaignRepositoryWrapper.existsByName(request.getName())) {
                throw CampaignExceptionFactory.duplicateName(request.getName(), messageSource);
            }
        }

        // Update campaign via factory
        campaignFactory.updateCampaignDraft(campaign, request, config);

        // Save the updated campaign
        campaignRepositoryWrapper.saveWithException(campaign);
    }

    @Override
    public void cancelCampaign(UUID identifier) {
        // Find campaign by identifier
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(identifier);

        // Validate campaign status is DRAFT
        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw CampaignExceptionFactory.campaignNotDraft(messageSource);
        }

        // Update status to CANCELLED
        campaign.setStatus(CampaignStatus.CANCELLED);

        // Save the updated campaign
        campaignRepositoryWrapper.saveWithException(campaign);
    }

    @Override
    public void submitCampaign(UUID identifier) {
        // Find campaign by identifier
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(identifier);

        // Validate campaign status is DRAFT
        if (!campaign.getStatus().canBeSubmitted()) {
            throw CampaignExceptionFactory.campaignNotDraft(messageSource);
        }

        // Validate document is generated
        Campaign.DocumentDetails documentDetails = campaign.getDocumentDetails();
        if (documentDetails == null || documentDetails.getDocumentId() == null) {
            throw CampaignExceptionFactory.documentNotGenerated(messageSource);
        }
        if (documentDetails.getDocumentStatus() != CampaignDocumentStatus.GENERATED) {
            throw CampaignExceptionFactory.documentNotGenerated(messageSource);
        }

        // Get CampaignConfig using campaign's configId
        CampaignConfig config = campaignConfigRepositoryWrapper.findByIdWithException(campaign.getConfigId());

        // Set status to SUBMISSION_IN_PROGRESS
        campaign.setStatus(CampaignStatus.SUBMISSION_IN_PROGRESS);

        // Save campaign with updated status
        campaignRepositoryWrapper.saveWithException(campaign);

        // Call factory to submit campaign (returns CompletableFuture)
        log.info("Starting campaign submission for campaign: {}", identifier);
        campaignFactory.submitCampaign(campaign, config)
                .thenAccept(result -> {
                    try {
                        // Update status to SUBMITTED
                        Campaign updatedCampaign = campaignRepositoryWrapper.findByIdentifierWithException(identifier);
                        updatedCampaign.setStatus(CampaignStatus.SUBMITTED);
                        
                        // Clear any error in provider details
                        if (updatedCampaign.getProviderDetails() != null) {
                            updatedCampaign.getProviderDetails().setError(null);
                        }
                        
                        campaignRepositoryWrapper.saveWithException(updatedCampaign);
                        log.info("Campaign submitted successfully: {}", identifier);
                    } catch (Exception e) {
                        log.error("Error updating campaign status to SUBMITTED for campaign: {}", identifier, e);
                        handleSubmissionError(identifier, e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error submitting campaign: {}", identifier, throwable);
                    String errorMessage = throwable.getCause() != null
                            ? throwable.getCause().getMessage()
                            : throwable.getMessage();
                    handleSubmissionError(identifier, errorMessage);
                    return null;
                });
    }

    private void handleSubmissionError(UUID identifier, String errorMessage) {
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(identifier);
        
        // Set error in provider details
        Campaign.ProviderDetails providerDetails = campaign.getProviderDetails();
        if (providerDetails == null) {
            providerDetails = Campaign.ProviderDetails.builder().build();
            campaign.setProviderDetails(providerDetails);
        }
        providerDetails.setError(errorMessage);

        campaign.setStatus(CampaignStatus.FAILED);
        campaignRepositoryWrapper.saveWithException(campaign);
        log.error("Campaign submission failed for campaign: {}, error: {}", identifier, errorMessage);
    }

    @Override
    public CampaignDetailedResponse refreshCampaign(UUID identifier) {
        // Find campaign by identifier
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(identifier);

        if(campaign.getStatus().isTerminal()){
            throw CampaignExceptionFactory.campaignInTerminalStatus(messageSource);
        }

        // Validate campaign status allows refresh
        if (!campaign.getStatus().canRefresh()) {
            return campaignReadService.getCampaignByIdentifier(identifier);
        }

        // Fetch CampaignConfig using campaign's configId
        CampaignConfig config = campaignConfigRepositoryWrapper.findByIdWithException(campaign.getConfigId());

        // Refresh campaign via factory
        campaignFactory.refreshCampaign(campaign, config);

        // Save the updated campaign
        campaignRepositoryWrapper.saveWithException(campaign);

        // Return detailed response using read service
        return campaignReadService.getCampaignByIdentifier(identifier);
    }

    @Override
    public void generateDocument(UUID campaignIdentifier, CampaignGenerateDocumentRequest request) {
        // Step 1: Find campaign by identifier
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(campaignIdentifier);

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw CampaignExceptionFactory.campaignNotDraft(messageSource);
        }

        // Step 2: Fetch CampaignConfig
        CampaignConfig config = campaignConfigRepositoryWrapper.findByIdWithException(campaign.getConfigId());

        // Step 3: Extract queryId and fileType from CampaignConfig
        CampaignConfig.Configs configs = config.getConfigs();
        if (configs == null || configs.getDataProviderId() == null) {
            throw new RuntimeException("Campaign config missing dataProviderId");
        }
        if (configs.getFileType() == null) {
            throw new RuntimeException("Campaign config missing fileType");
        }
        Long queryId = configs.getDataProviderId();
        FileType fileType = configs.getFileType();

        // Step 4: Initialize or update DocumentDetails
        Campaign.DocumentDetails documentDetails = campaign.getDocumentDetails();
        if (documentDetails == null) {
            documentDetails = Campaign.DocumentDetails.builder().build();
        }
        documentDetails.setDocumentStatus(CampaignDocumentStatus.GENERATION_IN_PROGRESS);
        documentDetails.setError(null);
        documentDetails.setDocumentId(null);
        campaign.setDocumentDetails(documentDetails);

        // Step 5: Save campaign with updated status
        campaignRepositoryWrapper.saveWithException(campaign);

        // Step 6: Build RedashReportRequest
        RedashReportRequest redashRequest = RedashReportRequest.builder()
                .queryId(queryId)
                .fileType(fileType)
                .parameters(request.getParameters())
                .build();

        // Step 7: Call redashService.generateReport() - returns CompletableFuture<Response>
        log.info("Starting document generation for campaign: {}", campaignIdentifier);
        redashService.generateReport(redashRequest)
                .thenAccept(response -> {
                    try {
                        handleDocumentGenerationSuccess(campaignIdentifier, campaign.getName(), fileType, response);
                    } catch (Exception e) {
                        log.error("Error handling document generation success for campaign: {}", campaignIdentifier, e);
                        handleDocumentGenerationError(campaignIdentifier, e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error generating document for campaign: {}", campaignIdentifier, throwable);
                    String errorMessage = throwable.getCause() != null
                            ? throwable.getCause().getMessage()
                            : throwable.getMessage();
                    handleDocumentGenerationError(campaignIdentifier, errorMessage);
                    return null;
                });
    }

    private void handleDocumentGenerationSuccess(UUID campaignIdentifier, String campaignName,
                                                 FileType fileType, Response response) {
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(campaignIdentifier);

        try (InputStream inputStream = response.body().asInputStream()) {
            // Get content type from response headers
            String contentType = FeignResponseUtils.getContentTypeFromResponse(response);

            // Get content length from response headers or calculate
            Long contentLength = FeignResponseUtils.getContentLengthFromResponse(response);

            // Generate file path
            String fileName = campaign.getName() + "_" + System.currentTimeMillis() + "." + fileType.name().toLowerCase();
            String filePath = CampaignDocumentUtils.generateDocumentPathForContactsFile(campaign.getId(), fileName);

            // Create DocumentCreateRequestInputStream
            DocumentCreateRequestInputStream documentRequest = DocumentCreateRequestInputStream.builder()
                    .name(fileName)
                    .file(inputStream)
                    .contentType(contentType)
                    .customPath(filePath)
                    .size(contentLength)
                    .build();

            // Upload document
            log.info("Uploading document for campaign: {}", campaignIdentifier);
            DocumentCreateResponse documentResponse = documentWriteService.createDocument(documentRequest);

            // Update campaign with document ID and status
            Campaign.DocumentDetails documentDetails = campaign.getDocumentDetails();
            if (documentDetails == null) {
                documentDetails = Campaign.DocumentDetails.builder().build();
            }
            documentDetails.setDocumentId(documentResponse.getId());
            documentDetails.setDocumentStatus(CampaignDocumentStatus.GENERATED);
            documentDetails.setError(null);
            campaign.setDocumentDetails(documentDetails);

            // Save campaign
            campaignRepositoryWrapper.saveWithException(campaign);
            log.info("Successfully generated and uploaded document for campaign: {}", campaignIdentifier);

        } catch (IOException e) {
            log.error("Error reading response stream for campaign: {}", campaignIdentifier, e);
            throw new RuntimeException("Failed to read document from response", e);
        }
    }

    private void handleDocumentGenerationError(UUID campaignIdentifier, String errorMessage) {
        Campaign campaign = campaignRepositoryWrapper.findByIdentifierWithException(campaignIdentifier);

        Campaign.DocumentDetails documentDetails = campaign.getDocumentDetails();
        if (documentDetails == null) {
            documentDetails = Campaign.DocumentDetails.builder().build();
        }
        documentDetails.setDocumentStatus(CampaignDocumentStatus.FAILED);
        documentDetails.setError(errorMessage);
        campaign.setDocumentDetails(documentDetails);

        campaignRepositoryWrapper.saveWithException(campaign);
        log.error("Document generation failed for campaign: {}, error: {}", campaignIdentifier, errorMessage);
    }
}
