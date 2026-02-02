package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

public class BulkOperationProcessorNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    BulkOperationProcessorNotFoundException(String message) {
        super(message);
    }
    
}
