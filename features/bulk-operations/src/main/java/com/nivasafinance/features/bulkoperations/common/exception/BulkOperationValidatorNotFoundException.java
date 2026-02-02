package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

/**
 * Thrown when no CSV validator is registered for a bulk operation type.
 */
public class BulkOperationValidatorNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BulkOperationValidatorNotFoundException(String message) {
        super(message);
    }

    public BulkOperationValidatorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
