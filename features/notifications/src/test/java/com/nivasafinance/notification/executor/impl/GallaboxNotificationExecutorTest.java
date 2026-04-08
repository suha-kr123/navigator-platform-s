package com.nivasafinance.notification.executor.impl;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.entity.NotificationTemplate;
import com.nivasafinance.notification.orchestrator.repository.NotificationTemplateRepository;
import com.nivasafinance.notification.orchestrator.service.WhatsAppNotificationTrackingService;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.gallabox.GallaboxWhatsAppProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GallaboxNotificationExecutorTest {

    @Mock
    private GallaboxWhatsAppProvider gallaboxWhatsAppProvider;

    @Mock
    private GallaboxConfigProvider gallaboxConfigProvider;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private WhatsAppNotificationTrackingService trackingService;

    private GallaboxNotificationExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new GallaboxNotificationExecutor(
                gallaboxWhatsAppProvider, gallaboxConfigProvider,
                notificationTemplateRepository, trackingService);
    }

    private NotificationReceipt buildReceipt(String recipientType) {
        Map<String, Object> details = new HashMap<>();
        details.put("recipient_type", recipientType);
        Map<String, Object> payload = new HashMap<>();
        payload.put("LeadName", "John");
        return NotificationReceipt.builder()
                .id(UUID.randomUUID())
                .notificationRecordId(UUID.randomUUID())
                .recipientContact("+919876543210")
                .templateIdentifier("test_template")
                .details(details)
                .messagePayload(payload)
                .build();
    }

    private NotificationTemplate buildTemplate() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("LeadName", "string");
        return NotificationTemplate.builder()
                .identifier("test_template")
                .variables(variables)
                .build();
    }

    @Test
    void send_happyPath_sendsAndTracksNotification() throws Exception {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Customer Gallabox Config", "GALLABOX", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(gallaboxConfigProvider.getConfigForRecipient("LEAD")).thenReturn(config);

        WhatsAppTemplateResponse response = WhatsAppTemplateResponse.builder()
                .status("ACCEPTED").messageId("gallabox-msg-001")
                .rawResponseBody("{\"id\":\"gallabox-msg-001\",\"status\":\"ACCEPTED\"}").build();
        when(gallaboxWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(response);

        executor.send(receipt, null);

        verify(trackingService).saveNotificationTracking(eq(receipt), eq("test_template"),
                eq("{\"id\":\"gallabox-msg-001\",\"status\":\"ACCEPTED\"}"));
    }

    @Test
    void send_nullRecipientType_throwsException() {
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).recipientContact("+919876543210")
                .templateIdentifier("test_template").details(new HashMap<>())
                .messagePayload(new HashMap<>()).build();

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when recipient type is null");
    }

    @Test
    void send_blankRecipientType_throwsException() {
        Map<String, Object> details = new HashMap<>();
        details.put("recipient_type", "  ");
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).recipientContact("+919876543210")
                .templateIdentifier("test_template").details(details)
                .messagePayload(new HashMap<>()).build();

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when recipient type is blank");
    }

    @Test
    void send_templateNotFound_throwsException() {
        NotificationReceipt receipt = buildReceipt("LEAD");
        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.empty());
        when(notificationTemplateRepository.findByIdentifier("test_template"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when template not found");
    }

    @Test
    void send_responseNull_throwsException() throws Exception {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Config", "GALLABOX", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(gallaboxConfigProvider.getConfigForRecipient("LEAD")).thenReturn(config);
        when(gallaboxWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when response is null");
    }

    @Test
    void send_responseFailedStatus_throwsException() throws Exception {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Config", "GALLABOX", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(gallaboxConfigProvider.getConfigForRecipient("LEAD")).thenReturn(config);

        WhatsAppTemplateResponse response = WhatsAppTemplateResponse.builder()
                .status("FAILED").errorMessage("Invalid phone number").build();
        when(gallaboxWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(response);

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when response status is FAILED");
    }

    @Test
    void send_nullRawResponseBody_skipsTracking() throws Exception {
        NotificationReceipt receipt = buildReceipt("ADVISOR");
        NotificationTemplate template = buildTemplate();
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Config", "GALLABOX", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(gallaboxConfigProvider.getConfigForRecipient("ADVISOR")).thenReturn(config);

        WhatsAppTemplateResponse response = WhatsAppTemplateResponse.builder()
                .status("ACCEPTED").messageId("msg-001").rawResponseBody(null).build();
        when(gallaboxWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(response);

        executor.send(receipt, null);

        verifyNoInteractions(trackingService);
    }

    @Test
    void send_trackingException_doesNotFailSend() throws Exception {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Config", "GALLABOX", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(gallaboxConfigProvider.getConfigForRecipient("LEAD")).thenReturn(config);

        WhatsAppTemplateResponse response = WhatsAppTemplateResponse.builder()
                .status("ACCEPTED").messageId("msg-001").rawResponseBody("{\"id\":\"msg-001\"}").build();
        when(gallaboxWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(response);
        doThrow(new RuntimeException("Tracking error")).when(trackingService)
                .saveNotificationTracking(any(), anyString(), anyString());

        assertDoesNotThrow(() -> executor.send(receipt, null),
                "Tracking failure should not fail the send");
    }

    @Test
    void getMode_returnsGallabox() {
        assertEquals("GALLABOX", executor.getMode(), "Mode should be GALLABOX");
    }

    @Test
    void getChannelType_returnsWhatsapp() {
        assertEquals("WHATSAPP", executor.getChannelType(), "Channel type should be WHATSAPP");
    }
}
