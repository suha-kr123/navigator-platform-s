package com.nivasafinance.notification.orchestrator.service.impl;

import com.nivasafinance.notification.orchestrator.dto.WhatsAppNotificationLogResponse;
import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import com.nivasafinance.notification.orchestrator.repository.AdvisorWhatsAppNotificationRepository;
import com.nivasafinance.notification.orchestrator.repository.LeadWhatsAppNotificationRepository;
import com.nivasafinance.notification.orchestrator.repository.NotificationReceiptRepository;
import com.nivasafinance.notification.orchestrator.service.WhatsAppNotificationReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhatsAppNotificationReadServiceImpl implements WhatsAppNotificationReadService {

    private final LeadWhatsAppNotificationRepository leadWhatsAppNotificationRepository;
    private final AdvisorWhatsAppNotificationRepository advisorWhatsAppNotificationRepository;
    private final NotificationReceiptRepository notificationReceiptRepository;

    @Override
    public List<WhatsAppNotificationLogResponse> getLeadWhatsAppNotificationLogsByIds(List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return List.of();
        }
        List<LeadWhatsAppNotification> rows = leadWhatsAppNotificationRepository.findAllById(notificationIds);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, NotificationReceipt> receiptById = loadReceiptsById(
                rows.stream()
                        .map(LeadWhatsAppNotification::getReceiptId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
        );
        return rows.stream()
                .map(n -> WhatsAppNotificationLogResponse.fromLead(n, receiptById.get(n.getReceiptId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<WhatsAppNotificationLogResponse> getAdvisorWhatsAppNotificationLogsByIds(List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return List.of();
        }
        List<AdvisorWhatsAppNotification> rows = advisorWhatsAppNotificationRepository.findAllById(notificationIds);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, NotificationReceipt> receiptById = loadReceiptsById(
                rows.stream()
                        .map(AdvisorWhatsAppNotification::getReceiptId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
        );
        return rows.stream()
                .map(n -> WhatsAppNotificationLogResponse.fromAdvisor(n, receiptById.get(n.getReceiptId())))
                .collect(Collectors.toList());
    }

    private Map<UUID, NotificationReceipt> loadReceiptsById(List<UUID> receiptIds) {
        if (receiptIds.isEmpty()) {
            return Map.of();
        }
        List<NotificationReceipt> receipts = notificationReceiptRepository.findAllById(receiptIds);
        return receipts.stream()
                .collect(Collectors.toMap(NotificationReceipt::getId, Function.identity(), (a, b) -> a));
    }
}
