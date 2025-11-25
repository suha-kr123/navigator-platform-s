package com.nivasafinance.features.leadtasks.dto;

import jakarta.validation.constraints.NotNull;
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
public class LeadRescheduleTaskRequest {
    
    @NotNull(message = "Task ID is required")
    private UUID taskIdentifier;

    @NotNull(message = "Preferred start time is required")
    private LocalDateTime preferredStartTime;
    
    @NotNull(message = "Preferred end time is required")
    private LocalDateTime preferredEndTime;
    
    private String reasonCodeValueKey;
}

