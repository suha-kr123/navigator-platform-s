package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

/**
 * Thrown when duplicate validators, processors, or report layouts are registered for the same operation type.
 */
public class BulkOperationDuplicateConfigurationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BulkOperationDuplicateConfigurationException(String message) {
        super(message);
    }

    public BulkOperationDuplicateConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
