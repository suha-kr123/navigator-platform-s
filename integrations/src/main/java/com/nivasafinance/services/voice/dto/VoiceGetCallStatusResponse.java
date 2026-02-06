package com.nivasafinance.services.voice.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceGetCallStatusResponse {
    private String callId;
    private VoiceStatus status;
    private CallDetails callDetails;
    private Leg from;
    private Leg to;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CallDetails {
        private String sid;
        private String direction;
        private String virtualNumber;
        private String state;
        private String status;
        private String legs;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer totalDuration;
        private Integer totalTalkTime;
        private String appId;
        private String appName;
        private String digits;
        private String campaignId;
        private String leadId;
        private String advisorId;
        private List<Recording> recordings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Recording {
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Leg {
        private VoiceStatus status;
        private String contactUri;
    }
}
