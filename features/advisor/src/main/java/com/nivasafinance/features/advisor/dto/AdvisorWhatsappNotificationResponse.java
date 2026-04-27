package com.nivasafinance.features.advisor.dto;

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
public class AdvisorWhatsappNotificationResponse {
    private WhatsAppNotificationLogResponse whatsappLogDetails;
    private UUID advisorIdentifier;
}
