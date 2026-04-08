package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import com.nivasafinance.notification.orchestrator.dto.WatiWebhookPayload;
import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.enums.WhatsAppMessageStatus;
import com.nivasafinance.notification.orchestrator.repository.AdvisorWhatsAppNotificationRepository;
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
class AdvisorWhatsAppNotificationServiceTest {

    @Mock
    private AdvisorWhatsAppNotificationRepository repository;

    private AdvisorWhatsAppNotificationService service;

    @BeforeEach
    void setUp() {
        service = new AdvisorWhatsAppNotificationService(repository);
    }

    private NotificationReceipt buildReceiptWithAdvisorId(UUID advisorId) {
        Map<String, Object> details = new HashMap<>();
        details.put("entity_id", advisorId.toString());
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
        UUID advisorId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithAdvisorId(advisorId);
        WatiSendTemplateResponse watiResponse = buildWatiResponse();

        AdvisorWhatsAppNotification saved = AdvisorWhatsAppNotification.builder()
                .id(1L).advisorIdentifier(advisorId).build();
        when(repository.save(any(AdvisorWhatsAppNotification.class))).thenReturn(saved);

        AdvisorWhatsAppNotification result = service.createFromWatiResponse(receipt, watiResponse, "my_template");

        assertNotNull(result, "Should return saved notification");
        ArgumentCaptor<AdvisorWhatsAppNotification> captor = ArgumentCaptor.forClass(AdvisorWhatsAppNotification.class);
        verify(repository).save(captor.capture());
        AdvisorWhatsAppNotification captured = captor.getValue();
        assertEquals(advisorId, captured.getAdvisorIdentifier(), "Advisor identifier should match");
        assertEquals("my_template", captured.getTemplateName(), "Template name should be from parameter");
        assertEquals(WhatsAppMessageStatus.SENT, captured.getStatus(), "Status should be SENT");
    }

