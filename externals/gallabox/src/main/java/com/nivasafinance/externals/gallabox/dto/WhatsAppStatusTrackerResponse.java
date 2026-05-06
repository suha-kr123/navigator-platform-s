package com.nivasafinance.externals.gallabox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppStatusTrackerResponse {
    private String whatsappMessageId;
    private String eventType;
    private String eventTimestampIst;
    private boolean updatedInLeadNotification;
    private boolean updatedInAdvisorNotification;
}
