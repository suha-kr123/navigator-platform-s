package com.nivasafinance.features.document.exception;

import java.io.Serial;

public class DocumentConflictException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public DocumentConflictException(String message) {
        super(message);
    }
}