    @Test
    void createFromWatiResponse_nullTemplateParam_usesResponseTemplateName() {
        UUID advisorId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithAdvisorId(advisorId);
        WatiSendTemplateResponse watiResponse = buildWatiResponse();

        when(repository.save(any(AdvisorWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdvisorWhatsAppNotification result = service.createFromWatiResponse(receipt, watiResponse, null);

        assertEquals("test_template", result.getTemplateName(), "Should fall back to response template name");
    }

    @Test
    void createFromWatiResponse_nullWatiResponse_throwsException() {
        NotificationReceipt receipt = buildReceiptWithAdvisorId(UUID.randomUUID());

        assertThrows(IllegalStateException.class,
                () -> service.createFromWatiResponse(receipt, null, "template"),
                "Should throw for null WATI response");
    }

    @Test
    void createFromWatiResponse_emptyReceivers_throwsException() {
        NotificationReceipt receipt = buildReceiptWithAdvisorId(UUID.randomUUID());
        WatiSendTemplateResponse watiResponse = WatiSendTemplateResponse.builder()
                .result(true).receivers(List.of()).build();

        assertThrows(IllegalStateException.class,
                () -> service.createFromWatiResponse(receipt, watiResponse, "template"),
                "Should throw for empty receivers");
    }

    @Test
    void createFromWatiResponse_noAdvisorId_throwsException() {
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).notificationRecordId(UUID.randomUUID())
                .details(new HashMap<>()).messagePayload(new HashMap<>()).build();
        WatiSendTemplateResponse watiResponse = buildWatiResponse();

        assertThrows(IllegalStateException.class,
                () -> service.createFromWatiResponse(receipt, watiResponse, "template"),
                "Should throw when advisor ID not found");
    }

    @Test
    void createFromGallaboxResponse_happyPath_savesNotification() {
        UUID advisorId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithAdvisorId(advisorId);

        when(repository.save(any(AdvisorWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdvisorWhatsAppNotification result = service.createFromGallaboxResponse(
                receipt, "msg-001", "sent", "template_name", "+919876543210");

        assertEquals(advisorId, result.getAdvisorIdentifier(), "Advisor identifier should match");
        assertEquals("msg-001", result.getWhatsappMessageId(), "Message ID should match");
        assertEquals(WhatsAppMessageStatus.SENT, result.getStatus(), "Status should be SENT for 'sent'");
        assertEquals("919876543210", result.getWaId(), "Phone number should have + removed");
    }

    @Test
    void createFromGallaboxResponse_nonSentStatus_setsFailedStatus() {
        UUID advisorId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithAdvisorId(advisorId);

        when(repository.save(any(AdvisorWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdvisorWhatsAppNotification result = service.createFromGallaboxResponse(
                receipt, "msg-001", "failed", "template_name", "919876543210");

        assertEquals(WhatsAppMessageStatus.FAILED, result.getStatus(), "Status should be FAILED for non-sent");
    }

    @Test
    void createFromGallaboxResponse_nullMessageId_generatesLocalMessageId() {
        UUID advisorId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithAdvisorId(advisorId);

        when(repository.save(any(AdvisorWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdvisorWhatsAppNotification result = service.createFromGallaboxResponse(
                receipt, null, "sent", "template_name", "919876543210");

        assertNotNull(result.getLocalMessageId(), "Local message ID should be generated");
        assertNull(result.getWhatsappMessageId(), "WhatsApp message ID should be null");
    }

    @Test
    void createFromGallaboxResponse_nullPhoneNumber_setsNullWaId() {
        UUID advisorId = UUID.randomUUID();
        NotificationReceipt receipt = buildReceiptWithAdvisorId(advisorId);

        when(repository.save(any(AdvisorWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdvisorWhatsAppNotification result = service.createFromGallaboxResponse(
                receipt, "msg-001", "sent", "template_name", null);

        assertNull(result.getWaId(), "WaId should be null for null phone number");
    }

    @Test
    void createFromGallaboxResponse_noAdvisorId_throwsException() {
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).notificationRecordId(UUID.randomUUID())
                .details(new HashMap<>()).messagePayload(new HashMap<>()).build();

        assertThrows(IllegalStateException.class,
                () -> service.createFromGallaboxResponse(receipt, "msg-001", "sent", "template_name", "919876543210"),
                "Should throw when advisor ID not found");
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
        AdvisorWhatsAppNotification notification = AdvisorWhatsAppNotification.builder()
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
        AdvisorWhatsAppNotification notification = AdvisorWhatsAppNotification.builder()
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
        AdvisorWhatsAppNotification notification = AdvisorWhatsAppNotification.builder()
                .id(1L).status(WhatsAppMessageStatus.SENT)
                .localMessageId("msg-001").whatsappMessageId("wa-msg-001").build();
        when(repository.findByLocalMessageId("msg-001")).thenReturn(Optional.of(notification));

        WatiWebhookPayload webhook = WatiWebhookPayload.builder()
                .localMessageId("msg-001")
                .eventType("message")
                .replyContextId("wa-msg-001")
                .text("Yes, I'm interested")
                .timestamp("1700000000")
                .build();

        service.updateFromWebhook(webhook);

        assertEquals("Yes, I'm interested", notification.getReplyText(), "Reply text should be set");
        verify(repository).save(notification);
    }

    @Test
    void updateFromWebhook_updatesWhatsappMessageId() {
        AdvisorWhatsAppNotification notification = AdvisorWhatsAppNotification.builder()
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
    void updateFromWebhook_updatesConversationAndTicketIds() {
        AdvisorWhatsAppNotification notification = AdvisorWhatsAppNotification.builder()
                .id(1L).status(WhatsAppMessageStatus.SENT).localMessageId("msg-001").build();
        when(repository.findByLocalMessageId("msg-001")).thenReturn(Optional.of(notification));

        WatiWebhookPayload webhook = WatiWebhookPayload.builder()
                .localMessageId("msg-001")
                .conversationId("conv-001")
                .ticketId("ticket-001")
                .build();

        service.updateFromWebhook(webhook);

        assertEquals("conv-001", notification.getConversationId(), "Conversation ID should be set");
        assertEquals("ticket-001", notification.getTicketId(), "Ticket ID should be set");
        verify(repository).save(notification);
    }

    @Test
    void updateFromWebhook_noUpdates_doesNotSave() {
        AdvisorWhatsAppNotification notification = AdvisorWhatsAppNotification.builder()
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
    void createFromWatiResponse_advisorIdInPayload_extractsSuccessfully() {
        UUID advisorId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("advisorIdentifier", advisorId.toString());
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).notificationRecordId(UUID.randomUUID())
                .details(new HashMap<>()).messagePayload(payload).build();
        WatiSendTemplateResponse watiResponse = buildWatiResponse();

        when(repository.save(any(AdvisorWhatsAppNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AdvisorWhatsAppNotification result = service.createFromWatiResponse(receipt, watiResponse, "template");

        assertEquals(advisorId, result.getAdvisorIdentifier(), "Should extract advisor ID from payload");
    }
}
