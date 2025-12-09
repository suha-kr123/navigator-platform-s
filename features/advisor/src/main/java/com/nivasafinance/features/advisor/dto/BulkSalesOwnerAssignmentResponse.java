package com.nivasafinance.features.advisor.dto;

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
public class BulkSalesOwnerAssignmentResponse {
    
    private int totalRequested;
    private int successful;
    private int failed;
    private List<UUID> successfulAdvisorIdentifiers;
    private List<AssignmentError> errors;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignmentError {
        private UUID advisorIdentifier;
        private String errorMessage;
    }
}

