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
public class RescheduleTaskRequest {
    
    @NotBlank(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "New due at is required")
    private LocalDateTime newDueAt;
    
    private String reason;
}

