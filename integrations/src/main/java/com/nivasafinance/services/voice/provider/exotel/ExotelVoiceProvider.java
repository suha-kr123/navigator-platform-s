package com.nivasafinance.services.voice.provider.exotel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.NavigatorRestService;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponse;
import com.nivasafinance.integrations.framework.core.data.IntegrationRestRequest;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationClientException;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationServerException;
import com.nivasafinance.services.voice.dto.*;
import com.nivasafinance.services.voice.provider.VoiceProvider;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelAddContactsCSVToListResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelConfiguration;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelCreateCampaignRequest;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelCreateCampaignResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelCSVUploadStatusResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelGetCallLegsResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelGetCallStatusResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelGetCampaignResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelV3CallRequest;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelV3CallResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import org.springframework.core.io.ByteArrayResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExotelVoiceProvider implements VoiceProvider {

    private final NavigatorRestService restService;
    private final ObjectMapper objectMapper;
    private final SecretManagerService secretManagerService;

    @Override
    public ThirdPartyProviderList getKey() {
        return ThirdPartyProviderList.EXOTEL;
    }

    @Override
    public ExotelConfiguration setupConfiguration(Map<String, String> map) {

        if(map == null || map.isEmpty()) {
            throw new NavigatorIntegrationServerException("Exotel configuration is empty.");
        }

        if(map.containsKey("secret_key")) {
           Map<String,Object> secretMap = secretManagerService.getSecret(map.get("secret_key"));
           return new ExotelConfiguration(
                   secretMap.get("accountSid").toString(),
                   secretMap.get("baseUrl").toString(),
                   secretMap.get("callWebhookUrl").toString(),
                   secretMap.get("campaignWebhookUrl").toString(),
                   secretMap.get("campaignCallWebhookUrl").toString(),
                   secretMap.get("apiKey").toString(),
                   secretMap.get("apiToken").toString()
           );
        }

        return new ExotelConfiguration(
                map.get("accountSid"),
                map.get("baseUrl"),
                map.get("callWebhookUrl"),
                map.get("campaignWebhookUrl"),
                map.get("campaignCallWebhookUrl"),
                map.get("apiKey"),
                map.get("apiToken")
        );
    }

    @Override
    public VoiceCallResponse makeCall(
            VoiceCallRequest request,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        ExotelConfiguration exotelConfig = setupConfiguration(config.getConfigurations());
        validateVoiceCallRequest(request);
        IntegrationRestRequest<String> restRequest =
                buildConnectCallRequest(request, exotelConfig, config, businessContext);
        IntegrationResponse integrationResponse = restService.doRestRequest(restRequest);
        if (!integrationResponse.isSuccess()) {
            throw new NavigatorIntegrationClientException(
                    "Exotel call failed: "
                            + (integrationResponse.getErrorMessage() != null
                            ? integrationResponse.getErrorMessage()
                            : integrationResponse.getResponseBody()));
        }
        return mapResponse(integrationResponse);
    }

    private static final int CALL_STATUS_RETRY_ATTEMPTS = 6;
    private static final int CALL_STATUS_RETRY_DELAY_SECONDS = 10;

    @Override
    public VoiceGetCallStatusResponse getCallStatus(
            String callSid,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        ExotelConfiguration exotelConfig = setupConfiguration(config.getConfigurations());
        validateGetCallStatusRequest(callSid);

        IntegrationRestRequest<Void> callRequest =
                buildGetCallStatusRequest(callSid, exotelConfig, config, businessContext);
        IntegrationResponse callResponse = executeWithRetry(
                callRequest,
                CALL_STATUS_RETRY_ATTEMPTS,
                CALL_STATUS_RETRY_DELAY_SECONDS,
                "Exotel get call status failed");

        ExotelGetCallStatusResponse exotelCallResponse;
        try {
            exotelCallResponse = objectMapper.readValue(
                    callResponse.getResponseBody(),
                    ExotelGetCallStatusResponse.class);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to parse get call status response: " + e.getMessage());
        }

        ExotelGetCallStatusResponse.CallDetails callDetails = exotelCallResponse.getCallDetails();
        if (callDetails == null) {
            throw new NavigatorIntegrationClientException("Call details not found in response");
        }

        IntegrationRestRequest<Void> legsRequest =
                buildGetCallLegsRequest(callSid, exotelConfig, config, businessContext);
        IntegrationResponse legsResponse = executeWithRetryOptional(
                legsRequest,
                CALL_STATUS_RETRY_ATTEMPTS,
                CALL_STATUS_RETRY_DELAY_SECONDS,
                "Exotel get call legs details failed");

        ExotelGetCallLegsResponse.LegDetails legDetails = null;
        if (legsResponse != null && legsResponse.isSuccess()) {
            try {
                ExotelGetCallLegsResponse exotelLegsResponse = objectMapper.readValue(
                        legsResponse.getResponseBody(),
                        ExotelGetCallLegsResponse.class);
                legDetails = exotelLegsResponse != null ? exotelLegsResponse.getLegDetails() : null;
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse get call legs response after retries, keeping legs null: {}", e.getMessage());
            }
        } else if (legsResponse == null) {
            log.warn("Legs API failed after {} retries, keeping legs null", CALL_STATUS_RETRY_ATTEMPTS);
        }

        return buildVoiceGetCallStatusResponse(callDetails, legDetails);
    }

    private IntegrationResponse executeWithRetry(
            IntegrationRestRequest<Void> request,
            int maxAttempts,
            int delaySeconds,
            String errorPrefix) {
        IntegrationResponse response = null;
        String lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            response = restService.doRestRequest(request);
            if (response.isSuccess()) {
                return response;
            }
            lastError = response.getErrorMessage() != null ? response.getErrorMessage() : response.getResponseBody();
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new NavigatorIntegrationClientException(errorPrefix + ": interrupted during retry");
                }
            }
        }
        throw new NavigatorIntegrationClientException(errorPrefix + ": " + lastError);
    }

    private IntegrationResponse executeWithRetryOptional(
            IntegrationRestRequest<Void> request,
            int maxAttempts,
            int delaySeconds,
            String errorLogPrefix) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            IntegrationResponse response = restService.doRestRequest(request);
            if (response.isSuccess()) {
                return response;
            }
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("{}: interrupted during retry", errorLogPrefix);
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public VoiceCreateListResponse uploadCSVList(VoiceCreateListRequest request, ThirdPartyConfig config, BusinessContext businessContext) {
        ExotelConfiguration exotelConfig = setupConfiguration(config.getConfigurations());
        validateCreateListRequest(request);
        
        // Upload CSV file to the created list
        IntegrationRestRequest<MultiValueMap<String, Object>> uploadCSVRequest =
                buildUploadCSVRequest(request, exotelConfig, config, businessContext);
        IntegrationResponse uploadCSVResponse = restService.doRestRequest(uploadCSVRequest);
        
        if (!uploadCSVResponse.isSuccess()) {
            throw new NavigatorIntegrationClientException(
                    "Exotel CSV upload failed: "
                            + (uploadCSVResponse.getErrorMessage() != null
                            ? uploadCSVResponse.getErrorMessage()
                            : uploadCSVResponse.getResponseBody()));
        }
        
        // Parse upload CSV response to get request ID
        ExotelAddContactsCSVToListResponse exotelUploadResponse;
        try {
            exotelUploadResponse = objectMapper.readValue(
                    uploadCSVResponse.getResponseBody(),
                    ExotelAddContactsCSVToListResponse.class);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to parse CSV upload response: " + e.getMessage());
        }
        
        return VoiceCreateListResponse.builder()
                .listId(exotelUploadResponse.getListSid())
                .requestId(exotelUploadResponse.getUploadId())
                .build();
    }

    @Override
    public VoiceCSVUploadStatusResponse getCSVUploadStatus(VoiceCSVUploadStatusRequest request, ThirdPartyConfig config, BusinessContext businessContext) {
        ExotelConfiguration exotelConfig = setupConfiguration(config.getConfigurations());
        validateCSVUploadStatusRequest(request);
        
        // Build GET request to fetch CSV upload status
        IntegrationRestRequest<Void> statusRequest =
                buildCSVUploadStatusRequest(request, exotelConfig, config, businessContext);
        IntegrationResponse statusResponse = restService.doRestRequest(statusRequest);
        
        if (!statusResponse.isSuccess()) {
            throw new NavigatorIntegrationClientException(
                    "Exotel CSV upload status check failed: "
                            + (statusResponse.getErrorMessage() != null
                            ? statusResponse.getErrorMessage()
                            : statusResponse.getResponseBody()));
        }
        
        // Parse response
        ExotelCSVUploadStatusResponse exotelStatusResponse;
        try {
            exotelStatusResponse = objectMapper.readValue(
                    statusResponse.getResponseBody(),
                    ExotelCSVUploadStatusResponse.class);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to parse CSV upload status response: " + e.getMessage());
        }
        
        // Map Exotel status to VoiceCSVUploadStatus enum
        VoiceCSVUploadStatus status = mapCSVUploadStatus(exotelStatusResponse.getStatus());
        
        return VoiceCSVUploadStatusResponse.builder()
                .status(status)
                .build();
    }

    @Override
    public VoiceCampaignResponse createCampaign(VoiceCampaignRequest request, ThirdPartyConfig config, BusinessContext businessContext) {
        ExotelConfiguration exotelConfig = setupConfiguration(config.getConfigurations());
        validateCampaignRequest(request);
        
        // Build Exotel campaign request
        ExotelCreateCampaignRequest exotelRequest = buildExotelCampaignRequest(request, exotelConfig);
        
        // Make API call
        IntegrationRestRequest<ExotelCreateCampaignRequest> campaignRequest =
                buildCreateCampaignRequest(exotelRequest, exotelConfig, config, businessContext);
        IntegrationResponse campaignResponse = restService.doRestRequest(campaignRequest);
        
        if (!campaignResponse.isSuccess()) {
            throw new NavigatorIntegrationClientException(
                    "Exotel create campaign failed: "
                            + (campaignResponse.getErrorMessage() != null
                            ? campaignResponse.getErrorMessage()
                            : campaignResponse.getResponseBody()));
        }
        
        // Parse response
        ExotelCreateCampaignResponse exotelCampaignResponse;
        try {
            exotelCampaignResponse = objectMapper.readValue(
                    campaignResponse.getResponseBody(),
                    ExotelCreateCampaignResponse.class);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to parse create campaign response: " + e.getMessage());
        }
        
        String campaignId = exotelCampaignResponse.getId();
        if (campaignId == null || campaignId.isBlank()) {
            throw new NavigatorIntegrationClientException("Campaign ID not found in create campaign response");
        }
        
        // Return response (VoiceCampaignResponse is currently empty, but structure is ready for future fields)
        return VoiceCampaignResponse.builder().campaignId(campaignId).providerKey(ThirdPartyProviderList.EXOTEL.getProvideName()).build();
    }

    @Override
    public VoiceCampaignResponse getCampaignDetails(VoiceGetCampaignDetailsRequest request, ThirdPartyConfig config, BusinessContext businessContext) {
        ExotelConfiguration exotelConfig = setupConfiguration(config.getConfigurations());
        validateGetCampaignDetailsRequest(request);
        
        // Build GET request to fetch campaign details
        IntegrationRestRequest<Void> campaignRequest =
                buildGetCampaignRequest(request, exotelConfig, config, businessContext);
        IntegrationResponse campaignResponse = restService.doRestRequest(campaignRequest);
        
        if (!campaignResponse.isSuccess()) {
            throw new NavigatorIntegrationClientException(
                    "Exotel get campaign details failed: "
                            + (campaignResponse.getErrorMessage() != null
                            ? campaignResponse.getErrorMessage()
                            : campaignResponse.getResponseBody()));
        }
        
        // Parse response
        ExotelGetCampaignResponse exotelCampaignResponse;
        try {
            exotelCampaignResponse = objectMapper.readValue(
                    campaignResponse.getResponseBody(),
                    ExotelGetCampaignResponse.class);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to parse get campaign details response: " + e.getMessage());
        }
        
        ExotelGetCampaignResponse.CampaignData campaignData = exotelCampaignResponse.getCampaignData();
        if (campaignData == null) {
            throw new NavigatorIntegrationClientException("Campaign data not found in response");
        }
        
        ExotelGetCampaignResponse.Summary exotelSummary = exotelCampaignResponse.getSummary();
        
        // Map Exotel status to VoiceCampaignStatus enum
        VoiceCampaignStatus status = mapCampaignStatus(campaignData.getStatus());
        
        // Build summary
        VoiceCampaignResponse.Summary summary = null;
        if (exotelSummary != null) {
            summary = VoiceCampaignResponse.Summary.builder()
                    .scheduled(exotelSummary.getCallScheduled())
                    .initialized(exotelSummary.getCallInitialized())
                    .completed(exotelSummary.getCallCompleted())
                    .failed(exotelSummary.getCallFailed())
                    .inProgress(exotelSummary.getCallInProgress())
                    .build();
        }
        
        return new VoiceCampaignResponse(
                campaignData.getId(),
                ThirdPartyProviderList.EXOTEL.getProvideName(),
                status,
                campaignData.getReportUrl(),
                summary
        );
    }

    private void validateVoiceCallRequest(VoiceCallRequest request) {
        if (request.getFromNumber() == null || request.getFromNumber().isBlank()) {
            throw new NavigatorIntegrationClientException("From number cannot be empty");
        }
        if (request.getToNumber() == null || request.getToNumber().isBlank()) {
            throw new NavigatorIntegrationClientException("To number cannot be empty");
        }
        if (request.getCallerId() == null || request.getCallerId().isBlank()) {
            throw new NavigatorIntegrationClientException("Caller ID cannot be empty");
        }
    }
    
    private void validateCreateListRequest(VoiceCreateListRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new NavigatorIntegrationClientException("List name cannot be empty");
        }
        if (request.getFile() == null) {
            throw new NavigatorIntegrationClientException("CSV file cannot be empty");
        }
    }
    
    private void validateCSVUploadStatusRequest(VoiceCSVUploadStatusRequest request) {
        if (request.getRequestId() == null || request.getRequestId().isBlank()) {
            throw new NavigatorIntegrationClientException("Request ID cannot be empty");
        }
    }
    
    private void validateCampaignRequest(VoiceCampaignRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new NavigatorIntegrationClientException("Campaign name cannot be empty");
        }
        if (request.getListId() == null || request.getListId().isBlank()) {
            throw new NavigatorIntegrationClientException("List ID cannot be empty");
        }
        if (request.getCallerId() == null || request.getCallerId().isBlank()) {
            throw new NavigatorIntegrationClientException("Caller ID cannot be empty");
        }
        if (request.getCallFlowId() == null || request.getCallFlowId().isBlank()) {
            throw new NavigatorIntegrationClientException("Call flow ID cannot be empty");
        }
    }
    
    private void validateGetCampaignDetailsRequest(VoiceGetCampaignDetailsRequest request) {
        if (request.getCampaignId() == null || request.getCampaignId().isBlank()) {
            throw new NavigatorIntegrationClientException("Campaign ID cannot be empty");
        }
    }

    private void validateGetCallStatusRequest(String callSid) {
        if (callSid == null || callSid.isBlank()) {
            throw new NavigatorIntegrationClientException("Call SID cannot be empty");
        }
    }

    private static final String EXOTEL_CALL_WEBHOOK_URL = "https://a136636e317a.ngrok-free.app/external/v1/exotel/outgoing/callback";

    private IntegrationRestRequest<String> buildConnectCallRequest(
            VoiceCallRequest request,
            ExotelConfiguration exotelConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext
    ) {
        String url = String.format(
                "%s/v3/accounts/%s/calls",
                EXOTEL_CCM_API_BASE,
                exotelConfig.getAccountSid());

        String customField;
        try {
            customField = request.getCallBackData() != null
                    ? objectMapper.writeValueAsString(request.getCallBackData())
                    : null;
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to serialize callback data: " + e.getMessage());
        }

        ExotelV3CallRequest payload = ExotelV3CallRequest.builder()
                .from(ExotelV3CallRequest.ContactUri.builder().contactUri(request.getFromNumber()).build())
                .to(ExotelV3CallRequest.ContactUri.builder().contactUri(request.getToNumber()).build())
                .recording(ExotelV3CallRequest.Recording.builder().record(true).channels("single").build())
                .virtualNumber(request.getCallerId())
                .customField(customField)
                .statusCallback(Collections.singletonList(
                        ExotelV3CallRequest.StatusCallback.builder()
                                .event("terminal")
                                .url(EXOTEL_CALL_WEBHOOK_URL)
                                .build()))
                .build();

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to serialize call request: " + e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(exotelConfig));

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                config.getId()
        );

        IntegrationRestRequest<String> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.POST);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(requestBody);
        restRequest.setHeaders(headers);

        return restRequest;
    }

    private IntegrationRestRequest<MultiValueMap<String, Object>> buildUploadCSVRequest(
            VoiceCreateListRequest request,
            ExotelConfiguration exotelConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext
    ) {
        String url = String.format(
                "%s/v2/accounts/%s/contacts/csv-upload",
                exotelConfig.getBaseUrl(),
                exotelConfig.getAccountSid());

        MultiValueMap<String, Object> payload = new LinkedMultiValueMap<>();
        payload.add("list_name", request.getName());
        
        // Convert InputStream to ByteArrayResource for multipart upload
        // ByteArrayResource can be read multiple times, unlike InputStreamResource
        byte[] fileBytes;
        try {
            fileBytes = request.getFile().readAllBytes();
        } catch (IOException e) {
            throw new NavigatorIntegrationClientException("Failed to read file content: " + e.getMessage());
        }
        
        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                // Use list name as default filename if original filename is not available
                return request.getName() + ".csv";
            }
        };
        payload.add("file_name", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(exotelConfig));

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                config.getId()
        );

        IntegrationRestRequest<MultiValueMap<String, Object>> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.POST);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(payload);
        restRequest.setHeaders(headers);

        return restRequest;
    }

    private IntegrationRestRequest<Void> buildCSVUploadStatusRequest(
            VoiceCSVUploadStatusRequest request,
            ExotelConfiguration exotelConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext
    ) {
        // requestId contains the upload_id from the previous upload response
        String url = String.format(
                "%s/v2/accounts/%s/csv-status/%s",
                exotelConfig.getBaseUrl(),
                exotelConfig.getAccountSid(),
                request.getRequestId());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(exotelConfig));

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                config.getId()
        );

        IntegrationRestRequest<Void> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.GET);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(null);
        restRequest.setHeaders(headers);

        return restRequest;
    }
    
    private VoiceCSVUploadStatus mapCSVUploadStatus(String status) {
        if (status == null) {
            return VoiceCSVUploadStatus.IN_PROGRESS;
        }
        return switch (status.toLowerCase()) {
            case "completed" -> VoiceCSVUploadStatus.COMPLETED;
            case "failed" -> VoiceCSVUploadStatus.FAILED;
            default -> VoiceCSVUploadStatus.IN_PROGRESS;
        };
    }
    
    private VoiceCampaignStatus mapCampaignStatus(String status) {
        if (status == null) {
            return VoiceCampaignStatus.CREATED;
        }
        return switch (status.toLowerCase()) {
            case "completed" -> VoiceCampaignStatus.COMPLETED;
            case "failed" -> VoiceCampaignStatus.FAILED;
            case "in-progress", "inprogress" -> VoiceCampaignStatus.IN_PROGRESS;
            case "cancelled", "canceled", "archived" -> VoiceCampaignStatus.CANCELLED;
            case "paused" -> VoiceCampaignStatus.PAUSED;
            case "created" -> VoiceCampaignStatus.CREATED;
            default -> VoiceCampaignStatus.CREATED;
        };
    }

    private ExotelCreateCampaignRequest buildExotelCampaignRequest(
            VoiceCampaignRequest request,
            ExotelConfiguration exotelConfig
    ) {
        // Build retries object
        ExotelCreateCampaignRequest.ExotelRetries retries = ExotelCreateCampaignRequest.ExotelRetries.builder()
                .mechanism("Exponential")
                .noOfRetries(request.getNoOfRetries())
                .intervalMins(request.getRetryIntervalMins())
                .onStatus(Arrays.asList("busy", "no-answer"))
                .build();
        
        // Build custom field JSON
        String customField = null;
        if (request.getCallBackData() != null) {
            try {
                customField = objectMapper.writeValueAsString(request.getCallBackData());
            } catch (JsonProcessingException e) {
                throw new NavigatorIntegrationClientException("Failed to serialize callback data: " + e.getMessage());
            }
        }
        
        // Build URL: https://my.exotel.com/{accountSid}/exoml/start_voice/{callFlowId}
        String flowUrl = String.format(
                "https://my.exotel.com/%s/exoml/start_voice/%s",
                exotelConfig.getAccountSid(),
                request.getCallFlowId());
        
        // Build lists array
        List<String> lists = Collections.singletonList(request.getListId());
        
        // Build schedule if scheduledAt is provided
        ExotelCreateCampaignRequest.Schedule schedule = null;
        if (request.getScheduledAt() != null) {
            // Format LocalDateTime to ISO-8601 with timezone offset (e.g., "2022-08-08T16:34:22+05:30")
            // Using Asia/Kolkata timezone (IST) which is +05:30
            ZoneId zoneId = ZoneId.of("Asia/Kolkata");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
            String sendAt = request.getScheduledAt().atZone(zoneId).format(formatter);
            
            schedule = ExotelCreateCampaignRequest.Schedule.builder()
                    .sendAt(sendAt)
                    .build();
        }
        
        // Build campaign object
        ExotelCreateCampaignRequest.Campaign campaign = ExotelCreateCampaignRequest.Campaign.builder()
                .name(request.getName())
                .type("trans")
                .campaignType("static")
                .url(flowUrl)
                .callerId(request.getCallerId())
                .lists(lists)
                .statusCallback(exotelConfig.getCampaignWebhookUrl())
                .callStatusCallback(exotelConfig.getCampaignCallWebhookUrl())
                .callDuplicateNumbers(false)
                .mode("custom")
                .throttle(request.getCpm())
                .retries(retries)
                .customField(customField)
                .schedule(schedule)
                .build();
        
        // Wrap campaign in campaigns array
        return ExotelCreateCampaignRequest.builder()
                .campaigns(Collections.singletonList(campaign))
                .build();
    }
    
    private IntegrationRestRequest<ExotelCreateCampaignRequest> buildCreateCampaignRequest(
            ExotelCreateCampaignRequest exotelRequest,
            ExotelConfiguration exotelConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext
    ) {
        String url = String.format(
                "%s/v2/accounts/%s/campaigns",
                exotelConfig.getBaseUrl(),
                exotelConfig.getAccountSid());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(exotelConfig));

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                config.getId()
        );

        IntegrationRestRequest<ExotelCreateCampaignRequest> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.POST);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(exotelRequest);
        restRequest.setHeaders(headers);

        return restRequest;
    }
    
    private static final String EXOTEL_CCM_API_BASE = "https://ccm-api.exotel.com";

    private IntegrationRestRequest<Void> buildGetCallStatusRequest(
            String callSid,
            ExotelConfiguration exotelConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext
    ) {
        String url = String.format(
                "%s/v3/accounts/%s/calls/%s",
                EXOTEL_CCM_API_BASE,
                exotelConfig.getAccountSid(),
                callSid);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(exotelConfig));

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                config.getId()
        );

        IntegrationRestRequest<Void> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.GET);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(null);
        restRequest.setHeaders(headers);

        return restRequest;
    }

    private IntegrationRestRequest<Void> buildGetCallLegsRequest(
            String callSid,
            ExotelConfiguration exotelConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext
    ) {
        String url = String.format(
                "%s/v3/accounts/%s/calls/%s/legs",
                EXOTEL_CCM_API_BASE,
                exotelConfig.getAccountSid(),
                callSid);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(exotelConfig));

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                config.getId()
        );

        IntegrationRestRequest<Void> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.GET);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(null);
        restRequest.setHeaders(headers);

        return restRequest;
    }

    private IntegrationRestRequest<Void> buildGetCampaignRequest(
            VoiceGetCampaignDetailsRequest request,
            ExotelConfiguration exotelConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext
    ) {
        String url = String.format(
                "%s/v2/accounts/%s/campaigns/%s",
                exotelConfig.getBaseUrl(),
                exotelConfig.getAccountSid(),
                request.getCampaignId());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(exotelConfig));

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                config.getId()
        );

        IntegrationRestRequest<Void> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.GET);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(null);
        restRequest.setHeaders(headers);

        return restRequest;
    }

    private String buildAuthorizationHeader(ExotelConfiguration config) {
        String credentials = config.getApiKey() + ":" + config.getApiToken();
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private VoiceCallResponse mapResponse(IntegrationResponse response) {
        String responseBody = response.getResponseBody();
        if (responseBody == null || responseBody.isBlank()) {
            throw new NavigatorIntegrationClientException("Empty response received from Exotel");
        }

        ExotelV3CallResponse exotelCallResponse;
        try {
            exotelCallResponse = objectMapper.readValue(responseBody, ExotelV3CallResponse.class);
        } catch (JsonProcessingException e) {
            throw new NavigatorIntegrationClientException("Failed to parse Exotel call response: " + e.getMessage());
        }

        ExotelV3CallResponse.CallDetails callDetails = exotelCallResponse.getCallDetails();
        if (callDetails == null) {
            throw new NavigatorIntegrationClientException("Missing call details in Exotel response");
        }
        if (callDetails.getSid() == null || callDetails.getSid().isBlank()) {
            throw new NavigatorIntegrationClientException("Missing Sid in Exotel response");
        }

        String status = callDetails.getStatus() != null ? callDetails.getStatus() : callDetails.getState();
        return new VoiceCallResponse(callDetails.getSid(), getStatus(status));
    }

    private VoiceGetCallStatusResponse buildVoiceGetCallStatusResponse(
            ExotelGetCallStatusResponse.CallDetails callDetails,
            ExotelGetCallLegsResponse.LegDetails legDetails) {
        VoiceGetCallStatusResponse.CallDetails voiceCallDetails = null;
        if (callDetails != null) {
            List<VoiceGetCallStatusResponse.Recording> recordings = null;
            if (callDetails.getRecordings() != null) {
                recordings = callDetails.getRecordings().stream()
                        .map(r -> VoiceGetCallStatusResponse.Recording.builder().url(r.getUrl()).build())
                        .toList();
            }

            Map<String, String> entityIds = extractEntityIds(callDetails.getCustomField());

            voiceCallDetails = VoiceGetCallStatusResponse.CallDetails.builder()
                    .sid(callDetails.getSid())
                    .direction(callDetails.getDirection())
                    .virtualNumber(callDetails.getVirtualNumber())
                    .state(callDetails.getState())
                    .status(callDetails.getStatus())
                    .legs(callDetails.getLegs())
                    .createdTime(parseDateTime(callDetails.getCreatedTime()))
                    .updatedTime(parseDateTime(callDetails.getUpdatedTime()))
                    .startTime(parseDateTime(callDetails.getStartTime()))
                    .endTime(parseDateTime(callDetails.getEndTime()))
                    .totalDuration(callDetails.getTotalDuration())
                    .totalTalkTime(callDetails.getTotalTalkTime())
                    .appId(callDetails.getAppId())
                    .appName(callDetails.getAppName())
                    .digits(callDetails.getDigits())
                    .campaignId(entityIds.get("campaignId"))
                    .leadId(entityIds.get("leadId"))
                    .advisorId(entityIds.get("advisorId"))
                    .recordings(recordings)
                    .build();
        }

        String fromLegStatus = null;
        String toLegStatus = null;
        VoiceGetCallStatusResponse.Leg fromLeg = null;
        VoiceGetCallStatusResponse.Leg toLeg = null;
        if (legDetails != null) {
            if (legDetails.getFrom() != null && !legDetails.getFrom().isEmpty()) {
                ExotelGetCallLegsResponse.Leg exotelFrom = legDetails.getFrom().get(0);
                fromLegStatus = exotelFrom.getStatus();
                fromLeg = mapToVoiceGetCallStatusLeg(exotelFrom);
            }
            if (legDetails.getTo() != null && !legDetails.getTo().isEmpty()) {
                ExotelGetCallLegsResponse.Leg exotelTo = legDetails.getTo().get(0);
                toLegStatus = exotelTo.getStatus();
                toLeg = mapToVoiceGetCallStatusLeg(exotelTo);
            }
        }

        String callStatus = callDetails != null ? callDetails.getStatus() : null;
        VoiceStatus status = mapExotelStatusToVoiceStatus(callStatus, fromLegStatus, toLegStatus);

        return VoiceGetCallStatusResponse.builder()
                .callId(callDetails != null ? callDetails.getSid() : null)
                .status(status)
                .callDetails(voiceCallDetails)
                .from(fromLeg)
                .to(toLeg)
                .build();
    }

    private VoiceGetCallStatusResponse.Leg mapToVoiceGetCallStatusLeg(ExotelGetCallLegsResponse.Leg exotelLeg) {
        if (exotelLeg == null) {
            return null;
        }
        return VoiceGetCallStatusResponse.Leg.builder()
                .status(mapLegStatusToVoiceStatus(exotelLeg.getStatus()))
                .contactUri(exotelLeg.getContactUri())
                .build();
    }

    private VoiceStatus mapLegStatusToVoiceStatus(String legStatus) {
        String norm = normalizeLegStatus(legStatus);
        if (norm == null) {
            return VoiceStatus.FAILED;
        }
        return switch (norm) {
            case "queued" -> VoiceStatus.QUEUED;
            case "in-progress" -> VoiceStatus.IN_PROGRESS;
            case "completed" -> VoiceStatus.COMPLETED;
            case "failed", "canceled", "cancelled" -> VoiceStatus.FAILED;
            case "no-answer" -> VoiceStatus.NO_ANSWER;
            case "busy" -> VoiceStatus.BUSY;
            default -> VoiceStatus.IN_PROGRESS;
        };
    }

    private VoiceStatus mapExotelStatusToVoiceStatus(String callStatus, String fromLegStatus, String toLegStatus) {
        String fromNorm = normalizeLegStatus(fromLegStatus);
        String toNorm = normalizeLegStatus(toLegStatus);
        boolean hasLegData = fromNorm != null || toNorm != null;

        if (hasLegData) {
            if ("in-progress".equals(fromNorm) || "in-progress".equals(toNorm)) {
                return VoiceStatus.IN_PROGRESS;
            }
            if ("no-answer".equals(fromNorm) || "no-answer".equals(toNorm)) {
                return VoiceStatus.NO_ANSWER;
            }
            if ("failed".equals(fromNorm) || "canceled".equals(fromNorm) || "cancelled".equals(fromNorm)
                || "failed".equals(toNorm) || "canceled".equals(toNorm) || "cancelled".equals(toNorm)) {
                return VoiceStatus.FAILED;
            }
        }

        String callNorm = callStatus != null ? callStatus.toLowerCase().trim() : null;
        if ("completed".equals(callNorm)) {
            if (!hasLegData) {
                return VoiceStatus.COMPLETED;
            }
            if ("completed".equals(fromNorm) && "completed".equals(toNorm)) {
                return VoiceStatus.COMPLETED;
            }
            return VoiceStatus.FAILED;
        }
        if ("from_leg_unanswered".equals(callNorm) || "to_leg_unanswered".equals(callNorm)) {
            return VoiceStatus.NO_ANSWER;
        }
        if ("from_leg_cancelled".equals(callNorm)) {
            return VoiceStatus.FAILED;
        }
        if ("to_leg_no_dial".equals(callNorm) || "from_leg_no_dial".equals(callNorm)) {
            return VoiceStatus.FAILED;
        }

        if (hasLegData) {
            log.warn("Unknown Exotel call status '{}', defaulting to FAILED", callStatus);
            return VoiceStatus.FAILED;
        }
        log.warn("Unknown Exotel call status '{}' with no leg data, defaulting to IN_PROGRESS", callStatus);
        return VoiceStatus.IN_PROGRESS;
    }

    private String normalizeLegStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String s = status.toLowerCase().trim();
        if ("no_answer".equals(s)) {
            return "no-answer";
        }
        return s;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date value '{}': {}", value, e.getMessage());
            log.debug("Date parse exception", e);
            return null;
        }
    }

    private Map<String, String> extractEntityIds(String customField) {
        Map<String, String> result = new HashMap<>();
        result.put("campaignId", null);
        result.put("leadId", null);
        result.put("advisorId", null);

        if (customField == null || customField.isBlank()) {
            return result;
        }

        String trimmed = customField.trim();
        if ("N/A".equalsIgnoreCase(trimmed)) {
            log.debug("Custom field is 'N/A', ignoring");
            return result;
        }

        try {
            String decoded = trimmed;
            if (decoded.startsWith("\"") && decoded.endsWith("\"") && decoded.length() >= 2) {
                decoded = decoded.substring(1, decoded.length() - 1);
            }
            decoded = decoded.replace("\\\"", "\"");

            if (decoded.isBlank() || "null".equalsIgnoreCase(decoded)) {
                return result;
            }

            JsonNode node = objectMapper.readTree(decoded);
            if (node.isTextual()) {
                String inner = node.asText();
                if (inner != null && !inner.isBlank()) {
                    node = objectMapper.readTree(inner);
                }
            }

            if (node.isObject()) {
                String entityType = node.path("entityType").asText(null);
                String identifier = node.path("identifier").asText(null);

                if (entityType != null && identifier != null) {
                    switch (entityType) {
                        case "CAMPAIGN" -> result.put("campaignId", identifier);
                        case "LEAD" -> result.put("leadId", identifier);
                        case "ADVISOR" -> result.put("advisorId", identifier);
                        default -> { }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse custom_field '{}': {}", customField, e.getMessage());
            log.debug("Custom field parse exception", e);
        }

        return result;
    }

    private VoiceStatus getStatus(String status) {
        if (status == null) {
            return VoiceStatus.IN_PROGRESS;
        }
        return switch (status.toLowerCase()) {
            case "queued" -> VoiceStatus.QUEUED;
            case "in-progress", "ringing" -> VoiceStatus.IN_PROGRESS;
            case "completed", "answered" -> VoiceStatus.COMPLETED;
            case "busy" -> VoiceStatus.BUSY;
            case "failed" -> VoiceStatus.FAILED;
            case "no-answer" -> VoiceStatus.NO_ANSWER;
            default -> VoiceStatus.IN_PROGRESS;
        };
    }
}
