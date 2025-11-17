package com.nivasafinance.services.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppTemplateRequest {
    private String phoneNumber;
    private String templateName;
    private String broadcastName;
    @Builder.Default
    private List<TemplateParameter> parameters = new ArrayList<>();
}

