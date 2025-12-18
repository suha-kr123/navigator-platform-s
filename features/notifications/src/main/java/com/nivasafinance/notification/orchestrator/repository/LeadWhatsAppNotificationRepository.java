package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadWhatsAppNotificationRepository extends JpaRepository<LeadWhatsAppNotification, Long> {
    
    Optional<LeadWhatsAppNotification> findByLocalMessageId(String localMessageId);
    
    Optional<LeadWhatsAppNotification> findByWhatsappMessageId(String whatsappMessageId);
    
    Optional<LeadWhatsAppNotification> findByReceiptId(UUID receiptId);
}

