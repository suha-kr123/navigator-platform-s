package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.entity.AppNotification;
import com.nivasafinance.notification.orchestrator.entity.Device;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.enums.AppNotificationStatus;
import com.nivasafinance.notification.orchestrator.repository.AppNotificationRepository;
import com.nivasafinance.notification.orchestrator.repository.DeviceRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppNotificationTrackingServiceTest {

    @Mock
    private AppNotificationRepository appNotificationRepository;

    @Mock
    private DeviceRepositoryWrapper deviceRepositoryWrapper;

    private AppNotificationTrackingService service;

    @BeforeEach
    void setUp() {
        service = new AppNotificationTrackingService(appNotificationRepository, deviceRepositoryWrapper);
    }

    private NotificationReceipt buildReceiptWithAppUser(String appUser) {
        Map<String, Object> details = new HashMap<>();
        details.put("appUser", appUser);
        return NotificationReceipt.builder()
                .id(UUID.randomUUID())
                .notificationRecordId(UUID.randomUUID())
                .templateIdentifier("template-001")
                .details(details)
                .build();
    }

    @Test
    void saveNotificationTracking_happyPath_savesRecord() {
        NotificationReceipt receipt = buildReceiptWithAppUser("user1");
        Device device = Device.builder().id(10L).appUser("user1").build();

        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("user1", "fcm-token"))
                .thenReturn(Optional.of(device));

        service.saveNotificationTracking(receipt, "fcm-token", "provider-msg-001", "test_template");

        ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
        verify(appNotificationRepository).save(captor.capture());
        AppNotification captured = captor.getValue();
        assertEquals(10L, captured.getDeviceId(), "Device ID should match");
        assertEquals(AppNotificationStatus.SENT, captured.getStatus(), "Status should be SENT");
        assertEquals("provider-msg-001", captured.getProviderMessageId(), "Provider message ID should match");
        assertEquals("test_template", captured.getTemplateName(), "Template name should match");
    }

    @Test
    void saveNotificationTracking_nullAppUser_skips() {
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).details(new HashMap<>()).messagePayload(new HashMap<>()).build();

        service.saveNotificationTracking(receipt, "fcm-token", "provider-msg-001", "test_template");

        verifyNoInteractions(deviceRepositoryWrapper);
        verifyNoInteractions(appNotificationRepository);
    }

    @Test
    void saveNotificationTracking_deviceNotFound_skips() {
        NotificationReceipt receipt = buildReceiptWithAppUser("user1");
        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("user1", "fcm-token"))
                .thenReturn(Optional.empty());

        service.saveNotificationTracking(receipt, "fcm-token", "provider-msg-001", "test_template");

        verifyNoInteractions(appNotificationRepository);
    }

    @Test
    void saveNotificationTracking_exceptionThrown_caughtSilently() {
        NotificationReceipt receipt = buildReceiptWithAppUser("user1");
        Device device = Device.builder().id(10L).appUser("user1").build();
        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("user1", "fcm-token"))
                .thenReturn(Optional.of(device));
        doThrow(new RuntimeException("DB error")).when(appNotificationRepository).save(any());

        assertDoesNotThrow(
                () -> service.saveNotificationTracking(receipt, "fcm-token", "provider-msg-001", "test_template"),
                "Exception should be caught silently");
    }

    @Test
    void saveNotificationTracking_appUserInPayload_extractsSuccessfully() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("appUser", "payloadUser");
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).notificationRecordId(UUID.randomUUID())
                .templateIdentifier("template-001")
                .details(new HashMap<>()).messagePayload(payload).build();
        Device device = Device.builder().id(10L).appUser("payloadUser").build();

        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("payloadUser", "fcm-token"))
                .thenReturn(Optional.of(device));

        service.saveNotificationTracking(receipt, "fcm-token", "provider-msg-001", "test_template");

        verify(appNotificationRepository).save(any(AppNotification.class));
    }

    @Test
    void saveFailedNotificationTracking_happyPath_savesFailedRecord() {
        NotificationReceipt receipt = buildReceiptWithAppUser("user1");
        Device device = Device.builder().id(10L).appUser("user1").build();

        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("user1", "fcm-token"))
                .thenReturn(Optional.of(device));

        service.saveFailedNotificationTracking(receipt, "fcm-token", "UNREGISTERED", "Token invalid", "test_template");

        ArgumentCaptor<AppNotification> captor = ArgumentCaptor.forClass(AppNotification.class);
        verify(appNotificationRepository).save(captor.capture());
        AppNotification captured = captor.getValue();
        assertEquals(AppNotificationStatus.FAILED, captured.getStatus(), "Status should be FAILED");
        assertEquals("UNREGISTERED", captured.getErrorCode(), "Error code should match");
        assertEquals("Token invalid", captured.getErrorMessage(), "Error message should match");
        assertNull(captured.getProviderMessageId(), "Provider message ID should be null for failed");
    }

    @Test
    void saveFailedNotificationTracking_nullAppUser_skips() {
        NotificationReceipt receipt = NotificationReceipt.builder()
                .id(UUID.randomUUID()).details(new HashMap<>()).messagePayload(new HashMap<>()).build();

        service.saveFailedNotificationTracking(receipt, "fcm-token", "ERROR", "msg", "test_template");

        verifyNoInteractions(deviceRepositoryWrapper);
        verifyNoInteractions(appNotificationRepository);
    }

    @Test
    void saveFailedNotificationTracking_deviceNotFound_skips() {
        NotificationReceipt receipt = buildReceiptWithAppUser("user1");
        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("user1", "fcm-token"))
                .thenReturn(Optional.empty());

        service.saveFailedNotificationTracking(receipt, "fcm-token", "ERROR", "msg", "test_template");

        verifyNoInteractions(appNotificationRepository);
    }

    @Test
    void saveFailedNotificationTracking_exceptionThrown_caughtSilently() {
        NotificationReceipt receipt = buildReceiptWithAppUser("user1");
        Device device = Device.builder().id(10L).appUser("user1").build();
        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("user1", "fcm-token"))
                .thenReturn(Optional.of(device));
        doThrow(new RuntimeException("DB error")).when(appNotificationRepository).save(any());

        assertDoesNotThrow(
                () -> service.saveFailedNotificationTracking(receipt, "fcm-token", "ERROR", "msg", "test_template"),
                "Exception should be caught silently");
    }
}
