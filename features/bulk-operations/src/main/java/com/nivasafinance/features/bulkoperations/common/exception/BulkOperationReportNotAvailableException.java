package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

public class BulkOperationReportNotAvailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BulkOperationReportNotAvailableException(String message) {
        super(message);
    }

    public BulkOperationReportNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
