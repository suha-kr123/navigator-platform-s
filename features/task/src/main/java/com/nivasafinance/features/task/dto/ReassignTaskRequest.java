package com.nivasafinance.features.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignTaskRequest {
    
    @NotBlank(message = "Task identifier is required")
    private String taskIdentifier;

    private String newAssignedTo;
    
    private String newAssignedToRole;
}

