package com.nivasafinance.features.workflow.dto;

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
public class ExecutePendingActionRequest {

    @NotNull(message = "Action identifier is required")
    private UUID actionIdentifier;

    private String assignTo;

    private LocalDateTime dueDate;
}
