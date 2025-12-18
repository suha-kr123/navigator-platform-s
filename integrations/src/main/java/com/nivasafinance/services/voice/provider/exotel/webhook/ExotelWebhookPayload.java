package com.nivasafinance.services.voice.provider.exotel.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExotelWebhookPayload {
    
    private String callSid;
    private String callFrom;
    private String callTo;
    private String callStatus;
    private String direction;
    private String created;
    private String from;
    private String to;
    private String currentTime;
    private String dialWhomNumber;
    private String eventType;
    private String customField;
    private String agentEmail;
    private String status;
}

