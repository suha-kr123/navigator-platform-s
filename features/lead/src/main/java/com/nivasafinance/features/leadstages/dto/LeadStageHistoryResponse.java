package com.nivasafinance.features.leadstages.dto;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadStageHistoryResponse {
    
    private Long id;
    private Long leadId;
    private String stageKey;
    private String stageFrom;
    private String subStageKey;
    private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
    private String movedBy;
    private String remarks;
    private List<LeadStageAssignmentHistoryResponse> assignmentHistory;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public static LeadStageHistoryResponse from(LeadStageHistory leadStageHistory) {
        List<LeadStageAssignmentHistoryResponse> assignmentHistoryResponses = ValidationUtils.isNonNull(leadStageHistory.getAssignmentHistory())
                ? leadStageHistory.getAssignmentHistory().stream()
                        .map(LeadStageAssignmentHistoryResponse::from)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return LeadStageHistoryResponse.builder()
                .id(leadStageHistory.getId())
                .leadId(leadStageHistory.getLeadId())
                .stageKey(leadStageHistory.getStageKey())
                .stageFrom(leadStageHistory.getStageFrom())
                .subStageKey(leadStageHistory.getSubStageKey())
                .enteredAt(leadStageHistory.getEnteredAt())
                .exitedAt(leadStageHistory.getExitedAt())
                .movedBy(leadStageHistory.getMovedBy())
                .remarks(leadStageHistory.getRemarks())
                .assignmentHistory(assignmentHistoryResponses)
                .createdAt(leadStageHistory.getCreatedAt())
                .createdBy(leadStageHistory.getCreatedBy())
                .updatedAt(leadStageHistory.getUpdatedAt())
                .updatedBy(leadStageHistory.getUpdatedBy())
                .build();
    }
}

