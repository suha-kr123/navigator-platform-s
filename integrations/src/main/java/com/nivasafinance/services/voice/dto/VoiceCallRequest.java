package com.nivasafinance.services.voice.dto;

import com.nivasafinance.common.enums.SystemEntities;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceCallRequest {
    private String fromNumber;
    private String toNumber;
    private String callerId;
    private CallBackData callBackData;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CallBackData{
        private SystemEntities entityType;
        private String identifier;
    }
}

