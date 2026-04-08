package com.nivasafinance.notification.executor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.entity.NotificationTemplate;
import com.nivasafinance.notification.orchestrator.repository.NotificationTemplateRepository;
import com.nivasafinance.notification.orchestrator.service.AppNotificationTrackingService;
import com.nivasafinance.notification.orchestrator.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseNotificationExecutorTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private DeviceService deviceService;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private AppNotificationTrackingService trackingService;

    private FirebaseNotificationExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new FirebaseNotificationExecutor(
                firebaseMessaging, deviceService, notificationTemplateRepository,
                trackingService, new ObjectMapper());
    }

    private NotificationReceipt buildReceipt(String recipientContact, String templateIdentifier,
                                              Map<String, Object> messagePayload) {
        return NotificationReceipt.builder()
                .id(UUID.randomUUID())
                .recipientContact(recipientContact)
                .templateIdentifier(templateIdentifier)
                .messagePayload(messagePayload)
                .build();
    }

    private NotificationTemplate buildTemplate(String identifier) {
        return NotificationTemplate.builder()
                .identifier(identifier)
                .detail("Hello {{name}}")
                .build();
    }

    @Test
    void send_happyPath_sendsAndTracks() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "John");
        payload.put("title", "New Lead");
        payload.put("body", "Lead created for {{name}}");

        NotificationReceipt receipt = buildReceipt("appUser1", "lead_template", payload);
        NotificationTemplate template = buildTemplate("lead_template");

        when(deviceService.getActiveNotificationTokensForUser("appUser1"))
                .thenReturn(List.of("fcm-token-1"));
        when(notificationTemplateRepository.findByIdentifier("lead_template"))
                .thenReturn(Optional.of(template));
        when(firebaseMessaging.send(any(Message.class))).thenReturn("msg-001");

        executor.send(receipt, null);

        verify(firebaseMessaging).send(any(Message.class));
        verify(trackingService).saveNotificationTracking(eq(receipt), eq("fcm-token-1"), eq("msg-001"), eq("lead_template"));
    }

    @Test
    void send_nullRecipientContact_extractsAppUserFromPayload() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("appUser", "payloadUser");
        payload.put("title", "Test");
        payload.put("body", "Test body");

        NotificationReceipt receipt = buildReceipt(null, "template1", payload);
        NotificationTemplate template = buildTemplate("template1");

        when(deviceService.getActiveNotificationTokensForUser("payloadUser"))
                .thenReturn(List.of("fcm-token-1"));
        when(notificationTemplateRepository.findByIdentifier("template1"))
                .thenReturn(Optional.of(template));
        when(firebaseMessaging.send(any(Message.class))).thenReturn("msg-002");

        executor.send(receipt, null);

        verify(deviceService).getActiveNotificationTokensForUser("payloadUser");
    }

    @Test
    void send_noAppUser_throwsException() {
        Map<String, Object> payload = new HashMap<>();
        NotificationReceipt receipt = buildReceipt(null, "template1", payload);

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when app user cannot be determined");
    }

    @Test
    void send_blankRecipientContact_throwsException() {
        Map<String, Object> payload = new HashMap<>();
        NotificationReceipt receipt = buildReceipt("  ", "template1", payload);

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw for blank recipient contact without appUser in payload");
    }

    @Test
    void send_noActiveTokens_throwsException() {
        NotificationReceipt receipt = buildReceipt("appUser1", "template1", new HashMap<>());
        when(deviceService.getActiveNotificationTokensForUser("appUser1"))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when no active tokens found");
    }

    @Test
    void send_templateNotFound_throwsException() {
        NotificationReceipt receipt = buildReceipt("appUser1", "missing_template", new HashMap<>());
        when(deviceService.getActiveNotificationTokensForUser("appUser1"))
                .thenReturn(List.of("fcm-token-1"));
        when(notificationTemplateRepository.findByIdentifier("missing_template"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when template not found");
    }

    @Test
    void send_allTokensFail_throwsException() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Test");
        payload.put("body", "Test body");
        NotificationReceipt receipt = buildReceipt("appUser1", "template1", payload);
        NotificationTemplate template = buildTemplate("template1");

        when(deviceService.getActiveNotificationTokensForUser("appUser1"))
                .thenReturn(List.of("bad-token"));
        when(notificationTemplateRepository.findByIdentifier("template1"))
                .thenReturn(Optional.of(template));
        when(firebaseMessaging.send(any(Message.class))).thenThrow(new RuntimeException("FCM error"));

        assertThrows(IllegalStateException.class,
                () -> executor.send(receipt, null),
                "Should throw when all tokens fail");

        verify(trackingService).saveFailedNotificationTracking(
                eq(receipt), eq("bad-token"), eq("UNEXPECTED_ERROR"), anyString(), eq("template1"));
    }

    @Test
    void send_renderedMessage_usedAsBody() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Jane");
        NotificationReceipt receipt = buildReceipt("appUser1", "template1", payload);
        NotificationTemplate template = buildTemplate("template1");

        when(deviceService.getActiveNotificationTokensForUser("appUser1"))
                .thenReturn(List.of("fcm-token-1"));
        when(notificationTemplateRepository.findByIdentifier("template1"))
                .thenReturn(Optional.of(template));
        when(firebaseMessaging.send(any(Message.class))).thenReturn("msg-003");

        executor.send(receipt, "Pre-rendered message for {{name}}");

        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    void send_multipleTokens_partialFailure_succeeds() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Test");
        payload.put("body", "Test body");
        NotificationReceipt receipt = buildReceipt("appUser1", "template1", payload);
        NotificationTemplate template = buildTemplate("template1");

        when(deviceService.getActiveNotificationTokensForUser("appUser1"))
                .thenReturn(List.of("good-token", "bad-token"));
        when(notificationTemplateRepository.findByIdentifier("template1"))
                .thenReturn(Optional.of(template));
        when(firebaseMessaging.send(any(Message.class)))
                .thenReturn("msg-001")
                .thenThrow(new RuntimeException("FCM error"));

        executor.send(receipt, null);

        verify(trackingService).saveNotificationTracking(eq(receipt), eq("good-token"), eq("msg-001"), eq("template1"));
        verify(trackingService).saveFailedNotificationTracking(eq(receipt), eq("bad-token"), anyString(), anyString(), eq("template1"));
    }

    @Test
    void getMode_returnsFirebase() {
        assertEquals("FIREBASE", executor.getMode(), "Mode should be FIREBASE");
    }

    @Test
    void getChannelType_returnsAndroid() {
        assertEquals("ANDROID", executor.getChannelType(), "Channel type should be ANDROID");
    }
}
