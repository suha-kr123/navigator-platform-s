package com.nivasafinance.features.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotBlank(message = "Task config key is required")
    private String taskConfigKey;

    private String assignedTo;

    private LocalDateTime dueAt;

    private TaskDetailsRequest taskDetails;
}