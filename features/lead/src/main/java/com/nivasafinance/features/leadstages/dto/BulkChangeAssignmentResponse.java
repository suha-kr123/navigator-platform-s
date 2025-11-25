package com.nivasafinance.features.leadstages.dto;

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
public class BulkChangeAssignmentResponse {
    
    private int totalRequested;
    private int successful;
    private int failed;
    private List<UUID> successfulLeadIdentifiers;
    private List<BulkAssignmentError> errors;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkAssignmentError {
        private UUID leadIdentifier;
        private String errorMessage;
    }
}

