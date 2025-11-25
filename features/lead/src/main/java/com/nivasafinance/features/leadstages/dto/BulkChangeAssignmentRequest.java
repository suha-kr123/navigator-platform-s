package com.nivasafinance.features.leadstages.dto;

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
public class BulkChangeAssignmentRequest {
    
    @NotEmpty(message = "Lead identifiers are required")
    private List<UUID> leadIdentifiers;

    @NotBlank(message = "Stage key is required")
    private String stageKey;

    @NotBlank(message = "New assigned to is required")
    private String newAssignedTo;
}

