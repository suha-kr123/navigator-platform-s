package com.nivasafinance.services.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppTemplateResponse {
    private String messageId;
    private String status;
    private String phoneNumber;
    private String templateName;
    private String deliveredAt;
    private String readAt;
    private String errorMessage;
    
    /**
     * Raw response body from WATI API.
     * Contains full response including receivers array with localMessageId.
     */
    private String rawResponseBody;
}

