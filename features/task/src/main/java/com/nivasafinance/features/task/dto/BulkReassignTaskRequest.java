package com.nivasafinance.features.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkReassignTaskRequest {
    
    @NotEmpty(message = "Task identifiers are required")
    private List<UUID> taskIdentifiers;

    @NotBlank(message = "New assigned to is required")
    private String newAssignedTo;
}

