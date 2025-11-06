package com.nivasafinance.features.document.exception;

import java.io.Serial;

public class DocumentOperationException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public DocumentOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DocumentOperationException(String message) {
        super(message);
    }
}


