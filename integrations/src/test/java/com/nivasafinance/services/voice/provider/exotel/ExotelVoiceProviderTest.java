package com.nivasafinance.services.voice.provider.exotel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.awssecretmanager.service.SecretManagerService;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.core.NavigatorRestService;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponse;
import com.nivasafinance.integrations.framework.core.data.IntegrationResponseStatus;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationClientException;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationServerException;
import com.nivasafinance.services.voice.dto.*;
import com.nivasafinance.services.voice.provider.exotel.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExotelVoiceProviderTest {

    @Mock
    private NavigatorRestService restService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecretManagerService secretManagerService;

    @InjectMocks
    private ExotelVoiceProvider provider;

    private ThirdPartyConfig config;
    private BusinessContext businessContext;
    private Map<String, String> configMap;

    @BeforeEach
    void setUp() {
        configMap = new HashMap<>();
        configMap.put("accountSid", "ACC_SID");
        configMap.put("baseUrl", "https://api.exotel.com");
        configMap.put("callWebhookUrl", "https://webhook.example.com/call");
        configMap.put("campaignWebhookUrl", "https://webhook.example.com/campaign");
        configMap.put("campaignCallWebhookUrl", "https://webhook.example.com/campaign-call");
        configMap.put("apiKey", "API_KEY");
        configMap.put("apiToken", "API_TOKEN");

        config = new ThirdPartyConfig(1L, "primary", "exotel", configMap);
        businessContext = new BusinessContext("LEAD", 1L, "VOICE_CALL");
    }

    // ── getKey ──────────────────────────────────────────────────────

    @Test
    void getKey_always_returnsExotel() {
        assertEquals(ThirdPartyProviderList.EXOTEL, provider.getKey(),
                "Provider key should be EXOTEL");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  setupConfiguration
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void setupConfiguration_withNullMap_throwsServerException() {
        assertThrows(NavigatorIntegrationServerException.class,
                () -> provider.setupConfiguration(null),
                "Null config map should throw NavigatorIntegrationServerException");
    }

    @Test
    void setupConfiguration_withEmptyMap_throwsServerException() {
        assertThrows(NavigatorIntegrationServerException.class,
                () -> provider.setupConfiguration(Collections.emptyMap()),
                "Empty config map should throw NavigatorIntegrationServerException");
    }

    @Test
    void setupConfiguration_withSecretKey_usesSecretManagerService() {
        Map<String, String> mapWithSecret = Map.of("secret_key", "my-secret");
        Map<String, Object> secretMap = Map.of(
                "accountSid", "SECRET_SID",
                "baseUrl", "https://secret-api.exotel.com",
                "callWebhookUrl", "https://secret-webhook/call",
                "campaignWebhookUrl", "https://secret-webhook/campaign",
                "campaignCallWebhookUrl", "https://secret-webhook/campaign-call",
                "apiKey", "SECRET_KEY",
                "apiToken", "SECRET_TOKEN"
        );
        when(secretManagerService.getSecret("my-secret")).thenReturn(secretMap);

        ExotelConfiguration result = provider.setupConfiguration(mapWithSecret);

        assertEquals("SECRET_SID", result.getAccountSid(),
                "AccountSid should come from secret manager");
        assertEquals("SECRET_KEY", result.getApiKey(),
                "ApiKey should come from secret manager");
        verify(secretManagerService).getSecret("my-secret");
    }

    @Test
    void setupConfiguration_withoutSecretKey_usesDirectMapValues() {
        ExotelConfiguration result = provider.setupConfiguration(configMap);

        assertEquals("ACC_SID", result.getAccountSid(),
                "AccountSid should come from the config map directly");
        assertEquals("API_KEY", result.getApiKey(),
                "ApiKey should come from the config map directly");
        assertEquals("https://api.exotel.com", result.getBaseUrl(),
                "BaseUrl should come from the config map directly");
        verifyNoInteractions(secretManagerService);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  makeCall: validation
    // ═══════════════════════════════════════════════════════════════════

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void makeCall_withInvalidFromNumber_throwsClientException(String fromNumber) {
        VoiceCallRequest request = new VoiceCallRequest(fromNumber, "9876543210", "CID", null);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.makeCall(request, config, businessContext),
                "Null, empty, or blank fromNumber should throw NavigatorIntegrationClientException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void makeCall_withInvalidToNumber_throwsClientException(String toNumber) {
        VoiceCallRequest request = new VoiceCallRequest("1234567890", toNumber, "CID", null);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.makeCall(request, config, businessContext),
                "Null, empty, or blank toNumber should throw NavigatorIntegrationClientException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void makeCall_withInvalidCallerId_throwsClientException(String callerId) {
        VoiceCallRequest request = new VoiceCallRequest("1234567890", "9876543210", callerId, null);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.makeCall(request, config, businessContext),
                "Null, empty, or blank callerId should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  makeCall: success and failure
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void makeCall_whenApiReturnsSuccess_returnsVoiceCallResponse() throws JsonProcessingException {
        VoiceCallRequest request = new VoiceCallRequest("1234567890", "9876543210", "CID", null);

        when(objectMapper.writeValueAsString(isNull())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(ExotelV3CallRequest.class))).thenReturn("{}");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK,
                "{\"response\":{\"call_details\":{\"sid\":\"CALL-001\",\"status\":\"queued\"}}}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelV3CallResponse exotelResponse = ExotelV3CallResponse.builder()
                .response(ExotelV3CallResponse.InnerResponse.builder()
                        .callDetails(ExotelV3CallResponse.CallDetails.builder()
                                .sid("CALL-001").status("queued").build())
                        .build())
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelV3CallResponse.class))).thenReturn(exotelResponse);

        VoiceCallResponse result = provider.makeCall(request, config, businessContext);

        assertEquals("CALL-001", result.getCallId(),
                "Call ID should match the Exotel response sid");
        assertEquals(VoiceStatus.QUEUED, result.getStatus(),
                "Status should be QUEUED for Exotel 'queued' status");
    }

    @Test
    void makeCall_whenApiReturnsFailure_throwsClientException() throws JsonProcessingException {
        VoiceCallRequest request = new VoiceCallRequest("1234567890", "9876543210", "CID", null);

        when(objectMapper.writeValueAsString(isNull())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(ExotelV3CallRequest.class))).thenReturn("{}");

        IntegrationResponse failureResponse = new IntegrationResponse(
                IntegrationResponseStatus.CLIENT_ERROR, HttpStatus.BAD_REQUEST, "error body", "Call failed");
        when(restService.doRestRequest(any())).thenReturn(failureResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.makeCall(request, config, businessContext),
                "Failed API call should throw NavigatorIntegrationClientException");
    }

    @Test
    void makeCall_whenResponseBodyEmpty_throwsClientException() throws JsonProcessingException {
        VoiceCallRequest request = new VoiceCallRequest("1234567890", "9876543210", "CID", null);

        when(objectMapper.writeValueAsString(isNull())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(ExotelV3CallRequest.class))).thenReturn("{}");

        IntegrationResponse emptyResponse = IntegrationResponse.successResponse(HttpStatus.OK, "");
        when(restService.doRestRequest(any())).thenReturn(emptyResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.makeCall(request, config, businessContext),
                "Empty response body should throw NavigatorIntegrationClientException");
    }

    @Test
    void makeCall_whenCallDetailsNull_throwsClientException() throws JsonProcessingException {
        VoiceCallRequest request = new VoiceCallRequest("1234567890", "9876543210", "CID", null);

        when(objectMapper.writeValueAsString(isNull())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(ExotelV3CallRequest.class))).thenReturn("{}");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":null}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelV3CallResponse exotelResponse = ExotelV3CallResponse.builder().response(null).build();
        when(objectMapper.readValue(anyString(), eq(ExotelV3CallResponse.class))).thenReturn(exotelResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.makeCall(request, config, businessContext),
                "Null call details should throw NavigatorIntegrationClientException");
    }

    @Test
    void makeCall_whenSidMissing_throwsClientException() throws JsonProcessingException {
        VoiceCallRequest request = new VoiceCallRequest("1234567890", "9876543210", "CID", null);

        when(objectMapper.writeValueAsString(isNull())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(ExotelV3CallRequest.class))).thenReturn("{}");

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":{}}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelV3CallResponse exotelResponse = ExotelV3CallResponse.builder()
                .response(ExotelV3CallResponse.InnerResponse.builder()
                        .callDetails(ExotelV3CallResponse.CallDetails.builder().sid(null).build())
                        .build())
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelV3CallResponse.class))).thenReturn(exotelResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.makeCall(request, config, businessContext),
                "Missing SID in call details should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCallStatus: validation
    // ═══════════════════════════════════════════════════════════════════

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void getCallStatus_withInvalidCallSid_throwsClientException(String callSid) {
        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.getCallStatus(callSid, config, businessContext),
                "Null, empty, or blank callSid should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCallStatus: success
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getCallStatus_whenApiReturnsSuccess_returnsCallStatusResponse() throws JsonProcessingException {
        IntegrationResponse callSuccessResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"call_details\":{}}");
        IntegrationResponse legsSuccessResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"leg_details\":{}}");

        when(restService.doRestRequest(any()))
                .thenReturn(callSuccessResponse)
                .thenReturn(legsSuccessResponse);

        ExotelGetCallStatusResponse callStatusResp = ExotelGetCallStatusResponse.builder()
                .response(ExotelGetCallStatusResponse.Response.builder()
                        .callDetails(ExotelGetCallStatusResponse.CallDetails.builder()
                                .sid("CALL-001").status("completed").build())
                        .build())
                .build();
        when(objectMapper.readValue(eq("{\"call_details\":{}}"), eq(ExotelGetCallStatusResponse.class)))
                .thenReturn(callStatusResp);

        ExotelGetCallLegsResponse legsResp = ExotelGetCallLegsResponse.builder()
                .response(ExotelGetCallLegsResponse.Response.builder()
                        .legDetails(ExotelGetCallLegsResponse.LegDetails.builder()
                                .from(List.of(ExotelGetCallLegsResponse.Leg.builder()
                                        .status("completed").contactUri("111").build()))
                                .to(List.of(ExotelGetCallLegsResponse.Leg.builder()
                                        .status("completed").contactUri("222").build()))
                                .build())
                        .build())
                .build();
        when(objectMapper.readValue(eq("{\"leg_details\":{}}"), eq(ExotelGetCallLegsResponse.class)))
                .thenReturn(legsResp);

        VoiceGetCallStatusResponse result = provider.getCallStatus("CALL-001", config, businessContext);

        assertEquals("CALL-001", result.getCallId(),
                "Call ID should match the Exotel response sid");
        assertEquals(VoiceStatus.COMPLETED, result.getStatus(),
                "Status should be COMPLETED when both legs are completed");
    }

    @Test
    void getCallStatus_whenCallDetailsNull_throwsClientException() throws JsonProcessingException {
        IntegrationResponse callSuccessResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{}");
        when(restService.doRestRequest(any())).thenReturn(callSuccessResponse);

        ExotelGetCallStatusResponse callStatusResp = ExotelGetCallStatusResponse.builder().response(null).build();
        when(objectMapper.readValue(eq("{}"), eq(ExotelGetCallStatusResponse.class))).thenReturn(callStatusResp);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.getCallStatus("CALL-001", config, businessContext),
                "Null call details should throw NavigatorIntegrationClientException");
    }

    @Test
    void getCallStatus_whenLegsApiFails_returnsResponseWithNullLegs() throws JsonProcessingException {
        IntegrationResponse callSuccessResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"call\":{}}");
        IntegrationResponse legsFailResponse = IntegrationResponse.clientErrorResponse("legs error");

        when(restService.doRestRequest(any()))
                .thenReturn(callSuccessResponse)
                .thenReturn(legsFailResponse)
                .thenReturn(legsFailResponse)
                .thenReturn(legsFailResponse)
                .thenReturn(legsFailResponse)
                .thenReturn(legsFailResponse)
                .thenReturn(legsFailResponse);

        ExotelGetCallStatusResponse callStatusResp = ExotelGetCallStatusResponse.builder()
                .response(ExotelGetCallStatusResponse.Response.builder()
                        .callDetails(ExotelGetCallStatusResponse.CallDetails.builder()
                                .sid("CALL-001").status("completed").build())
                        .build())
                .build();
        when(objectMapper.readValue(eq("{\"call\":{}}"), eq(ExotelGetCallStatusResponse.class)))
                .thenReturn(callStatusResp);

        VoiceGetCallStatusResponse result = provider.getCallStatus("CALL-001", config, businessContext);

        assertEquals("CALL-001", result.getCallId(),
                "Call ID should still be populated even when legs API fails");
        assertNull(result.getFrom(),
                "From leg should be null when legs API fails");
        assertNull(result.getTo(),
                "To leg should be null when legs API fails");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  uploadCSVList: validation
    // ═══════════════════════════════════════════════════════════════════

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void uploadCSVList_withInvalidName_throwsClientException(String name) {
        VoiceCreateListRequest request = VoiceCreateListRequest.builder()
                .name(name)
                .file(new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8)))
                .build();

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.uploadCSVList(request, config, businessContext),
                "Null, empty, or blank name should throw NavigatorIntegrationClientException");
    }

    @Test
    void uploadCSVList_withNullFile_throwsClientException() {
        VoiceCreateListRequest request = VoiceCreateListRequest.builder()
                .name("test-list").file(null).build();

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.uploadCSVList(request, config, businessContext),
                "Null file should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  uploadCSVList: success and failure
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void uploadCSVList_whenApiReturnsSuccess_returnsCreateListResponse() throws JsonProcessingException {
        VoiceCreateListRequest request = VoiceCreateListRequest.builder()
                .name("test-list")
                .file(new ByteArrayInputStream("name,phone\nTest,123".getBytes(StandardCharsets.UTF_8)))
                .build();

        IntegrationResponse uploadResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":{}}");
        when(restService.doRestRequest(any())).thenReturn(uploadResponse);

        ExotelAddContactsCSVToListResponse exotelResponse = ExotelAddContactsCSVToListResponse.builder()
                .response(ExotelAddContactsCSVToListResponse.Response.builder()
                        .data(ExotelAddContactsCSVToListResponse.ResponseData.builder()
                                .summary(ExotelAddContactsCSVToListResponse.Summary.builder()
                                        .uploadId("UP-001").listSid("LIST-001").build())
                                .build())
                        .build())
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelAddContactsCSVToListResponse.class)))
                .thenReturn(exotelResponse);

        VoiceCreateListResponse result = provider.uploadCSVList(request, config, businessContext);

        assertEquals("LIST-001", result.getListId(),
                "List ID should come from the Exotel response");
        assertEquals("UP-001", result.getRequestId(),
                "Request ID should come from the Exotel upload response");
    }

    @Test
    void uploadCSVList_whenApiReturnsFailure_throwsClientException() {
        VoiceCreateListRequest request = VoiceCreateListRequest.builder()
                .name("test-list")
                .file(new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8)))
                .build();

        IntegrationResponse failureResponse = new IntegrationResponse(
                IntegrationResponseStatus.CLIENT_ERROR, HttpStatus.BAD_REQUEST, "error", "Upload failed");
        when(restService.doRestRequest(any())).thenReturn(failureResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.uploadCSVList(request, config, businessContext),
                "Failed CSV upload should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCSVUploadStatus: validation
    // ═══════════════════════════════════════════════════════════════════

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void getCSVUploadStatus_withInvalidRequestId_throwsClientException(String requestId) {
        VoiceCSVUploadStatusRequest request = VoiceCSVUploadStatusRequest.builder()
                .requestId(requestId).build();

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.getCSVUploadStatus(request, config, businessContext),
                "Null, empty, or blank requestId should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCSVUploadStatus: success and failure
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getCSVUploadStatus_whenApiReturnsCompleted_returnsCompletedStatus() throws JsonProcessingException {
        VoiceCSVUploadStatusRequest request = VoiceCSVUploadStatusRequest.builder()
                .requestId("REQ-001").build();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":{}}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelCSVUploadStatusResponse exotelResponse = ExotelCSVUploadStatusResponse.builder()
                .response(ExotelCSVUploadStatusResponse.Response.builder()
                        .data(ExotelCSVUploadStatusResponse.ResponseData.builder()
                                .status("completed").build())
                        .build())
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelCSVUploadStatusResponse.class)))
                .thenReturn(exotelResponse);

        VoiceCSVUploadStatusResponse result = provider.getCSVUploadStatus(request, config, businessContext);

        assertEquals(VoiceCSVUploadStatus.COMPLETED, result.getStatus(),
                "Status should be COMPLETED when Exotel returns 'completed'");
    }

    @Test
    void getCSVUploadStatus_whenApiReturnsFailed_returnsFailedStatus() throws JsonProcessingException {
        VoiceCSVUploadStatusRequest request = VoiceCSVUploadStatusRequest.builder()
                .requestId("REQ-001").build();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":{}}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelCSVUploadStatusResponse exotelResponse = ExotelCSVUploadStatusResponse.builder()
                .response(ExotelCSVUploadStatusResponse.Response.builder()
                        .data(ExotelCSVUploadStatusResponse.ResponseData.builder()
                                .status("failed").build())
                        .build())
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelCSVUploadStatusResponse.class)))
                .thenReturn(exotelResponse);

        VoiceCSVUploadStatusResponse result = provider.getCSVUploadStatus(request, config, businessContext);

        assertEquals(VoiceCSVUploadStatus.FAILED, result.getStatus(),
                "Status should be FAILED when Exotel returns 'failed'");
    }

    @Test
    void getCSVUploadStatus_whenApiReturnsNullStatus_returnsInProgressStatus() throws JsonProcessingException {
        VoiceCSVUploadStatusRequest request = VoiceCSVUploadStatusRequest.builder()
                .requestId("REQ-001").build();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":{}}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelCSVUploadStatusResponse exotelResponse = ExotelCSVUploadStatusResponse.builder()
                .response(ExotelCSVUploadStatusResponse.Response.builder()
                        .data(ExotelCSVUploadStatusResponse.ResponseData.builder()
                                .status(null).build())
                        .build())
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelCSVUploadStatusResponse.class)))
                .thenReturn(exotelResponse);

        VoiceCSVUploadStatusResponse result = provider.getCSVUploadStatus(request, config, businessContext);

        assertEquals(VoiceCSVUploadStatus.IN_PROGRESS, result.getStatus(),
                "Null status from Exotel should default to IN_PROGRESS");
    }

    @Test
    void getCSVUploadStatus_whenApiReturnsFailure_throwsClientException() {
        VoiceCSVUploadStatusRequest request = VoiceCSVUploadStatusRequest.builder()
                .requestId("REQ-001").build();

        IntegrationResponse failureResponse = new IntegrationResponse(
                IntegrationResponseStatus.CLIENT_ERROR, HttpStatus.BAD_REQUEST, "error", "Status check failed");
        when(restService.doRestRequest(any())).thenReturn(failureResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.getCSVUploadStatus(request, config, businessContext),
                "Failed status check should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  createCampaign: validation
    // ═══════════════════════════════════════════════════════════════════

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createCampaign_withInvalidName_throwsClientException(String name) {
        VoiceCampaignRequest request = new VoiceCampaignRequest();
        request.setName(name);
        request.setListId("LIST-001");
        request.setCallerId("CID");
        request.setCallFlowId("FLOW-001");

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.createCampaign(request, config, businessContext),
                "Null, empty, or blank campaign name should throw NavigatorIntegrationClientException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createCampaign_withInvalidListId_throwsClientException(String listId) {
        VoiceCampaignRequest request = new VoiceCampaignRequest();
        request.setName("campaign");
        request.setListId(listId);
        request.setCallerId("CID");
        request.setCallFlowId("FLOW-001");

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.createCampaign(request, config, businessContext),
                "Null, empty, or blank listId should throw NavigatorIntegrationClientException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createCampaign_withInvalidCallerId_throwsClientException(String callerId) {
        VoiceCampaignRequest request = new VoiceCampaignRequest();
        request.setName("campaign");
        request.setListId("LIST-001");
        request.setCallerId(callerId);
        request.setCallFlowId("FLOW-001");

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.createCampaign(request, config, businessContext),
                "Null, empty, or blank callerId should throw NavigatorIntegrationClientException");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createCampaign_withInvalidCallFlowId_throwsClientException(String callFlowId) {
        VoiceCampaignRequest request = new VoiceCampaignRequest();
        request.setName("campaign");
        request.setListId("LIST-001");
        request.setCallerId("CID");
        request.setCallFlowId(callFlowId);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.createCampaign(request, config, businessContext),
                "Null, empty, or blank callFlowId should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  createCampaign: success and failure
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void createCampaign_whenApiReturnsSuccess_returnsCampaignResponse() throws JsonProcessingException {
        VoiceCampaignRequest request = buildValidCampaignRequest();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":[]}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelCreateCampaignResponse exotelResponse = ExotelCreateCampaignResponse.builder()
                .response(List.of(ExotelCreateCampaignResponse.ResponseItem.builder()
                        .data(ExotelCreateCampaignResponse.ResponseData.builder()
                                .id("CAMP-001").build())
                        .build()))
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelCreateCampaignResponse.class)))
                .thenReturn(exotelResponse);

        VoiceCampaignResponse result = provider.createCampaign(request, config, businessContext);

        assertEquals("CAMP-001", result.getCampaignId(),
                "Campaign ID should come from the Exotel response");
        assertEquals(ThirdPartyProviderList.EXOTEL.getProvideName(), result.getProviderKey(),
                "Provider key should be exotel");
    }

    @Test
    void createCampaign_whenApiReturnsFailure_throwsClientException() throws JsonProcessingException {
        VoiceCampaignRequest request = buildValidCampaignRequest();

        IntegrationResponse failureResponse = new IntegrationResponse(
                IntegrationResponseStatus.CLIENT_ERROR, HttpStatus.BAD_REQUEST, "error", "Campaign creation failed");
        when(restService.doRestRequest(any())).thenReturn(failureResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.createCampaign(request, config, businessContext),
                "Failed campaign creation should throw NavigatorIntegrationClientException");
    }

    @Test
    void createCampaign_whenCampaignIdNull_throwsClientException() throws JsonProcessingException {
        VoiceCampaignRequest request = buildValidCampaignRequest();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":[]}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelCreateCampaignResponse exotelResponse = ExotelCreateCampaignResponse.builder()
                .response(List.of(ExotelCreateCampaignResponse.ResponseItem.builder()
                        .data(ExotelCreateCampaignResponse.ResponseData.builder()
                                .id(null).build())
                        .build()))
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelCreateCampaignResponse.class)))
                .thenReturn(exotelResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.createCampaign(request, config, businessContext),
                "Null campaign ID in response should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCampaignDetails: validation
    // ═══════════════════════════════════════════════════════════════════

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void getCampaignDetails_withInvalidCampaignId_throwsClientException(String campaignId) {
        VoiceGetCampaignDetailsRequest request = VoiceGetCampaignDetailsRequest.builder()
                .campaignId(campaignId).build();

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.getCampaignDetails(request, config, businessContext),
                "Null, empty, or blank campaignId should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCampaignDetails: success and failure
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getCampaignDetails_whenApiReturnsSuccess_returnsCampaignResponse() throws JsonProcessingException {
        VoiceGetCampaignDetailsRequest request = VoiceGetCampaignDetailsRequest.builder()
                .campaignId("CAMP-001").build();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":[]}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelGetCampaignResponse exotelResponse = ExotelGetCampaignResponse.builder()
                .response(List.of(ExotelGetCampaignResponse.ResponseItem.builder()
                        .data(ExotelGetCampaignResponse.CampaignData.builder()
                                .id("CAMP-001").status("completed").reportUrl("https://report.url").build())
                        .summary(ExotelGetCampaignResponse.Summary.builder()
                                .callScheduled(100L).callCompleted(80L).callFailed(10L)
                                .callInitialized(5L).callInProgress(5L).build())
                        .build()))
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelGetCampaignResponse.class)))
                .thenReturn(exotelResponse);

        VoiceCampaignResponse result = provider.getCampaignDetails(request, config, businessContext);

        assertEquals("CAMP-001", result.getCampaignId(),
                "Campaign ID should match the Exotel response");
        assertEquals(VoiceCampaignStatus.COMPLETED, result.getStatus(),
                "Status should be COMPLETED for Exotel 'completed' status");
        assertEquals("https://report.url", result.getReportUrl(),
                "Report URL should come from Exotel response");
        assertNotNull(result.getSummary(),
                "Summary should be populated from Exotel response");
        assertEquals(80L, result.getSummary().getCompleted(),
                "Completed count should match Exotel summary");
    }

    @Test
    void getCampaignDetails_whenCampaignDataNull_throwsClientException() throws JsonProcessingException {
        VoiceGetCampaignDetailsRequest request = VoiceGetCampaignDetailsRequest.builder()
                .campaignId("CAMP-001").build();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelGetCampaignResponse exotelResponse = ExotelGetCampaignResponse.builder()
                .response(null).build();
        when(objectMapper.readValue(anyString(), eq(ExotelGetCampaignResponse.class)))
                .thenReturn(exotelResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.getCampaignDetails(request, config, businessContext),
                "Null campaign data should throw NavigatorIntegrationClientException");
    }

    @Test
    void getCampaignDetails_whenSummaryNull_returnsResponseWithNullSummary() throws JsonProcessingException {
        VoiceGetCampaignDetailsRequest request = VoiceGetCampaignDetailsRequest.builder()
                .campaignId("CAMP-001").build();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":[]}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelGetCampaignResponse exotelResponse = ExotelGetCampaignResponse.builder()
                .response(List.of(ExotelGetCampaignResponse.ResponseItem.builder()
                        .data(ExotelGetCampaignResponse.CampaignData.builder()
                                .id("CAMP-001").status("created").build())
                        .summary(null)
                        .build()))
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelGetCampaignResponse.class)))
                .thenReturn(exotelResponse);

        VoiceCampaignResponse result = provider.getCampaignDetails(request, config, businessContext);

        assertEquals("CAMP-001", result.getCampaignId(),
                "Campaign ID should still be populated when summary is null");
        assertNull(result.getSummary(),
                "Summary should be null when Exotel summary is null");
    }

    @Test
    void getCampaignDetails_whenApiReturnsFailure_throwsClientException() {
        VoiceGetCampaignDetailsRequest request = VoiceGetCampaignDetailsRequest.builder()
                .campaignId("CAMP-001").build();

        IntegrationResponse failureResponse = new IntegrationResponse(
                IntegrationResponseStatus.CLIENT_ERROR, HttpStatus.BAD_REQUEST, "error", "Get campaign failed");
        when(restService.doRestRequest(any())).thenReturn(failureResponse);

        assertThrows(NavigatorIntegrationClientException.class,
                () -> provider.getCampaignDetails(request, config, businessContext),
                "Failed campaign details fetch should throw NavigatorIntegrationClientException");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCampaignDetails: campaign status mapping
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getCampaignDetails_whenStatusInProgress_mapsToCampaignInProgress() throws JsonProcessingException {
        VoiceCampaignResponse result = fetchCampaignWithStatus("in-progress");

        assertEquals(VoiceCampaignStatus.IN_PROGRESS, result.getStatus(),
                "'in-progress' should map to IN_PROGRESS");
    }

    @Test
    void getCampaignDetails_whenStatusFailed_mapsToCampaignFailed() throws JsonProcessingException {
        VoiceCampaignResponse result = fetchCampaignWithStatus("failed");

        assertEquals(VoiceCampaignStatus.FAILED, result.getStatus(),
                "'failed' should map to FAILED");
    }

    @Test
    void getCampaignDetails_whenStatusCancelled_mapsToCampaignCancelled() throws JsonProcessingException {
        VoiceCampaignResponse result = fetchCampaignWithStatus("cancelled");

        assertEquals(VoiceCampaignStatus.CANCELLED, result.getStatus(),
                "'cancelled' should map to CANCELLED");
    }

    @Test
    void getCampaignDetails_whenStatusPaused_mapsToCampaignPaused() throws JsonProcessingException {
        VoiceCampaignResponse result = fetchCampaignWithStatus("paused");

        assertEquals(VoiceCampaignStatus.PAUSED, result.getStatus(),
                "'paused' should map to PAUSED");
    }

    @Test
    void getCampaignDetails_whenStatusNull_defaultsToCreated() throws JsonProcessingException {
        VoiceCampaignResponse result = fetchCampaignWithStatus(null);

        assertEquals(VoiceCampaignStatus.CREATED, result.getStatus(),
                "Null status should default to CREATED");
    }

    @Test
    void getCampaignDetails_whenStatusUnknown_defaultsToCreated() throws JsonProcessingException {
        VoiceCampaignResponse result = fetchCampaignWithStatus("something_unknown");

        assertEquals(VoiceCampaignStatus.CREATED, result.getStatus(),
                "Unknown status should default to CREATED");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    private VoiceCampaignRequest buildValidCampaignRequest() {
        VoiceCampaignRequest request = new VoiceCampaignRequest();
        request.setName("test-campaign");
        request.setListId("LIST-001");
        request.setCallerId("CID");
        request.setCallFlowId("FLOW-001");
        request.setNoOfRetries(2);
        request.setRetryIntervalMins(5);
        request.setCpm(10);
        return request;
    }

    private VoiceCampaignResponse fetchCampaignWithStatus(String status) throws JsonProcessingException {
        VoiceGetCampaignDetailsRequest request = VoiceGetCampaignDetailsRequest.builder()
                .campaignId("CAMP-001").build();

        IntegrationResponse successResponse = IntegrationResponse.successResponse(HttpStatus.OK, "{\"response\":[]}");
        when(restService.doRestRequest(any())).thenReturn(successResponse);

        ExotelGetCampaignResponse exotelResponse = ExotelGetCampaignResponse.builder()
                .response(List.of(ExotelGetCampaignResponse.ResponseItem.builder()
                        .data(ExotelGetCampaignResponse.CampaignData.builder()
                                .id("CAMP-001").status(status).build())
                        .build()))
                .build();
        when(objectMapper.readValue(anyString(), eq(ExotelGetCampaignResponse.class)))
                .thenReturn(exotelResponse);

        return provider.getCampaignDetails(request, config, businessContext);
    }
}
