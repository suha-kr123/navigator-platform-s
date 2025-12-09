package com.nivasafinance.services.voice.provider.exotel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.NavigatorRestService;
import com.nivasafinance.integrations.framework.core.data.ApiContext;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponse;
import com.nivasafinance.integrations.framework.core.data.IntegrationRestRequest;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationClientException;
import com.nivasafinance.integrations.framework.core.utils.XMLUtils;
import com.nivasafinance.services.voice.dto.VoiceCallRequest;
import com.nivasafinance.services.voice.dto.VoiceCallResponse;
import com.nivasafinance.services.voice.dto.VoiceStatus;
import com.nivasafinance.services.voice.provider.VoiceProvider;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelCallResponse;
import com.nivasafinance.services.voice.provider.exotel.data.ExotelConfiguration;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
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

    @Override
    public ThirdPartyProviderList getKey() {
        return ThirdPartyProviderList.EXOTEL;
    }

    @Override
    public ExotelConfiguration setupConfiguration(Map<String, String> map) {
        return new ExotelConfiguration( //todo use secret manager
                map.getOrDefault("accountSid", "nivasafinance1"),
                map.getOrDefault("baseUrl", "https://api.exotel.com"),
                map.getOrDefault("webhookUrl", "https://tv5fid365h.execute-api.ap-south-1.amazonaws.com/external/exotel/callback"),
                map.getOrDefault("apiKey", "be905126ae5b7be0965af8816738053e74a7fdb80abef3d0"),
                map.getOrDefault("apiToken", "fa31c8dbafd884fc2b8c5d3e09e6260a8e66815013b53283")
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
        payload.add("StatusCallback", exotelConfig.getWebhookUrl());
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
