package com.nivasafinance.services.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateLogRequest {
    private String messageId;
    private String phoneNumber;
    private String templateName;
    @Builder.Default
    private String status = "sent";
    private String broadcastName;
}

