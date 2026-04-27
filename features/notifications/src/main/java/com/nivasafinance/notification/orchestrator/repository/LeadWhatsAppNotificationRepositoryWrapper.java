package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadWhatsAppNotificationRepositoryWrapper {

    private final LeadWhatsAppNotificationRepository leadWhatsAppNotificationRepository;

    public Page<LeadWhatsAppNotification> findByLeadIdentifierOrderByIdDesc(
            UUID leadIdentifier,
            Pageable pageable
    ) {
        return leadWhatsAppNotificationRepository.findByLeadIdentifierOrderByIdDesc(leadIdentifier, pageable);
    }
}
