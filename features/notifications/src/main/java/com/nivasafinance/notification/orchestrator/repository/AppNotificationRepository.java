package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {

    Optional<AppNotification> findByProviderMessageId(String providerMessageId);

    Optional<AppNotification> findByReceiptId(UUID receiptId);

    Optional<AppNotification> findByDeviceIdAndReceiptId(Long deviceId, UUID receiptId);
}
