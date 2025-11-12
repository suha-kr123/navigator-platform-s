package com.nivasafinance.services.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCallRequest {
    private String fromNumber;
    private String toNumber;
    private String exophone;
    private String entityName;
    private Long entityId;
    private String callPurpose;
}

