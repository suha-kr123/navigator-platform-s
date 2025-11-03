package com.nivasafinance.features.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleTaskRequest {
    
    @NotNull(message = "Task ID is required")
    private String taskIdentifier;

    @NotNull(message = "New due at is required")
    private LocalDateTime newDueAt;
    
    private String reason;
}

