package com.nivasafinance.features.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
  
    private UUID taskIdentifier;

    private String taskConfigKey;

    private String taskName;

    private String taskDescription;

    private String assignedTo;

    private LocalDateTime dueAt;

    private String outcome;

    private OutcomeDetailsResponse outcomeDetails;

    private TaskDetailsResponse taskDetails;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;
}

