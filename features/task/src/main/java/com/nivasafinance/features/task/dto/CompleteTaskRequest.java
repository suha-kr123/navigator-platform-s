package com.nivasafinance.features.task.dto;

import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteTaskRequest {

    @NotNull(message = "Task identifier is required")
    private UUID taskIdentifier;

    @NotBlank(message = "Outcome is required")
    private String outcomeCodeValueKey;

    private OutcomeDetailsRequest outcomeDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OutcomeDetailsRequest {
        private String remarks;
        private Map<String, Object> locationDetails;
    }
}


