package com.nivasafinance.notification.orchestrator.dto;

import com.nivasafinance.notification.orchestrator.entity.AdvisorWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.LeadWhatsAppNotification;
import com.nivasafinance.notification.orchestrator.entity.NotificationReceipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppNotificationLogResponse {

    private Long id;
    private String templateName;
    private String provider;
    private String toNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> statusTrack;

    public static WhatsAppNotificationLogResponse fromLead(
            LeadWhatsAppNotification notification,
            NotificationReceipt receipt
    ) {
        return build(
                notification.getId(),
                notification.getTemplateName(),
                notification.getWaId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getStatusTrack(),
                receipt
        );
    }

    public static WhatsAppNotificationLogResponse fromAdvisor(
            AdvisorWhatsAppNotification notification,
            NotificationReceipt receipt
    ) {
        return build(
                notification.getId(),
                notification.getTemplateName(),
                notification.getWaId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getStatusTrack(),
                receipt
        );
    }

    private static WhatsAppNotificationLogResponse build(
            Long id,
            String templateName,
            String waId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Map<String, Object> statusTrack,
            NotificationReceipt receipt
    ) {
        String toNumber = null;
        String provider = null;
        if (receipt != null) {
            toNumber = receipt.getRecipientContact();
            provider = receipt.getMode();
        }
        if (toNumber == null || toNumber.isBlank()) {
            toNumber = waId;
        }

        return WhatsAppNotificationLogResponse.builder()
                .id(id)
                .templateName(templateName)
                .provider(provider)
                .toNumber(toNumber)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .statusTrack(statusTrack)
                .build();
    }
}
