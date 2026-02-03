package com.nivasafinance.features.bulkoperations.common.enums;

import lombok.Getter;

@Getter
public enum BulkOperationStatus {
    UPLOADED("File uploaded, waiting for validation"),
    UPLOADED_DRY_RUN("File uploaded (dry-run), waiting for validation"),
    VALIDATION_IN_PROGRESS("CSV validation in progress"),
    VALIDATION_IN_PROGRESS_DRY_RUN("CSV validation in progress (dry-run)"),
    VALIDATED("Validation completed successfully"),
    VALIDATED_DRY_RUN("Validation completed (dry-run), awaiting preview execution"),
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
               this == UPLOADED_DRY_RUN ||
               this == VALIDATION_IN_PROGRESS ||
               this == VALIDATION_IN_PROGRESS_DRY_RUN ||
               this == VALIDATED ||
               this == VALIDATED_DRY_RUN ||
               this == PROCESSING_IN_PROGRESS;
    }

    /** True when this status indicates a dry-run flow (preview mode, no actual domain updates). */
    public boolean isDryRunFlow() {
        return this == UPLOADED_DRY_RUN ||
               this == VALIDATION_IN_PROGRESS_DRY_RUN ||
               this == VALIDATED_DRY_RUN ||
               this == DRY_RUN_COMPLETED;
    }
}
