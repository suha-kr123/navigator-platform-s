package com.nivasafinance.notification.executor.impl;

import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.core.data.ThirdPartyConfig;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.entity.NotificationRecord;
import com.nivasafinance.notification.orchestrator.entity.NotificationTemplate;
import com.nivasafinance.notification.orchestrator.repository.NotificationRecordRepository;
import com.nivasafinance.notification.orchestrator.repository.NotificationTemplateRepository;
import com.nivasafinance.notification.orchestrator.service.WhatsAppNotificationTrackingService;
import com.nivasafinance.services.whatsapp.dto.WhatsAppTemplateResponse;
import com.nivasafinance.services.whatsapp.provider.wati.WatiWhatsAppProvider;
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
class WhatsAppNotificationExecutorTest {

    @Mock
    private WatiWhatsAppProvider watiWhatsAppProvider;

    @Mock
    private WatiConfigProvider watiConfigProvider;

    @Mock
    private NotificationRecordRepository notificationRecordRepository;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private WhatsAppNotificationTrackingService trackingService;

    private WhatsAppNotificationExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new WhatsAppNotificationExecutor(
                watiWhatsAppProvider, watiConfigProvider,
                notificationRecordRepository, notificationTemplateRepository, trackingService);
    }

    private NotificationReceipt buildReceipt(String recipientType) {
        UUID recordId = UUID.randomUUID();
        Map<String, Object> details = new HashMap<>();
        details.put("recipient_type", recipientType);
        Map<String, Object> payload = new HashMap<>();
        payload.put("LeadName", "John");
        return NotificationReceipt.builder()
                .id(UUID.randomUUID())
                .notificationRecordId(recordId)
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

    private NotificationRecord buildRecord(UUID recordId, String idempotencyKey) {
        return NotificationRecord.builder()
                .id(recordId)
                .idempotencyKey(idempotencyKey)
                .build();
    }

    @Test
    void send_happyPath_sendsAndTracksNotification() throws Exception {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();
        NotificationRecord record = buildRecord(receipt.getNotificationRecordId(), "idem-key-001");
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Customer WATI Config", "WATI", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(notificationRecordRepository.findById(receipt.getNotificationRecordId()))
                .thenReturn(Optional.of(record));
        when(watiConfigProvider.getConfigForRecipient("LEAD")).thenReturn(config);

        WhatsAppTemplateResponse response = WhatsAppTemplateResponse.builder()
                .status("sent").messageId("wati-msg-001")
                .rawResponseBody("{\"result\":true}").build();
        when(watiWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(response);

        executor.send(receipt, null);

        verify(trackingService).saveNotificationTracking(eq(receipt), eq("test_template"), eq("{\"result\":true}"));
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
    void send_notificationRecordNotFound_throwsException() {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(notificationRecordRepository.findById(receipt.getNotificationRecordId()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when notification record not found");
    }

    @Test
    void send_responseNull_throwsException() throws Exception {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();
        NotificationRecord record = buildRecord(receipt.getNotificationRecordId(), null);
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Config", "WATI", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(notificationRecordRepository.findById(receipt.getNotificationRecordId()))
                .thenReturn(Optional.of(record));
        when(watiConfigProvider.getConfigForRecipient("LEAD")).thenReturn(config);
        when(watiWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when response is null");
    }

    @Test
    void send_responseNotSentStatus_throwsException() throws Exception {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();
        NotificationRecord record = buildRecord(receipt.getNotificationRecordId(), null);
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Config", "WATI", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(notificationRecordRepository.findById(receipt.getNotificationRecordId()))
                .thenReturn(Optional.of(record));
        when(watiConfigProvider.getConfigForRecipient("LEAD")).thenReturn(config);

        WhatsAppTemplateResponse response = WhatsAppTemplateResponse.builder()
                .status("FAILED").errorMessage("Template not approved").build();
        when(watiWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(response);

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when response status is not 'sent'");
    }

    @Test
    void send_nullIdempotencyKey_usesFallbackBroadcastName() throws Exception {
        NotificationReceipt receipt = buildReceipt("ADVISOR");
        NotificationTemplate template = buildTemplate();
        NotificationRecord record = buildRecord(receipt.getNotificationRecordId(), null);
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Config", "WATI", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(notificationRecordRepository.findById(receipt.getNotificationRecordId()))
                .thenReturn(Optional.of(record));
        when(watiConfigProvider.getConfigForRecipient("ADVISOR")).thenReturn(config);

        WhatsAppTemplateResponse response = WhatsAppTemplateResponse.builder()
                .status("sent").messageId("msg-002").rawResponseBody("{\"result\":true}").build();
        when(watiWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(response);

        executor.send(receipt, null);

        verify(watiWhatsAppProvider).sendTemplate(argThat(req ->
                req.getBroadcastName().startsWith("notification_")), eq(config), any(BusinessContext.class));
    }

    @Test
    void send_trackingException_doesNotFailSend() throws Exception {
        NotificationReceipt receipt = buildReceipt("LEAD");
        NotificationTemplate template = buildTemplate();
        NotificationRecord record = buildRecord(receipt.getNotificationRecordId(), "key");
        ThirdPartyConfig config = new ThirdPartyConfig(null, "Config", "WATI", Map.of());

        when(notificationTemplateRepository.findByIdentifierIgnoreCase("test_template"))
                .thenReturn(Optional.of(template));
        when(notificationRecordRepository.findById(receipt.getNotificationRecordId()))
                .thenReturn(Optional.of(record));
        when(watiConfigProvider.getConfigForRecipient("LEAD")).thenReturn(config);

        WhatsAppTemplateResponse response = WhatsAppTemplateResponse.builder()
                .status("sent").messageId("msg-001").rawResponseBody("{\"result\":true}").build();
        when(watiWhatsAppProvider.sendTemplate(any(), eq(config), any(BusinessContext.class)))
                .thenReturn(response);
        doThrow(new RuntimeException("Tracking error")).when(trackingService)
                .saveNotificationTracking(any(), anyString(), anyString());

        assertDoesNotThrow(() -> executor.send(receipt, null),
                "Tracking failure should not fail the send");
    }

    @Test
    void getMode_returnsWati() {
        assertEquals("WATI", executor.getMode(), "Mode should be WATI");
    }

    @Test
    void getChannelType_returnsWhatsapp() {
        assertEquals("WHATSAPP", executor.getChannelType(), "Channel type should be WHATSAPP");
    }
}
