package com.nivasafinance.services.voice.provider.exotel;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.nivasafinance.integrations.framework.core.utils.XMLUtils;
import com.nivasafinance.services.voice.dto.*;
import com.nivasafinance.services.voice.provider.VoiceProvider;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelAddContactsCSVToListResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelCallResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelConfiguration;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelCreateCampaignRequest;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelCreateCampaignResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelCSVUploadStatusResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelGetCampaignResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.core.io.ByteArrayResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
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
                   secretMap.get("apiKey").toString(),
                   secretMap.get("apiToken").toString()
           );
        }

        return new ExotelConfiguration(
                map.get("accountSid"),
                map.get("baseUrl"),
                map.get("callWebhookUrl"),
                map.get("campaignWebhookUrl"),
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
        IntegrationRestRequest<MultiValueMap<String, String>> restRequest =
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

    @Override
    public VoiceCallResponse getCallStatus(
            String callSid,
            ThirdPartyConfig config,
            BusinessContext businessContext) {
        setupConfiguration(config.getConfigurations());
        throw new UnsupportedOperationException("Not yet implemented");
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

    private IntegrationRestRequest<MultiValueMap<String, String>> buildConnectCallRequest(
            VoiceCallRequest request,
            ExotelConfiguration exotelConfig,
            ThirdPartyConfig config,
            BusinessContext businessContext
    ) {
        String url = String.format(
                "%s/v1/Accounts/%s/Calls/connect",
                exotelConfig.getBaseUrl(),
                exotelConfig.getAccountSid());

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("From", request.getFromNumber());
        payload.add("To", request.getToNumber());
        payload.add("CallerId", request.getCallerId());
        payload.add("Record", "true");
        try {
            payload.add("CustomField", objectMapper.writeValueAsString(request.getCallBackData()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        payload.add("StatusCallback", exotelConfig.getCallWebhookUrl());
        payload.add("StatusCallbackEvents[0]", "terminal");
        payload.add("StatusCallbackContentType", "application/json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, buildAuthorizationHeader(exotelConfig));

        ApiContext apiContext = new ApiContext(
                getKey().getProvideName(),
                config.getId()
        );

        IntegrationRestRequest<MultiValueMap<String, String>> restRequest = new IntegrationRestRequest<>();
        restRequest.setUrl(url);
        restRequest.setMethod(HttpMethod.POST);
        restRequest.setBusinessContext(businessContext);
        restRequest.setApiContext(apiContext);
        restRequest.setQueryParams(new LinkedMultiValueMap<>());
        restRequest.setRequestBody(payload);
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
            case "cancelled", "canceled" -> VoiceCampaignStatus.CANCELLED;
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
                .onStatus(Arrays.asList("busy", "no-answer", "failed"))
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
        
        // Build campaign object
        ExotelCreateCampaignRequest.Campaign campaign = ExotelCreateCampaignRequest.Campaign.builder()
                .name(request.getName())
                .type("trans")
                .campaignType("static")
                .url(flowUrl)
                .callerId(request.getCallerId())
                .lists(lists)
                .statusCallback(exotelConfig.getCampaignWebhookUrl())
                .callDuplicateNumbers(false)
                .mode("custom")
                .throttle(request.getCpm())
                .retries(retries)
                .customField(customField)
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

        ExotelCallResponse exotelCallResponse =
                XMLUtils.xmlToClassObject(responseBody, ExotelCallResponse.class);

        if (exotelCallResponse == null || exotelCallResponse.getCall() == null) {
            throw new NavigatorIntegrationClientException("Missing call details in Exotel response");
        }

        ExotelCallResponse.Call callDetails = exotelCallResponse.getCall();
        if (callDetails.getSid() == null || callDetails.getSid().isBlank()) {
            throw new NavigatorIntegrationClientException("Missing Sid in Exotel response");
        }

        return new VoiceCallResponse(callDetails.getSid(), getStatus(callDetails.getStatus()));
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
