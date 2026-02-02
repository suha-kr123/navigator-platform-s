package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

public class BulkOperationNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BulkOperationNotFoundException(String message) {
        super(message);
    }

    public BulkOperationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
