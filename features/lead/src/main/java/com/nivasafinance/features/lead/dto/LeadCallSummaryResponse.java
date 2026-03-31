package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.entity.Lead;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCallSummaryResponse {

    private OutboundConnectRate outboundConnectRate;
    private CallTimeHourRange bestTimeToCall;
    private LocalDateTime lastConnectedCallAt;
    private LocalDateTime lastCallAttemptAt;
    private Double averageOutboundTalkDurationSeconds;
    private Integer consecutiveNoAnswers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutboundConnectRate {
        private Integer connected;
        private Integer total;
        private String rate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallTimeHourRange {
        private LocalTime start;
        private LocalTime end;
    }

    public static LeadCallSummaryResponse fromEntity(Lead.CallSummaryDetails details) {
        if (details == null) {
            return empty();
        }
        int total = details.getTotalOutboundCalls() != null ? details.getTotalOutboundCalls() : 0;
        int connected = details.getOutboundConnectedCalls() != null ? details.getOutboundConnectedCalls() : 0;
        return LeadCallSummaryResponse.builder()
                .outboundConnectRate(OutboundConnectRate.builder()
                        .connected(connected)
                        .total(total)
                        .rate(formatRate(connected, total))
                        .build())
                .bestTimeToCall(mapRange(details.getBestTimeToCall()))
                .lastConnectedCallAt(details.getLastConnectedCallAt())
                .lastCallAttemptAt(details.getLastCallAttemptAt())
                .averageOutboundTalkDurationSeconds(details.getAverageOutboundTalkDurationSeconds())
                .consecutiveNoAnswers(details.getConsecutiveNoAnswers())
                .build();
    }

    private static CallTimeHourRange mapRange(Lead.CallTimeHourRange range) {
        if (range == null) {
            return null;
        }
        return CallTimeHourRange.builder()
                .start(range.getStart())
                .end(range.getEnd())
                .build();
    }

    private static String formatRate(int connected, int total) {
        if (total <= 0) {
            return "0%";
        }
        return Math.round(100.0 * connected / total) + "%";
    }

    private static LeadCallSummaryResponse empty() {
        return LeadCallSummaryResponse.builder()
                .outboundConnectRate(OutboundConnectRate.builder()
                        .connected(0)
                        .total(0)
                        .rate("0%")
                        .build())
                .bestTimeToCall(null)
                .lastConnectedCallAt(null)
                .lastCallAttemptAt(null)
                .averageOutboundTalkDurationSeconds(null)
                .consecutiveNoAnswers(0)
                .build();
    }
}
