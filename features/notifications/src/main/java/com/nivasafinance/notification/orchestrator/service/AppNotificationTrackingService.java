package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.entity.Device;
import com.nivasafinance.notification.orchestrator.entity.AppNotification;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.enums.AppNotificationStatus;
import com.nivasafinance.notification.orchestrator.repository.DeviceRepositoryWrapper;
import com.nivasafinance.notification.orchestrator.repository.AppNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/** Saves app notification tracking (sent/failed) for FCM receipts. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppNotificationTrackingService {

    private final AppNotificationRepository appNotificationRepository;
    private final DeviceRepositoryWrapper deviceRepositoryWrapper;

    @Transactional
    public void saveNotificationTracking(NotificationReceipt receipt, String notificationToken,
                                         String providerMessageId, String templateName) {
        try {
            log.debug("Saving app notification tracking for receipt: {}, notificationToken: {}, messageId: {}",
                    receipt.getId(), notificationToken != null ? notificationToken.substring(0, Math.min(20, notificationToken.length())) + "..." : "null",
                    providerMessageId);

            String appUser = extractAppUser(receipt);
            if (appUser == null) {
                log.warn("Cannot save app notification tracking - app user not found in receipt {}", receipt.getId());
                return;
            }

            Optional<Device> deviceOpt = deviceRepositoryWrapper.findByAppUserAndNotificationToken(appUser, notificationToken);
            if (deviceOpt.isEmpty()) {
                log.warn("Device not found for notification token in receipt {}. Cannot save tracking record.", receipt.getId());
                return;
            }

            Device device = deviceOpt.get();
            AppNotification notification = AppNotification.builder()
                    .deviceId(device.getId())
                    .receiptId(receipt.getId())
                    .notificationRecordId(receipt.getNotificationRecordId())
                    .notificationToken(notificationToken)
                    .templateId(receipt.getTemplateIdentifier())
                    .templateName(templateName)
                    .providerMessageId(providerMessageId)
                    .status(AppNotificationStatus.SENT)
                    .sentTimestamp(Instant.now())
                    .build();
            notification.setCreatedBy("system");
            notification.setUpdatedBy("system");

            appNotificationRepository.save(notification);
            log.debug("Saved app notification tracking record with ID: {}", notification.getId());

        } catch (Exception ex) {
            log.error("Failed to save app notification tracking for receipt {}", receipt.getId(), ex);
        }
    }

    @Transactional
    public void saveFailedNotificationTracking(NotificationReceipt receipt, String notificationToken,
                                               String errorCode, String errorMessage, String templateName) {
        try {
            log.debug("Saving failed app notification tracking for receipt: {}, notificationToken: {}, errorCode: {}",
                    receipt.getId(), notificationToken != null ? notificationToken.substring(0, Math.min(20, notificationToken.length())) + "..." : "null",
                    errorCode);

            String appUser = extractAppUser(receipt);
            if (appUser == null) {
                log.warn("Cannot save app notification tracking - app user not found in receipt {}", receipt.getId());
                return;
            }

            Optional<Device> deviceOpt = deviceRepositoryWrapper.findByAppUserAndNotificationToken(appUser, notificationToken);
            if (deviceOpt.isEmpty()) {
                log.warn("Device not found for notification token in receipt {}. Cannot save tracking record.", receipt.getId());
                return;
            }

            Device device = deviceOpt.get();
            AppNotification notification = AppNotification.builder()
                    .deviceId(device.getId())
                    .receiptId(receipt.getId())
                    .notificationRecordId(receipt.getNotificationRecordId())
                    .notificationToken(notificationToken)
                    .templateId(receipt.getTemplateIdentifier())
                    .templateName(templateName)
                    .providerMessageId(null)
                    .status(AppNotificationStatus.FAILED)
                    .errorCode(errorCode)
                    .errorMessage(errorMessage)
                    .sentTimestamp(Instant.now())
                    .build();
            notification.setCreatedBy("system");
            notification.setUpdatedBy("system");

            appNotificationRepository.save(notification);
            log.debug("Saved failed app notification tracking record with ID: {}", notification.getId());

        } catch (Exception ex) {
            log.error("Failed to save failed app notification tracking for receipt {}", receipt.getId(), ex);
        }
    }

    private String extractAppUser(NotificationReceipt receipt) {
        if (receipt.getDetails() != null) {
            Object appUserObj = receipt.getDetails().get("appUser");
            if (appUserObj != null) {
                return appUserObj.toString();
            }
        }

        if (receipt.getMessagePayload() != null) {
            Object appUserObj = receipt.getMessagePayload().get("appUser");
            if (appUserObj != null) {
                return appUserObj.toString();
            }
        }

        return null;
    }
}
