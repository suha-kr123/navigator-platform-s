package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdvisorWhatsAppNotificationRepositoryWrapper {

    private final AdvisorWhatsAppNotificationRepository advisorWhatsAppNotificationRepository;

    public Page<AdvisorWhatsAppNotification> findByAdvisorIdentifierOrderByIdDesc(
            UUID advisorIdentifier,
            Pageable pageable
    ) {
        return advisorWhatsAppNotificationRepository.findByAdvisorIdentifierOrderByIdDesc(advisorIdentifier, pageable);
    }
}
