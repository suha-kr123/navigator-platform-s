package com.nivasafinance.features.leadtasks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadCompleteTaskRequest {

    @NotNull(message = "Task identifier is required")
    private UUID taskIdentifier;

    @NotBlank(message = "Outcome is required")
    private String outcomeCodeValueKey;

    private String remarks;
    
}

