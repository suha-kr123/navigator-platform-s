package com.nivasafinance.services.voice.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for call notifications returned to frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallNotificationResponse {
    
    private String callSid;
    private String callFrom;
    private String callTo;
    private String callStatus;
    private String direction;
    private String eventType;
    private String agentEmail;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
}

