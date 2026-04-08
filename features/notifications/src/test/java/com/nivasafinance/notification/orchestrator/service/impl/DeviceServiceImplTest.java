package com.nivasafinance.notification.orchestrator.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.notification.orchestrator.dto.RegisterDeviceRequest;
import com.nivasafinance.notification.orchestrator.entity.Device;
import com.nivasafinance.notification.orchestrator.enums.Platform;
import com.nivasafinance.notification.orchestrator.repository.DeviceRepositoryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepositoryWrapper deviceRepositoryWrapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    @BeforeEach
    void setUp() {
        UserContext.setUsername("testUser");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void registerDeviceForUser_newDevice_createsNewDevice() {
        RegisterDeviceRequest request = RegisterDeviceRequest.builder()
                .notificationToken("token123")
                .platform("ANDROID")
                .deviceId("device001")
                .appVersion("1.0")
                .osVersion("14")
                .deviceModel("Pixel")
                .apkVersion("2.0")
                .sdkVersion("33")
                .build();

        when(deviceRepositoryWrapper.findByAppUserAndDeviceId("appUser1", "device001"))
                .thenReturn(Optional.empty());
        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("appUser1", "token123"))
                .thenReturn(Optional.empty());

        Device savedDevice = Device.builder().id(1L).appUser("appUser1").build();
        when(deviceRepositoryWrapper.saveWithException(any(Device.class))).thenReturn(savedDevice);

        deviceService.registerDeviceForUser("appUser1", request);

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceRepositoryWrapper).saveWithException(captor.capture());
        Device captured = captor.getValue();
        assertEquals("appUser1", captured.getAppUser(), "App user should match");
        assertEquals("token123", captured.getNotificationToken(), "Notification token should match");
        assertEquals(Platform.ANDROID, captured.getPlatform(), "Platform should be ANDROID");
        assertTrue(captured.getIsActive(), "New device should be active");
    }

    @Test
    void registerDeviceForUser_existingDeviceByDeviceId_updatesDevice() {
        Device existingDevice = Device.builder()
                .id(10L).appUser("appUser1").notificationToken("oldToken")
                .platform(Platform.ANDROID).isActive(false).build();

        RegisterDeviceRequest request = RegisterDeviceRequest.builder()
                .notificationToken("newToken")
                .platform("ANDROID")
                .deviceId("device001")
                .appVersion("2.0")
                .build();

        when(deviceRepositoryWrapper.findByAppUserAndDeviceId("appUser1", "device001"))
                .thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryWrapper.saveWithException(any(Device.class))).thenReturn(existingDevice);

        deviceService.registerDeviceForUser("appUser1", request);

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceRepositoryWrapper).saveWithException(captor.capture());
        Device captured = captor.getValue();
        assertTrue(captured.getIsActive(), "Existing device should be reactivated");
        assertEquals("newToken", captured.getNotificationToken(), "Token should be updated");
        assertEquals("2.0", captured.getAppVersion(), "App version should be updated");
    }

    @Test
    void registerDeviceForUser_existingDeviceByToken_updatesDevice() {
        Device existingDevice = Device.builder()
                .id(10L).appUser("appUser1").notificationToken("token123")
                .platform(Platform.ANDROID).isActive(true).build();

        RegisterDeviceRequest request = RegisterDeviceRequest.builder()
                .notificationToken("token123")
                .platform("ANDROID")
                .build();

        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("appUser1", "token123"))
                .thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryWrapper.saveWithException(any(Device.class))).thenReturn(existingDevice);

        deviceService.registerDeviceForUser("appUser1", request);

        verify(deviceRepositoryWrapper).saveWithException(any(Device.class));
    }

    @Test
    void registerDeviceForUser_blankDeviceId_skipsDeviceIdLookup() {
        RegisterDeviceRequest request = RegisterDeviceRequest.builder()
                .notificationToken("token123")
                .platform("ANDROID")
                .deviceId("  ")
                .build();

        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("appUser1", "token123"))
                .thenReturn(Optional.empty());

        Device savedDevice = Device.builder().id(1L).appUser("appUser1").build();
        when(deviceRepositoryWrapper.saveWithException(any(Device.class))).thenReturn(savedDevice);

        deviceService.registerDeviceForUser("appUser1", request);

        verify(deviceRepositoryWrapper, never()).findByAppUserAndDeviceId(anyString(), anyString());
    }

    @Test
    void registerDeviceForUser_nullOptionalFields_doesNotOverwriteExisting() {
        Device existingDevice = Device.builder()
                .id(10L).appUser("appUser1").notificationToken("token123")
                .platform(Platform.ANDROID).isActive(true)
                .appVersion("1.0").osVersion("13").deviceModel("Pixel")
                .apkVersion("1.0").sdkVersion("30")
                .build();

        RegisterDeviceRequest request = RegisterDeviceRequest.builder()
                .notificationToken("token123")
                .platform(null)
                .deviceId(null)
                .appVersion(null)
                .osVersion(null)
                .deviceModel(null)
                .apkVersion(null)
                .sdkVersion(null)
                .build();

        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("appUser1", "token123"))
                .thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryWrapper.saveWithException(any(Device.class))).thenReturn(existingDevice);

        deviceService.registerDeviceForUser("appUser1", request);

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceRepositoryWrapper).saveWithException(captor.capture());
        Device captured = captor.getValue();
        assertEquals("1.0", captured.getAppVersion(), "App version should remain unchanged");
        assertEquals(Platform.ANDROID, captured.getPlatform(), "Platform should remain unchanged");
    }

    @Test
    void registerDeviceForUser_noUserContext_usesSystemAsCreatedBy() {
        UserContext.clear();

        RegisterDeviceRequest request = RegisterDeviceRequest.builder()
                .notificationToken("token123")
                .platform("ANDROID")
                .build();

        when(deviceRepositoryWrapper.findByAppUserAndNotificationToken("appUser1", "token123"))
                .thenReturn(Optional.empty());

        Device savedDevice = Device.builder().id(1L).appUser("appUser1").build();
        when(deviceRepositoryWrapper.saveWithException(any(Device.class))).thenReturn(savedDevice);

        deviceService.registerDeviceForUser("appUser1", request);

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceRepositoryWrapper).saveWithException(captor.capture());
        assertEquals("system", captor.getValue().getCreatedBy(), "CreatedBy should be 'system' when no user context");
    }

    @Test
    void deactivateDevicesByTokens_nullTokens_returnsZero() {
        int result = deviceService.deactivateDevicesByTokens("appUser1", null);

        assertEquals(0, result, "Should return 0 for null tokens");
        verifyNoInteractions(deviceRepositoryWrapper);
    }

    @Test
    void deactivateDevicesByTokens_emptyTokens_returnsZero() {
        int result = deviceService.deactivateDevicesByTokens("appUser1", Collections.emptyList());

        assertEquals(0, result, "Should return 0 for empty tokens");
        verifyNoInteractions(deviceRepositoryWrapper);
    }

    @Test
    void deactivateDevicesByTokens_noDevicesFound_returnsZero() {
        when(deviceRepositoryWrapper.findByAppUserAndNotificationTokenIn("appUser1", List.of("token1")))
                .thenReturn(Collections.emptyList());

        int result = deviceService.deactivateDevicesByTokens("appUser1", List.of("token1"));

        assertEquals(0, result, "Should return 0 when no devices found");
    }

    @Test
    void deactivateDevicesByTokens_activeDevices_deactivatesAndReturnsCount() {
        Device activeDevice = Device.builder()
                .id(1L).appUser("appUser1").notificationToken("token1").isActive(true).build();
        Device inactiveDevice = Device.builder()
                .id(2L).appUser("appUser1").notificationToken("token2").isActive(false).build();

        when(deviceRepositoryWrapper.findByAppUserAndNotificationTokenIn("appUser1", List.of("token1", "token2")))
                .thenReturn(List.of(activeDevice, inactiveDevice));
        when(deviceRepositoryWrapper.saveWithException(any(Device.class))).thenReturn(activeDevice);

        int result = deviceService.deactivateDevicesByTokens("appUser1", List.of("token1", "token2"));

        assertEquals(1, result, "Should only deactivate active devices");
        assertFalse(activeDevice.getIsActive(), "Active device should be deactivated");
    }

    @Test
    void getActiveNotificationTokensForUser_delegatesToWrapper() {
        List<String> tokens = List.of("token1", "token2");
        when(deviceRepositoryWrapper.findActiveNotificationTokensByAppUser("appUser1")).thenReturn(tokens);

        List<String> result = deviceService.getActiveNotificationTokensForUser("appUser1");

        assertEquals(tokens, result, "Should return tokens from wrapper");
        verify(deviceRepositoryWrapper).findActiveNotificationTokensByAppUser("appUser1");
    }
}
