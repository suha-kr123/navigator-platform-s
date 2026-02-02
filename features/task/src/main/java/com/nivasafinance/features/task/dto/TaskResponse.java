package com.nivasafinance.features.task.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.entity.TaskConfig;
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
  
    private Long id;
    
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
    
    /**
     * Context information about the entity associated with this task.
     * Contains entity type, identifier, and additional entity-specific data.
     */
    private EntityContextResponse entityContext;

    public static TaskResponse from(Task task, TaskConfig taskConfig, ObjectMapper objectMapper) {
        OutcomeDetailsResponse outcomeDetails = null;
        if (com.nivasafinance.common.utils.ValidationUtils.isNonNull(task.getOutcomeDetails())) {
            outcomeDetails = OutcomeDetailsResponse.builder()
                    .remarks(task.getOutcomeDetails().getRemarks())
                    .completedAt(task.getOutcomeDetails().getCompletedAt())
                    .completedBy(task.getOutcomeDetails().getCompletedBy())
                    .rescheduleReasonCodeValueKey(task.getOutcomeDetails().getRescheduleReasonCodeValueKey())
                    .locationDetails(task.getOutcomeDetails().getLocationDetails())
                    .build();
        }
        
        TaskDetailsResponse taskDetails = null;
        if (com.nivasafinance.common.utils.ValidationUtils.isNonNull(task.getTaskDetails())) {
            TaskDetailsResponse.PreferredCallWindow preferredCallWindow = null;
            if (com.nivasafinance.common.utils.ValidationUtils.isNonNull(task.getTaskDetails().getPreferredCallWindow())) {
                preferredCallWindow = TaskDetailsResponse.PreferredCallWindow.builder()
                        .start(task.getTaskDetails().getPreferredCallWindow().getStart())
                        .end(task.getTaskDetails().getPreferredCallWindow().getEnd())
                        .build();
            }
            taskDetails = TaskDetailsResponse.builder()
                    .entityId(task.getTaskDetails().getEntityId())
                    .entityType(task.getTaskDetails().getEntityType())
                    .preferredCallWindow(preferredCallWindow)
                    .creatorRemarks(task.getTaskDetails().getCreatorRemarks())
                    .iterationCount(task.getTaskDetails().getIterationCount())
                    .rescheduledFromTaskIdentifier(task.getTaskDetails().getRescheduledFromTaskIdentifier())
                    .rescheduleReasonCodeValueKey(task.getTaskDetails().getRescheduleReasonCodeValueKey())
                    .rescheduledFromTaskRemarks(task.getTaskDetails().getRescheduledFromTaskRemarks())
                    .build();
        }
        
        // Prioritize task.name if present, otherwise fallback to taskConfig.name
        String taskName = com.nivasafinance.common.utils.ValidationUtils.isNonNull(task.getName()) 
                ? task.getName() 
                : (com.nivasafinance.common.utils.ValidationUtils.isNonNull(taskConfig) ? taskConfig.getName() : null);
        
        return TaskResponse.builder()
                .id(task.getId())
                .taskIdentifier(task.getTaskIdentifier())
                .taskConfigKey(task.getTaskConfigKey())
                .taskName(taskName)
                .taskDescription(com.nivasafinance.common.utils.ValidationUtils.isNonNull(taskConfig) ? taskConfig.getDescription() : null)
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

