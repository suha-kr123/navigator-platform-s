package com.nivasafinance.features.bulkoperations.common.enums;

public enum RowProcessingStatus {
    PENDING,  // Validated but not yet processed
    SUCCESS,
    FAILED,
    SKIPPED,
    CANCELLED  // Row was cancelled when operation was cancelled
}
