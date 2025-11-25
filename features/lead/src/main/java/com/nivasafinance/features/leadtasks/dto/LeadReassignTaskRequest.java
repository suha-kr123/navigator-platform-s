package com.nivasafinance.features.leadtasks.dto;

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
public class LeadReassignTaskRequest {
    
    @NotNull(message = "Task identifier is required")
    private UUID taskIdentifier;

    private String newAssignedTo;
}

