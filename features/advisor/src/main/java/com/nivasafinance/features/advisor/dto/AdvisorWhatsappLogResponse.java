package com.nivasafinance.features.advisor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nivasafinance.features.whatsapp.entity.WhatsappLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdvisorWhatsappLogResponse {

    private UUID identifier;
    private String direction;
    private String senderLabel;
    private String messageType;
    private WhatsappLog.MessageData messageData;
    private WhatsappLog.TemplateDetails templateDetails;
    private String status;
    private String createdSourceType;
    private LocalDateTime messageTime;
}
