package com.nivasafinance.features.leadstages.dto;

import com.nivasafinance.features.leadstages.entity.LeadStageAssignmentHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadStageAssignmentHistoryResponse {
    
    private Long id;
    private String assignedTo;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public static LeadStageAssignmentHistoryResponse from(LeadStageAssignmentHistory assignment) {
        return LeadStageAssignmentHistoryResponse.builder()
                .id(assignment.getId())
                .assignedTo(assignment.getAssignedTo())
                .assignedBy(assignment.getAssignedBy())
                .assignedAt(assignment.getAssignedAt())
                .unassignedAt(assignment.getUnassignedAt())
                .createdAt(assignment.getCreatedAt())
                .createdBy(assignment.getCreatedBy())
                .updatedAt(assignment.getUpdatedAt())
                .updatedBy(assignment.getUpdatedBy())
                .build();
    }
}

