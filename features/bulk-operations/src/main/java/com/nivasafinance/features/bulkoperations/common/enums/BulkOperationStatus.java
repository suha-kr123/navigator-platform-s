package com.nivasafinance.features.bulkoperations.common.enums;

import lombok.Getter;

@Getter
public enum BulkOperationStatus {
    UPLOADED("File uploaded, waiting for validation"),
    VALIDATION_IN_PROGRESS("CSV validation in progress"),
    VALIDATED("Validation completed successfully"),
    VALIDATION_FAILED("Validation failed"),
    PROCESSING_IN_PROGRESS("Processing rows"),
    COMPLETED("All rows processed successfully"),
    PARTIALLY_COMPLETED("Some rows failed during processing"),
    FAILED("Processing failed"),
    CANCELLED("Job was cancelled"),
    DRY_RUN_COMPLETED("Dry-run completed (no actual updates)");

    private final String description;

    BulkOperationStatus(String description) {
        this.description = description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || 
               this == PARTIALLY_COMPLETED || 
               this == FAILED || 
               this == CANCELLED || 
               this == VALIDATION_FAILED ||
               this == DRY_RUN_COMPLETED;
    }

    public boolean canCancel() {
        return this == UPLOADED || 
               this == VALIDATION_IN_PROGRESS || 
               this == VALIDATED || 
               this == PROCESSING_IN_PROGRESS;
    }
}
