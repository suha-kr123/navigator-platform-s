package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

public class BulkOperationNotCancellableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BulkOperationNotCancellableException(String message) {
        super(message);
    }

    public BulkOperationNotCancellableException(String message, Throwable cause) {
        super(message, cause);
    }
}
