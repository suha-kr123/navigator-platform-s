package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdvisorWhatsAppNotificationRepository extends JpaRepository<AdvisorWhatsAppNotification, Long> {
    
    Optional<AdvisorWhatsAppNotification> findByLocalMessageId(String localMessageId);
    
    Optional<AdvisorWhatsAppNotification> findByWhatsappMessageId(String whatsappMessageId);
    
    Optional<AdvisorWhatsAppNotification> findByReceiptId(UUID receiptId);
}

