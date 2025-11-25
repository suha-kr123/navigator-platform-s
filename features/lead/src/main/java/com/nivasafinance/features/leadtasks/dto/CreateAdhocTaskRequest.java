package com.nivasafinance.features.leadtasks.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdhocTaskRequest {

    @NotBlank(message = "Task config key is required")
    private String taskConfigKey;

    private String assignedTo;

    private LocalDateTime dueAt;

    private String stageKey;

    private String creatorRemarks;

    private LocalDateTime preferredCallWindowStart;
    
    private LocalDateTime preferredCallWindowEnd;

}
