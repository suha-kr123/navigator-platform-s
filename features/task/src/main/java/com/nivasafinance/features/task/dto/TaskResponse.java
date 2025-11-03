package com.nivasafinance.features.task.dto;

import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.entity.TaskConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
  
    private Long id;

    private String taskConfigKey;

    private String taskName;

    private String taskDescription;

    private String assignedTo;

    private String assignedToRole;

    private LocalDateTime dueAt;

    private String outcome;

    private Map<String, Object> outcomeDetails;

    private Map<String, Object> taskDetails;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;

    /**
     * Factory method to create TaskResponse from Task and TaskConfig entities
     * 
     * @param task the task entity
     * @param taskConfig the task config entity
     * @return TaskResponse
     */
    public static TaskResponse from(Task task, TaskConfig taskConfig) {
        return TaskResponse.builder()
                .id(task.getId())
                .taskConfigKey(task.getTaskConfigKey())
                .taskName(taskConfig != null ? taskConfig.getName() : null)
                .taskDescription(taskConfig != null ? taskConfig.getDescription() : null)
                .assignedTo(task.getAssignedTo())
                .assignedToRole(task.getAssignedToRole())
                .dueAt(task.getDueAt())
                .outcome(task.getOutcome())
                .outcomeDetails(task.getOutcomeDetails())
                .taskDetails(task.getTaskDetails())
                .createdAt(task.getCreatedAt())
                .createdBy(task.getCreatedBy())
                .updatedAt(task.getUpdatedAt())
                .updatedBy(task.getUpdatedBy())
                .build();
    }
}

