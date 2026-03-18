package com.nivasafinance.features.advisor.dto.self;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvisorSelfLeadStageHistoryResponse {
    private String externalStage;
    private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
}
