package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

public class BulkOperationNotCompletedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BulkOperationNotCompletedException(String message) {
        super(message);
    }
    
    public BulkOperationNotCompletedException(String message, Throwable cause) {
        super(message, cause);
    }
}
