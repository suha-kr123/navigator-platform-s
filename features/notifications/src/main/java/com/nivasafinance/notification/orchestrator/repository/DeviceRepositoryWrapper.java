package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.notification.orchestrator.entity.Device;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class DeviceRepositoryWrapper {

    private final DeviceRepository deviceRepository;
    private final MessageSource messageSource;

    /** Save device; throws with localized message on failure. */
    public Device saveWithException(Device device) {
        try {
            return deviceRepository.save(device);
        } catch (DataAccessException e) {
            throw new IllegalStateException(
                    ExceptionUtils.createLocalizedMessage("error.device.operation.create", null, messageSource), e);
        }
    }

    public Optional<Device> findByAppUserAndDeviceId(String appUser, String deviceId) {
        return deviceRepository.findByAppUserAndDeviceId(appUser, deviceId);
    }

    public Optional<Device> findByAppUserAndNotificationToken(String appUser, String notificationToken) {
        return deviceRepository.findByAppUserAndNotificationToken(appUser, notificationToken);
    }

    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    public List<Device> findByAppUserAndNotificationTokenIn(String appUser, List<String> notificationTokens) {
        return deviceRepository.findByAppUserAndNotificationTokenIn(appUser, notificationTokens);
    }

    public List<String> findActiveNotificationTokensByAppUser(String appUser) {
        return deviceRepository.findActiveNotificationTokensByAppUser(appUser);
    }
}
