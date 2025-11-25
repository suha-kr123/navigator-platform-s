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
public class UpdateDueDateRequest {
    
    @NotNull(message = "Task identifier is required")
    private UUID taskIdentifier;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueAt;
}

