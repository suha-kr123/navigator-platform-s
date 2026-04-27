package com.nivasafinance.features.lead.dto;

import com.nivasafinance.notification.orchestrator.dto.WhatsAppNotificationLogResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadWhatsappNotificationResponse {
    private WhatsAppNotificationLogResponse whatsappLogDetails;
    private UUID leadIdentifier;
}
