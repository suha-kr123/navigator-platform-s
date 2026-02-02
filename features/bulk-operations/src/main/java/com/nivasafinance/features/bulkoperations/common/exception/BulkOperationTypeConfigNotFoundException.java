package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

public class BulkOperationTypeConfigNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    BulkOperationTypeConfigNotFoundException(String message) {
        super(message);
    }
}
