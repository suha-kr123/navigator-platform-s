package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

/**
 * Thrown when no report layout is registered for a bulk operation type.
 */
public class BulkOperationReportLayoutNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BulkOperationReportLayoutNotFoundException(String message) {
        super(message);
    }

    public BulkOperationReportLayoutNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
