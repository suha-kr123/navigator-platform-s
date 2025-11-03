package com.nivasafinance.features.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignTaskRequest {
    
    @NotBlank(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "New assigned to is required")
    private String newAssignedTo;
    
    private String newAssignedToRole;
}

