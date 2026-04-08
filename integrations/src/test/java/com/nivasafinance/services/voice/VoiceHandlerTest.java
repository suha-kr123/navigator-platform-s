package com.nivasafinance.services.voice;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyProviderList;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.data.RunConfig;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.services.voice.dto.*;
import com.nivasafinance.services.voice.provider.VoiceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoiceHandlerTest {

    private static final String PROVIDER_NAME = "exotel";

    private VoiceProvider primaryProvider;
    private VoiceHandler handler;
    private RunConfig runConfig;
    private ThirdPartyConfig primaryConfig;
    private BusinessContext businessContext;

    @BeforeEach
    void setUp() {
        primaryProvider = mock(VoiceProvider.class);
        when(primaryProvider.getKey()).thenReturn(ThirdPartyProviderList.EXOTEL);

        handler = new VoiceHandler(Set.of(primaryProvider));

        primaryConfig = new ThirdPartyConfig(1L, "primary", PROVIDER_NAME, Map.of());
        runConfig = new RunConfig(primaryConfig, null, 1);
        ReflectionTestUtils.setField(handler, "runConfig", runConfig);

        businessContext = new BusinessContext("LEAD", 1L, "VOICE_CALL");
    }

    // ── getKey ──────────────────────────────────────────────────────

    @Test
    void getKey_always_returnsVoice() {
        assertEquals(ThirdPartyServiceList.VOICE, handler.getKey(),
                "Handler key should be VOICE");
    }

    // ── constructor ─────────────────────────────────────────────────

    @Test
    void constructor_withProviders_resolvesProviderByName() {
        VoiceCallResponse expectedResponse = new VoiceCallResponse("CALL-001", VoiceStatus.QUEUED);
        when(primaryProvider.makeCall(
                any(VoiceCallRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCallRequest request = new VoiceCallRequest("1234567890", "0987654321", "CALLER_ID", null);

        VoiceCallResponse result = handler.makeCall(request, businessContext);

        assertEquals(expectedResponse, result,
                "Provider registered during construction should be resolved by name from config");
    }

    @Test
    void constructor_withEmptyProviderSet_throwsVoiceHandlerException() {
        VoiceHandler emptyHandler = new VoiceHandler(Collections.emptySet());
        ReflectionTestUtils.setField(emptyHandler, "runConfig", runConfig);

        assertThrows(VoiceHandlerException.class,
                () -> emptyHandler.makeCall(new VoiceCallRequest(), businessContext),
                "Empty provider set should cause VoiceHandlerException on provider lookup");
    }

    // ── makeCall ────────────────────────────────────────────────────

    @Test
    void makeCall_withValidProvider_returnsResponse() {
        VoiceCallResponse expectedResponse = new VoiceCallResponse("CALL-001", VoiceStatus.IN_PROGRESS);
        when(primaryProvider.makeCall(
                any(VoiceCallRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCallRequest request = new VoiceCallRequest("1234567890", "0987654321", "CALLER_ID", null);

        VoiceCallResponse result = handler.makeCall(request, businessContext);

        assertEquals(expectedResponse, result,
                "Should return the response from the primary provider");
    }

    @Test
    void makeCall_whenPrimaryProviderNotFound_throwsVoiceHandlerException() {
        primaryConfig.setProvider("unknown_provider");

        assertThrows(VoiceHandlerException.class,
                () -> handler.makeCall(new VoiceCallRequest(), businessContext),
                "Should throw VoiceHandlerException when primary provider is not in the map");
    }

    @Test
    void makeCall_withFallbackConfigPresent_returnsResponse() {
        ThirdPartyConfig fallbackConfig = new ThirdPartyConfig(2L, "fallback", PROVIDER_NAME, Map.of());
        runConfig.setFallbackConfig(fallbackConfig);

        VoiceCallResponse expectedResponse = new VoiceCallResponse("CALL-002", VoiceStatus.QUEUED);
        when(primaryProvider.makeCall(
                any(VoiceCallRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCallResponse result = handler.makeCall(
                new VoiceCallRequest("111", "222", "CID", null), businessContext);

        assertEquals(expectedResponse, result,
                "Should return response when fallback config is also present");
    }

    @Test
    void makeCall_withNullFallbackConfig_returnsResponse() {
        runConfig.setFallbackConfig(null);

        VoiceCallResponse expectedResponse = new VoiceCallResponse("CALL-003", VoiceStatus.COMPLETED);
        when(primaryProvider.makeCall(
                any(VoiceCallRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCallResponse result = handler.makeCall(
                new VoiceCallRequest("111", "222", "CID", null), businessContext);

        assertEquals(expectedResponse, result,
                "Should succeed without fallback config when primary succeeds");
    }

    // ── getCallStatus ───────────────────────────────────────────────

    @Test
    void getCallStatus_withValidProvider_returnsResponse() {
        VoiceGetCallStatusResponse expectedResponse = VoiceGetCallStatusResponse.builder()
                .callId("CALL-001").status(VoiceStatus.COMPLETED).build();
        when(primaryProvider.getCallStatus(
                any(String.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceGetCallStatusResponse result = handler.getCallStatus("CALL-001", businessContext);

        assertEquals(expectedResponse, result,
                "Should return the call status response from the primary provider");
    }

    @Test
    void getCallStatus_whenPrimaryProviderNotFound_throwsVoiceHandlerException() {
        primaryConfig.setProvider("unknown_provider");

        assertThrows(VoiceHandlerException.class,
                () -> handler.getCallStatus("CALL-001", businessContext),
                "Should throw VoiceHandlerException when primary provider is not in the map");
    }

    @Test
    void getCallStatus_withFallbackConfigPresent_returnsResponse() {
        ThirdPartyConfig fallbackConfig = new ThirdPartyConfig(2L, "fallback", PROVIDER_NAME, Map.of());
        runConfig.setFallbackConfig(fallbackConfig);

        VoiceGetCallStatusResponse expectedResponse = VoiceGetCallStatusResponse.builder()
                .callId("CALL-001").status(VoiceStatus.NO_ANSWER).build();
        when(primaryProvider.getCallStatus(
                any(String.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceGetCallStatusResponse result = handler.getCallStatus("CALL-001", businessContext);

        assertEquals(expectedResponse, result,
                "Should return response when fallback config is present");
    }

    // ── uploadCSVList ───────────────────────────────────────────────

    @Test
    void uploadCSVList_withValidProvider_returnsResponse() {
        VoiceCreateListResponse expectedResponse = VoiceCreateListResponse.builder()
                .listId("LIST-001").requestId("REQ-001").build();
        when(primaryProvider.uploadCSVList(
                any(VoiceCreateListRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCreateListRequest request = VoiceCreateListRequest.builder().name("test-list").build();

        VoiceCreateListResponse result = handler.uploadCSVList(request, businessContext);

        assertEquals(expectedResponse, result,
                "Should return the upload response from the primary provider");
    }

    @Test
    void uploadCSVList_whenPrimaryProviderNotFound_throwsVoiceHandlerException() {
        primaryConfig.setProvider("unknown_provider");

        assertThrows(VoiceHandlerException.class,
                () -> handler.uploadCSVList(VoiceCreateListRequest.builder().build(), businessContext),
                "Should throw VoiceHandlerException when primary provider is not in the map");
    }

    // ── getCSVUploadStatus ──────────────────────────────────────────

    @Test
    void getCSVUploadStatus_withValidProvider_returnsResponse() {
        VoiceCSVUploadStatusResponse expectedResponse = VoiceCSVUploadStatusResponse.builder()
                .status(VoiceCSVUploadStatus.COMPLETED).build();
        when(primaryProvider.getCSVUploadStatus(
                any(VoiceCSVUploadStatusRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCSVUploadStatusRequest request = VoiceCSVUploadStatusRequest.builder().requestId("REQ-001").build();

        VoiceCSVUploadStatusResponse result = handler.getCSVUploadStatus(request, businessContext);

        assertEquals(expectedResponse, result,
                "Should return the CSV upload status from the primary provider");
    }

    @Test
    void getCSVUploadStatus_whenPrimaryProviderNotFound_throwsVoiceHandlerException() {
        primaryConfig.setProvider("unknown_provider");

        assertThrows(VoiceHandlerException.class,
                () -> handler.getCSVUploadStatus(
                        VoiceCSVUploadStatusRequest.builder().build(), businessContext),
                "Should throw VoiceHandlerException when primary provider is not in the map");
    }

    // ── createCampaign ──────────────────────────────────────────────

    @Test
    void createCampaign_withValidProvider_returnsResponse() {
        VoiceCampaignResponse expectedResponse = VoiceCampaignResponse.builder()
                .campaignId("CAMP-001").providerKey(PROVIDER_NAME).build();
        when(primaryProvider.createCampaign(
                any(VoiceCampaignRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCampaignRequest request = new VoiceCampaignRequest();
        request.setName("test-campaign");
        request.setListId("LIST-001");
        request.setCallerId("CID");
        request.setCallFlowId("FLOW-001");

        VoiceCampaignResponse result = handler.createCampaign(request, businessContext);

        assertEquals(expectedResponse, result,
                "Should return the campaign response from the primary provider");
    }

    @Test
    void createCampaign_whenPrimaryProviderNotFound_throwsVoiceHandlerException() {
        primaryConfig.setProvider("unknown_provider");

        assertThrows(VoiceHandlerException.class,
                () -> handler.createCampaign(new VoiceCampaignRequest(), businessContext),
                "Should throw VoiceHandlerException when primary provider is not in the map");
    }

    @Test
    void createCampaign_withFallbackConfigPresent_returnsResponse() {
        ThirdPartyConfig fallbackConfig = new ThirdPartyConfig(2L, "fallback", PROVIDER_NAME, Map.of());
        runConfig.setFallbackConfig(fallbackConfig);

        VoiceCampaignResponse expectedResponse = VoiceCampaignResponse.builder()
                .campaignId("CAMP-002").build();
        when(primaryProvider.createCampaign(
                any(VoiceCampaignRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCampaignRequest request = new VoiceCampaignRequest();
        request.setName("camp");
        request.setListId("L1");
        request.setCallerId("C1");
        request.setCallFlowId("F1");

        VoiceCampaignResponse result = handler.createCampaign(request, businessContext);

        assertEquals(expectedResponse, result,
                "Should return response when fallback config is present");
    }

    // ── getCampaignDetails ──────────────────────────────────────────

    @Test
    void getCampaignDetails_withValidProvider_returnsResponse() {
        VoiceCampaignResponse expectedResponse = VoiceCampaignResponse.builder()
                .campaignId("CAMP-001").status(VoiceCampaignStatus.COMPLETED).build();
        when(primaryProvider.getCampaignDetails(
                any(VoiceGetCampaignDetailsRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceGetCampaignDetailsRequest request = VoiceGetCampaignDetailsRequest.builder()
                .campaignId("CAMP-001").build();

        VoiceCampaignResponse result = handler.getCampaignDetails(request, businessContext);

        assertEquals(expectedResponse, result,
                "Should return the campaign details from the primary provider");
    }

    @Test
    void getCampaignDetails_whenPrimaryProviderNotFound_throwsVoiceHandlerException() {
        primaryConfig.setProvider("unknown_provider");

        assertThrows(VoiceHandlerException.class,
                () -> handler.getCampaignDetails(
                        VoiceGetCampaignDetailsRequest.builder().build(), businessContext),
                "Should throw VoiceHandlerException when primary provider is not in the map");
    }

    @Test
    void getCampaignDetails_withFallbackConfigPresent_returnsResponse() {
        ThirdPartyConfig fallbackConfig = new ThirdPartyConfig(2L, "fallback", PROVIDER_NAME, Map.of());
        runConfig.setFallbackConfig(fallbackConfig);

        VoiceCampaignResponse expectedResponse = VoiceCampaignResponse.builder()
                .campaignId("CAMP-001").status(VoiceCampaignStatus.IN_PROGRESS)
                .summary(VoiceCampaignResponse.Summary.builder().completed(10L).failed(2L).build())
                .build();
        when(primaryProvider.getCampaignDetails(
                any(VoiceGetCampaignDetailsRequest.class), any(ThirdPartyConfig.class), any(BusinessContext.class)))
                .thenReturn(expectedResponse);

        VoiceCampaignResponse result = handler.getCampaignDetails(
                VoiceGetCampaignDetailsRequest.builder().campaignId("CAMP-001").build(), businessContext);

        assertEquals(expectedResponse, result,
                "Should return response when fallback config is present");
    }
}
