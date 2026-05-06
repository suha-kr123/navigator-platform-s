package com.nivasafinance.features.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffTodayActivityResponse {

    private LocalDate date;
    private LocalDateTime firstCallAt;
    private LocalDateTime lastCallAt;
    private CallStats inbound;
    private CallStats outbound;
    private long stageMoves;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallStats {
        private long total;
        private long connected;
        private long noAnswer;
        private long busy;
        private long failed;
        private long totalConnectTimeSeconds;
        private long avgConnectTimeSeconds;
    }
}
