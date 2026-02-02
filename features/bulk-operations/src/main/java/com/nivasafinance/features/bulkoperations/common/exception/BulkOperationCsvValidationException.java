package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

import com.nivasafinance.common.exception.ValidationException;

public class BulkOperationCsvValidationException extends ValidationException {
    
    @Serial
    private static final long serialVersionUID = 1L;

    public BulkOperationCsvValidationException(String message) {
        super(message);
    }

    public BulkOperationCsvValidationException(String message, Throwable cause) {
        super(message + (cause != null ? " Caused by: " + cause.getMessage() : ""));
    }
}