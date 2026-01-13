package com.nivasafinance.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

