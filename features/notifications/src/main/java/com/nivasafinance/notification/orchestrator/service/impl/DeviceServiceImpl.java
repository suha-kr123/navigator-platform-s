package com.nivasafinance.notification.orchestrator.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.notification.orchestrator.dto.RegisterDeviceRequest;
import com.nivasafinance.notification.orchestrator.entity.Device;
import com.nivasafinance.notification.orchestrator.enums.Platform;
import com.nivasafinance.notification.orchestrator.repository.DeviceRepositoryWrapper;
import com.nivasafinance.notification.orchestrator.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepositoryWrapper deviceRepositoryWrapper;

    @Override
    @Transactional
    public void registerDeviceForUser(String appUser, RegisterDeviceRequest request) {
        log.info("Registering device for app user: {}", appUser);

        String currentUsername = UserContext.getUsername();
        Instant now = Instant.now();

        Optional<Device> existingDevice = Optional.empty();
        if (request.getDeviceId() != null && !request.getDeviceId().isBlank()) {
            existingDevice = deviceRepositoryWrapper.findByAppUserAndDeviceId(appUser, request.getDeviceId());
        }
        if (existingDevice.isEmpty()) {
            existingDevice = deviceRepositoryWrapper.findByAppUserAndNotificationToken(appUser, request.getNotificationToken());
        }
        // Upsert: update existing or create new

        Device device;
        if (existingDevice.isPresent()) {
            device = existingDevice.get();
            log.info("Updating existing device {} for app user {}", device.getId(), appUser);
            device.setLastRegistrationTokenTime(now);
            device.setIsActive(true);
            if (request.getNotificationToken() != null && !request.getNotificationToken().isBlank()) {
                device.setNotificationToken(request.getNotificationToken());
            }
            if (request.getDeviceId() != null) {
                device.setDeviceId(request.getDeviceId());
            }
            if (request.getAppVersion() != null) {
                device.setAppVersion(request.getAppVersion());
            }
            if (request.getOsVersion() != null) {
                device.setOsVersion(request.getOsVersion());
            }
            if (request.getDeviceModel() != null) {
                device.setDeviceModel(request.getDeviceModel());
            }
            if (request.getApkVersion() != null) {
                device.setApkVersion(request.getApkVersion());
            }
            if (request.getSdkVersion() != null) {
                device.setSdkVersion(request.getSdkVersion());
            }
            if (request.getPlatform() != null) {
                device.setPlatform(Platform.valueOf(request.getPlatform().toUpperCase()));
            }
        } else {
            log.info("Creating new device for app user {}", appUser);
            device = Device.builder()
                    .appUser(appUser)
                    .notificationToken(request.getNotificationToken())
                    .platform(Platform.valueOf(request.getPlatform().toUpperCase()))
                    .deviceId(request.getDeviceId())
                    .appVersion(request.getAppVersion())
                    .osVersion(request.getOsVersion())
                    .deviceModel(request.getDeviceModel())
                    .apkVersion(request.getApkVersion())
                    .sdkVersion(request.getSdkVersion())
                    .isActive(true)
                    .lastRegistrationTokenTime(now)
                    .build();
            device.setCreatedBy(currentUsername != null ? currentUsername : "system");
        }

        device.setUpdatedBy(currentUsername != null ? currentUsername : "system");
        Device saved = deviceRepositoryWrapper.saveWithException(device);

        log.info("Device {} registered/updated successfully for app user {}", saved.getId(), appUser);
    }

    @Override
    @Transactional
    public int deactivateDevicesByTokens(String appUser, List<String> notificationTokens) {
        if (notificationTokens == null || notificationTokens.isEmpty()) {
            return 0;
        }

        log.info("Deactivating {} device(s) by tokens for app user {}", notificationTokens.size(), appUser);

        List<Device> devices = deviceRepositoryWrapper.findByAppUserAndNotificationTokenIn(appUser, notificationTokens);

        if (devices.isEmpty()) {
            log.warn("No devices found for app user {} with provided tokens", appUser);
            return 0;
        }

        String currentUsername = UserContext.getUsername();
        int deactivatedCount = 0;

        for (Device device : devices) {
            if (device.getIsActive()) {
                device.setIsActive(false);
                device.setUpdatedBy(currentUsername != null ? currentUsername : "system");
                deviceRepositoryWrapper.saveWithException(device);
                deactivatedCount++;
                log.debug("Deactivated device {} (token: {}) for app user {}",
                        device.getId(),
                        device.getNotificationToken().substring(0, Math.min(20, device.getNotificationToken().length())) + "...",
                        appUser);
            }
        }

        log.info("Deactivated {} device(s) for app user {}", deactivatedCount, appUser);
        return deactivatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveNotificationTokensForUser(String appUser) {
        log.debug("Getting active notification tokens for app user: {}", appUser);
        return deviceRepositoryWrapper.findActiveNotificationTokensByAppUser(appUser);
    }
}
