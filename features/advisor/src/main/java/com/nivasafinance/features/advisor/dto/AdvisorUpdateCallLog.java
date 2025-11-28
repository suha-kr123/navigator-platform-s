package com.nivasafinance.features.advisor.dto;

import com.nivasafinance.features.call.enums.CallStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvisorUpdateCallLog {
    private CallStatus status;
    private RecordingDetails recordingDetails;
    private CompletionDetails completionDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecordingDetails {
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompletionDetails {
        private Long duration;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<CompletionLeg> legs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompletionLeg {
        private String duration;
        private CallStatus status;
    }
}
