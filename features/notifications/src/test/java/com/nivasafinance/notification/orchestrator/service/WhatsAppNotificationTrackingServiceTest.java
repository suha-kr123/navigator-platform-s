package com.nivasafinance.notification.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppNotificationTrackingServiceTest {

    @Mock
    private WatiResponseParser watiResponseParser;

    @Mock
    private LeadWhatsAppNotificationService leadWhatsAppNotificationService;

    @Mock
    private AdvisorWhatsAppNotificationService advisorWhatsAppNotificationService;

    @Mock
    private ObjectMapper objectMapper;

    private WhatsAppNotificationTrackingService service;

    @BeforeEach
    void setUp() {
        service = new WhatsAppNotificationTrackingService(
                watiResponseParser, leadWhatsAppNotificationService,
                advisorWhatsAppNotificationService, objectMapper);
    }

    private NotificationReceipt buildReceipt(String mode, String recipientType) {
        Map<String, Object> details = new HashMap<>();
        if (recipientType != null) {
            details.put("recipient_type", recipientType);
        }
        return NotificationReceipt.builder()
                .id(UUID.randomUUID())
                .mode(mode)
                .recipientContact("+919876543210")
                .details(details)
                .build();
    }

    @Test
    void saveNotificationTracking_nullResponseBody_skips() {
        NotificationReceipt receipt = buildReceipt("WATI", "LEAD");

        service.saveNotificationTracking(receipt, "template", null);

        verifyNoInteractions(watiResponseParser);
        verifyNoInteractions(leadWhatsAppNotificationService);
    }

    @Test
    void saveNotificationTracking_blankResponseBody_skips() {
        NotificationReceipt receipt = buildReceipt("WATI", "LEAD");

        service.saveNotificationTracking(receipt, "template", "  ");

        verifyNoInteractions(watiResponseParser);
    }

    @Test
    void saveNotificationTracking_nullMode_skips() {
        NotificationReceipt receipt = buildReceipt(null, "LEAD");

        service.saveNotificationTracking(receipt, "template", "{}");

        verifyNoInteractions(watiResponseParser);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void saveNotificationTracking_blankMode_skips() {
        NotificationReceipt receipt = buildReceipt("  ", "LEAD");

        service.saveNotificationTracking(receipt, "template", "{}");

        verifyNoInteractions(watiResponseParser);
    }

    @Test
    void saveNotificationTracking_watiLeadRecipient_delegatesToLeadService() {
        NotificationReceipt receipt = buildReceipt("WATI", "LEAD");
        WatiSendTemplateResponse watiResponse = WatiSendTemplateResponse.builder()
                .result(true).receivers(List.of()).build();
        when(watiResponseParser.parseSendTemplateResponse(anyString())).thenReturn(watiResponse);

        service.saveNotificationTracking(receipt, "template", "{\"result\":true}");

        verify(leadWhatsAppNotificationService).createFromWatiResponse(receipt, watiResponse, "template");
        verifyNoInteractions(advisorWhatsAppNotificationService);
    }

    @Test
    void saveNotificationTracking_watiAdvisorRecipient_delegatesToAdvisorService() {
        NotificationReceipt receipt = buildReceipt("WATI", "ADVISOR");
        WatiSendTemplateResponse watiResponse = WatiSendTemplateResponse.builder()
                .result(true).receivers(List.of()).build();
        when(watiResponseParser.parseSendTemplateResponse(anyString())).thenReturn(watiResponse);

        service.saveNotificationTracking(receipt, "template", "{\"result\":true}");

        verify(advisorWhatsAppNotificationService).createFromWatiResponse(receipt, watiResponse, "template");
        verifyNoInteractions(leadWhatsAppNotificationService);
    }

    @Test
    void saveNotificationTracking_watiUnknownRecipient_logsWarning() {
        NotificationReceipt receipt = buildReceipt("WATI", "UNKNOWN");
        WatiSendTemplateResponse watiResponse = WatiSendTemplateResponse.builder()
                .result(true).receivers(List.of()).build();
        when(watiResponseParser.parseSendTemplateResponse(anyString())).thenReturn(watiResponse);

        service.saveNotificationTracking(receipt, "template", "{\"result\":true}");

        verifyNoInteractions(leadWhatsAppNotificationService);
        verifyNoInteractions(advisorWhatsAppNotificationService);
    }

    @Test
    void saveNotificationTracking_watiParseReturnsNull_skips() {
        NotificationReceipt receipt = buildReceipt("WATI", "LEAD");
        when(watiResponseParser.parseSendTemplateResponse(anyString())).thenReturn(null);

        service.saveNotificationTracking(receipt, "template", "invalid json");

        verifyNoInteractions(leadWhatsAppNotificationService);
    }

    @Test
    void saveNotificationTracking_gallaboxLeadRecipient_delegatesToLeadService() throws Exception {
        NotificationReceipt receipt = buildReceipt("GALLABOX", "LEAD");
        String responseBody = "{\"id\":\"msg-001\",\"status\":\"ACCEPTED\"}";

        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        when(objectMapper.readTree(responseBody)).thenReturn(jsonNode);

        service.saveNotificationTracking(receipt, "template", responseBody);

        verify(leadWhatsAppNotificationService).createFromGallaboxResponse(
                eq(receipt), eq("msg-001"), eq("sent"), eq("template"), eq("+919876543210"));
    }

    @Test
    void saveNotificationTracking_gallaboxAdvisorRecipient_delegatesToAdvisorService() throws Exception {
        NotificationReceipt receipt = buildReceipt("GALLABOX", "ADVISOR");
        String responseBody = "{\"id\":\"msg-001\",\"status\":\"ACCEPTED\"}";

        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        when(objectMapper.readTree(responseBody)).thenReturn(jsonNode);

        service.saveNotificationTracking(receipt, "template", responseBody);

        verify(advisorWhatsAppNotificationService).createFromGallaboxResponse(
                eq(receipt), eq("msg-001"), eq("sent"), eq("template"), eq("+919876543210"));
    }

    @Test
    void saveNotificationTracking_unknownMode_logsWarning() {
        NotificationReceipt receipt = buildReceipt("TWILIO", "LEAD");

        service.saveNotificationTracking(receipt, "template", "{\"data\":\"test\"}");

        verifyNoInteractions(watiResponseParser);
        verifyNoInteractions(leadWhatsAppNotificationService);
        verifyNoInteractions(advisorWhatsAppNotificationService);
    }

    @Test
    void saveNotificationTracking_exceptionThrown_caughtSilently() throws Exception {
        NotificationReceipt receipt = buildReceipt("GALLABOX", "LEAD");
        when(objectMapper.readTree(anyString())).thenThrow(new RuntimeException("Parse error"));

        service.saveNotificationTracking(receipt, "template", "{\"bad\":\"json\"}");

        verifyNoInteractions(leadWhatsAppNotificationService);
    }

    @Test
    void saveNotificationTracking_gallaboxNonAcceptedStatus_passesLowercaseStatus() throws Exception {
        NotificationReceipt receipt = buildReceipt("GALLABOX", "LEAD");
        String responseBody = "{\"id\":\"msg-001\",\"status\":\"REJECTED\"}";

        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        when(objectMapper.readTree(responseBody)).thenReturn(jsonNode);

        service.saveNotificationTracking(receipt, "template", responseBody);

        verify(leadWhatsAppNotificationService).createFromGallaboxResponse(
                eq(receipt), eq("msg-001"), eq("rejected"), eq("template"), eq("+919876543210"));
    }
}
