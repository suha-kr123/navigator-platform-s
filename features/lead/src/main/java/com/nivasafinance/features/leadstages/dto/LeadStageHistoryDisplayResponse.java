package com.nivasafinance.features.leadstages.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadStageHistoryDisplayResponse {
    private String displayLabel;
    private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
}
