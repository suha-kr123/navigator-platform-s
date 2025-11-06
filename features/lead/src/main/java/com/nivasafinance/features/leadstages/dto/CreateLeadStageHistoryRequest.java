package com.nivasafinance.features.leadstages.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeadStageHistoryRequest {
    private Long leadId;
    private String previousStageKey;
    private String stageKey;
    private String assignedTo;
    private String remarks;
}
