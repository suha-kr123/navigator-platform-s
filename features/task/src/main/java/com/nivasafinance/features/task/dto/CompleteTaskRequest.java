package com.nivasafinance.features.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteTaskRequest {
    
    @NotBlank(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Outcome is required")
    private String outcome;
    
    private Map<String, Object> outcomeDetails;
}

