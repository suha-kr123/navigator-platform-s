package com.nivasafinance.externals.exotel.service.impl;

import com.nivasafinance.features.call.service.CallNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ExotelServiceImplWebhookTest {

    @Mock private com.nivasafinance.integrations.framework.ServiceFactory<com.nivasafinance.services.voice.VoiceHandler> voiceServiceFactory;
    @Mock private com.nivasafinance.features.lead.service.LeadReadService leadReadService;
    @Mock private com.nivasafinance.features.lead.service.LeadWriteService leadWriteService;
    @Mock private com.nivasafinance.features.lead.service.LeadCallWriteService leadCallWriteService;
    @Mock private com.nivasafinance.features.task.service.TaskWriteService taskWriteService;
    @Mock private com.nivasafinance.features.person.service.PersonReadService personReadService;
    @Mock private com.nivasafinance.features.advisor.service.AdvisorReadService advisorReadService;
    @Mock private com.nivasafinance.features.advisor.service.AdvisorWriteService advisorWriteService;
    @Mock private com.nivasafinance.features.advisor.service.AdvisorCallWriteService advisorCallWriteService;
    @Mock private com.nivasafinance.features.campaign.service.CampaignReadService campaignReadService;
    @Mock private com.nivasafinance.features.campaign.service.CampaignWriteService campaignWriteService;
    @Mock private com.nivasafinance.features.call.service.CallReadService callReadService;
    @Mock private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @Mock private CallNotificationService callNotificationService;

    @InjectMocks
    private ExotelServiceImpl service;

    // ── handleWebhook: missing required fields ──────────────────────
    @Test
    void handleWebhook_missingRequired_returnsErrorStatus() {
        // Arrange
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        // Missing CallSid, CallFrom, CallTo, Direction

        // Act
        Map<String, Object> resp = service.handleWebhook(form);

        // Assert
        assertEquals("error", resp.get("status"), "Webhook with missing required fields should return error");
        assertTrue(String.valueOf(resp.get("message")).toLowerCase().contains("invalid"),
                "Error message should indicate invalid webhook data");
        verifyNoInteractions(callNotificationService);
    }

    // ── handleWebhook: happy path minimal data ──────────────────────
    @Test
    void handleWebhook_minimalValid_sendsNotificationAndReturnsSuccess() {
        // Arrange
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("CallSid", "SID-1");
        form.add("CallFrom", "+919999000111");
        form.add("CallTo", "0801234567");
        form.add("Direction", "incoming");
        form.add("CallStatus", "ringing");
        form.add("DialWhomNumber", "0809998887");
        form.add("AgentEmail", "agent@example.com");
        form.add("Timestamp", "2025-01-01T10:00:00");

        // Act
        Map<String, Object> resp = service.handleWebhook(form);

        // Assert
        assertEquals("success", resp.get("status"), "Valid webhook should return success");
        verify(callNotificationService).sendNotificationAsync(any(), any());
    }

    // ── handleWebhook: direction normalization ──────────────────────
    @Test
    void handleWebhook_directionNormalizedToInbound() {
        // Arrange
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("CallSid", "SID-2");
        form.add("CallFrom", "9999000111");
        form.add("CallTo", "0801234567");
        form.add("Direction", "INBOUND");
        form.add("Status", "initiated");

        // Act
        Map<String, Object> resp = service.handleWebhook(form);

        // Assert
        assertEquals("success", resp.get("status"), "Valid webhook should return success");
        verify(callNotificationService).sendNotificationAsync(any(), any());
    }
}

