package com.nivasafinance.services.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCallResponse {
    private String callSid;
    private String status;
    private String fromNumber;
    private String toNumber;
    private Integer duration;
    private String recordingUrl;
    private String errorMessage;
}

