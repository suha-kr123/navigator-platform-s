package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import com.nivasafinance.notification.orchestrator.dto.WatiWebhookPayload;
import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.enums.WhatsAppMessageStatus;
import com.nivasafinance.notification.orchestrator.repository.LeadWhatsAppNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadWhatsAppNotificationServiceTest {

    @Mock
    private LeadWhatsAppNotificationRepository repository;

    private LeadWhatsAppNotificationService service;

    @BeforeEach
    void setUp() {
        service = new LeadWhatsAppNotificationService(repository);
    }

    private NotificationReceipt buildReceiptWithLeadId(UUID leadId) {
        Map<String, Object> details = new HashMap<>();
        details.put("entity_id", leadId.toString());
        return NotificationReceipt.builder()
                .id(UUID.randomUUID())
                .notificationRecordId(UUID.randomUUID())
                .details(details)
                .build();
    }

    private WatiSendTemplateResponse buildWatiResponse() {
        WatiSendTemplateResponse.Receiver receiver = WatiSendTemplateResponse.Receiver.builder()
                .localMessageId("local-msg-001")
                .waId("919876543210")
                .isValidWhatsAppNumber(true)
                .build();
        return WatiSendTemplateResponse.builder()
                .result(true)
                .templateName("test_template")
                .receivers(List.of(receiver))
                .build();
    }

    @Test
    void createFromWatiResponse_happyPath_savesNotification() {
        UUID leadId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithLeadId(leadId);
        WatiSendTemplateResponse watiResponse = buildWatiResponse();

        LeadWhatsAppNotification saved = LeadWhatsAppNotification.builder()
                .id(1L).leadIdentifier(leadId).build();
        when(repository.save(any(LeadWhatsAppNotification.class))).thenReturn(saved);

        LeadWhatsAppNotification result = service.createFromWatiResponse(receipt, watiResponse, "my_template");

        assertNotNull(result, "Should return saved notification");
        ArgumentCaptor<LeadWhatsAppNotification> captor = ArgumentCaptor.forClass(LeadWhatsAppNotification.class);
        verify(repository).save(captor.capture());
        LeadWhatsAppNotification captured = captor.getValue();
        assertEquals(leadId, captured.getLeadIdentifier(), "Lead identifier should match");
        assertEquals("my_template", captured.getTemplateName(), "Template name should be from parameter");
        assertEquals(WhatsAppMessageStatus.SENT, captured.getStatus(), "Status should be SENT");
    }

    @Test
    void createFromWatiResponse_nullTemplateParam_usesResponseTemplateName() {
        UUID leadId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithLeadId(leadId);
        WatiSendTemplateResponse watiResponse = buildWatiResponse();

        when(repository.save(any(LeadWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LeadWhatsAppNotification result = service.createFromWatiResponse(receipt, watiResponse, null);

        assertEquals("test_template", result.getTemplateName(), "Should fall back to response template name");
    }

    @Test
    void createFromWatiResponse_nullWatiResponse_throwsException() {
        NotificationReceipt receipt = buildReceiptWithLeadId(UUID.randomUUID());

        assertThrows(IllegalStateException.class,
                () -> service.createFromWatiResponse(receipt, null, "template"),
                "Should throw for null WATI response");
    }

    @Test
    void createFromWatiResponse_emptyReceivers_throwsException() {
        NotificationReceipt receipt = buildReceiptWithLeadId(UUID.randomUUID());
        WatiSendTemplateResponse watiResponse = WatiSendTemplateResponse.builder()
                .result(true).receivers(List.of()).build();

        assertThrows(IllegalStateException.class,
                () -> service.createFromWatiResponse(receipt, watiResponse, "template"),
                "Should throw for empty receivers");
    }

    @Test
    void createFromWatiResponse_noLeadId_throwsException() {
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).notificationRecordId(UUID.randomUUID())
                .details(new HashMap<>()).messagePayload(new HashMap<>()).build();
        WatiSendTemplateResponse watiResponse = buildWatiResponse();

        assertThrows(IllegalStateException.class,
                () -> service.createFromWatiResponse(receipt, watiResponse, "template"),
                "Should throw when lead ID not found");
    }

    @Test
    void createFromGallaboxResponse_happyPath_savesNotification() {
        UUID leadId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithLeadId(leadId);

        when(repository.save(any(LeadWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LeadWhatsAppNotification result = service.createFromGallaboxResponse(
                receipt, "msg-001", "sent", "template_name", "+919876543210");

        assertEquals(leadId, result.getLeadIdentifier(), "Lead identifier should match");
        assertEquals("msg-001", result.getWhatsappMessageId(), "Message ID should match");
        assertEquals(WhatsAppMessageStatus.SENT, result.getStatus(), "Status should be SENT for 'sent'");
        assertEquals("919876543210", result.getWaId(), "Phone number should have + removed");
    }

    @Test
    void createFromGallaboxResponse_nonSentStatus_setsFailedStatus() {
        UUID leadId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithLeadId(leadId);

        when(repository.save(any(LeadWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LeadWhatsAppNotification result = service.createFromGallaboxResponse(
                receipt, "msg-001", "rejected", "template_name", "919876543210");

        assertEquals(WhatsAppMessageStatus.FAILED, result.getStatus(), "Status should be FAILED for non-sent");
    }

    @Test
    void createFromGallaboxResponse_nullMessageId_generatesLocalMessageId() {
        UUID leadId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithLeadId(leadId);

        when(repository.save(any(LeadWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LeadWhatsAppNotification result = service.createFromGallaboxResponse(
                receipt, null, "sent", "template_name", "919876543210");

        assertNotNull(result.getLocalMessageId(), "Local message ID should be generated");
        assertNull(result.getWhatsappMessageId(), "WhatsApp message ID should be null");
    }

    @Test
    void createFromGallaboxResponse_noLeadId_throwsException() {
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).notificationRecordId(UUID.randomUUID())
                .details(new HashMap<>()).messagePayload(new HashMap<>()).build();

        assertThrows(IllegalStateException.class,
                () -> service.createFromGallaboxResponse(receipt, "msg-001", "sent", "template_name", "919876543210"),
                "Should throw when lead ID not found");
    }

    @Test
    void updateFromWebhook_nullLocalMessageId_returnsEarly() {
        WatiWebhookPayload webhook = WatiWebhookPayload.builder().localMessageId(null).build();

        service.updateFromWebhook(webhook);

        verifyNoInteractions(repository);
    }

    @Test
    void updateFromWebhook_notificationNotFound_returnsEarly() {
        WatiWebhookPayload webhook = WatiWebhookPayload.builder().localMessageId("msg-001").build();
        when(repository.findByLocalMessageId("msg-001")).thenReturn(Optional.empty());

        service.updateFromWebhook(webhook);

        verify(repository, never()).save(any());
    }

    @Test
    void updateFromWebhook_deliveredEvent_updatesStatusToDelivered() {
        LeadWhatsAppNotification notification = LeadWhatsAppNotification.builder()
                .id(1L).status(WhatsAppMessageStatus.SENT).localMessageId("msg-001").build();
        when(repository.findByLocalMessageId("msg-001")).thenReturn(Optional.of(notification));

        WatiWebhookPayload webhook = WatiWebhookPayload.builder()
                .localMessageId("msg-001")
                .eventType("sentMessageDELIVERED_v2")
                .timestamp("1700000000")
                .build();

        service.updateFromWebhook(webhook);

        assertEquals(WhatsAppMessageStatus.DELIVERED, notification.getStatus(), "Status should be DELIVERED");
        assertNotNull(notification.getDeliveredTimestamp(), "Delivered timestamp should be set");
        verify(repository).save(notification);
    }

    @Test
    void updateFromWebhook_repliedEvent_updatesStatusToReplied() {
        LeadWhatsAppNotification notification = LeadWhatsAppNotification.builder()
                .id(1L).status(WhatsAppMessageStatus.DELIVERED).localMessageId("msg-001").build();
        when(repository.findByLocalMessageId("msg-001")).thenReturn(Optional.of(notification));

        WatiWebhookPayload webhook = WatiWebhookPayload.builder()
                .localMessageId("msg-001")
                .eventType("sentMessageREPLIED_v2")
                .timestamp("1700000000")
                .build();

        service.updateFromWebhook(webhook);

        assertEquals(WhatsAppMessageStatus.REPLIED, notification.getStatus(), "Status should be REPLIED");
        assertTrue(notification.getIsReplied(), "IsReplied should be true");
    }

    @Test
    void updateFromWebhook_messageWithReplyContext_updatesReplyText() {
        LeadWhatsAppNotification notification = LeadWhatsAppNotification.builder()
                .id(1L).status(WhatsAppMessageStatus.SENT)
                .localMessageId("msg-001").whatsappMessageId("wa-msg-001").build();
        when(repository.findByLocalMessageId("msg-001")).thenReturn(Optional.of(notification));

        WatiWebhookPayload webhook = WatiWebhookPayload.builder()
                .localMessageId("msg-001")
                .eventType("message")
                .replyContextId("wa-msg-001")
                .text("I'm interested")
                .timestamp("1700000000")
                .build();

        service.updateFromWebhook(webhook);

        assertEquals("I'm interested", notification.getReplyText(), "Reply text should be set");
        verify(repository).save(notification);
    }

    @Test
    void updateFromWebhook_updatesWhatsappMessageId() {
        LeadWhatsAppNotification notification = LeadWhatsAppNotification.builder()
                .id(1L).status(WhatsAppMessageStatus.SENT)
                .localMessageId("msg-001").whatsappMessageId(null).build();
        when(repository.findByLocalMessageId("msg-001")).thenReturn(Optional.of(notification));

        WatiWebhookPayload webhook = WatiWebhookPayload.builder()
                .localMessageId("msg-001")
                .whatsappMessageId("wamid-001")
                .build();

        service.updateFromWebhook(webhook);

        assertEquals("wamid-001", notification.getWhatsappMessageId(), "WhatsApp message ID should be updated");
        verify(repository).save(notification);
    }

    @Test
    void updateFromWebhook_noUpdates_doesNotSave() {
        LeadWhatsAppNotification notification = LeadWhatsAppNotification.builder()
                .id(1L).status(WhatsAppMessageStatus.SENT)
                .localMessageId("msg-001").whatsappMessageId("already-set").build();
        when(repository.findByLocalMessageId("msg-001")).thenReturn(Optional.of(notification));

        WatiWebhookPayload webhook = WatiWebhookPayload.builder()
                .localMessageId("msg-001")
                .build();

        service.updateFromWebhook(webhook);

        verify(repository, never()).save(any());
    }

    @Test
    void createFromWatiResponse_leadIdInPayload_extractsSuccessfully() {
        UUID leadId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("leadIdentifier", leadId.toString());
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).notificationRecordId(UUID.randomUUID())
                .details(new HashMap<>()).messagePayload(payload).build();
        WatiSendTemplateResponse watiResponse = buildWatiResponse();

        when(repository.save(any(LeadWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LeadWhatsAppNotification result = service.createFromWatiResponse(receipt, watiResponse, "template");

        assertEquals(leadId, result.getLeadIdentifier(), "Should extract lead ID from payload");
    }
}
