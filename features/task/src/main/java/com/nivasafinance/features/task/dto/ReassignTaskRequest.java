package com.nivasafinance.features.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignTaskRequest {
    
    @NotNull(message = "Task identifier is required")
    private UUID taskIdentifier;

    private String newAssignedTo;
}

