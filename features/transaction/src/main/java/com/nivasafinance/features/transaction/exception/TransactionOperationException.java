package com.nivasafinance.features.transaction.exception;

import java.io.Serial;

public class TransactionOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TransactionOperationException(String message) {
        super(message);
    }

    public TransactionOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
