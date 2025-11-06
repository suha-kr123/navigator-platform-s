package com.nivasafinance.features.task.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.entity.TaskConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
  
    private String taskIdentifier;

    private String taskConfigKey;

    private String taskName;

    private String taskDescription;

    private String assignedTo;

    private LocalDateTime dueAt;

    private String outcome;

    private OutcomeDetails outcomeDetails;

    private TaskDetails taskDetails;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;

    public static TaskResponse from(Task task, TaskConfig taskConfig, ObjectMapper objectMapper) {
        OutcomeDetails outcomeDetails = null;
        if (task.getOutcomeDetails() != null && !task.getOutcomeDetails().isEmpty()) {
            outcomeDetails = objectMapper.convertValue(task.getOutcomeDetails(), OutcomeDetails.class);
        }
        
        TaskDetails taskDetails = null;
        if (task.getTaskDetails() != null && !task.getTaskDetails().isEmpty()) {
            taskDetails = objectMapper.convertValue(task.getTaskDetails(), TaskDetails.class);
        }
        
        return TaskResponse.builder()
                .taskIdentifier(task.getTaskIdentifier())
                .taskConfigKey(task.getTaskConfigKey())
                .taskName(taskConfig != null ? taskConfig.getName() : null)
                .taskDescription(taskConfig != null ? taskConfig.getDescription() : null)
                .assignedTo(task.getAssignedTo())
                .dueAt(task.getDueAt())
                .outcome(task.getOutcome())
                .outcomeDetails(outcomeDetails)
                .taskDetails(taskDetails)
                .createdAt(task.getCreatedAt())
                .createdBy(task.getCreatedBy())
                .updatedAt(task.getUpdatedAt())
                .updatedBy(task.getUpdatedBy())
                .build();
    }
}

