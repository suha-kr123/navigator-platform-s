package com.nivasafinance.features.advisor.dto;

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
public class BulkSalesOwnerAssignmentRequest {
    
    @NotEmpty(message = "Advisor identifiers are required")
    private List<UUID> advisorIdentifiers;

    @NotBlank(message = "Sales owner is required")
    private String salesOwner;
}

