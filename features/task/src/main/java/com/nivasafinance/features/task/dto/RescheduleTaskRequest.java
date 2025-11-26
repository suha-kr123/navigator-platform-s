package com.nivasafinance.features.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleTaskRequest {
    
    @NotNull(message = "Task ID is required")
    private UUID taskIdentifier;

    @NotNull(message = "Preferred start time is required")
    private LocalDateTime preferredStartTime;
    
    @NotNull(message = "Preferred end time is required")
    private LocalDateTime preferredEndTime;
    
    private String reasonCodeValueKey;
    
    private String creatorRemarks;
}

