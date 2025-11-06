package com.nivasafinance.features.task.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.task.entity.TaskConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskConfigResponse {
    private Long id;
    private String taskConfigKey;
    private String name;
    private String description;
    private TaskConfigDetails taskConfigDetails;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskConfigDetails {
        private List<CodeValueResponse> outcomes;
        private Boolean rescheduledAllowed;
        private List<CodeValueResponse> rescheduleReasons;
    }

    public static TaskConfigResponse from(TaskConfig taskConfig, ObjectMapper objectMapper) {
        TaskConfigDetails details = null;
        if (taskConfig.getTaskConfigDetails() != null && !taskConfig.getTaskConfigDetails().isEmpty()) {
            details = objectMapper.convertValue(
                taskConfig.getTaskConfigDetails(), 
                new TypeReference<TaskConfigDetails>() {}
            );
        }
        
        return TaskConfigResponse.builder()
                .id(taskConfig.getId())
                .taskConfigKey(taskConfig.getTaskConfigKey())
                .name(taskConfig.getName())
                .description(taskConfig.getDescription())
                .taskConfigDetails(details)
                .isActive(taskConfig.getIsActive())
                .createdAt(taskConfig.getCreatedAt())
                .createdBy(taskConfig.getCreatedBy())
                .updatedAt(taskConfig.getUpdatedAt())
                .updatedBy(taskConfig.getUpdatedBy())
                .build();
    }
}

