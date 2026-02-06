package com.nivasafinance.services.creditbureau;

import java.io.Serial;

public class CreditBureauHandlerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CreditBureauHandlerException(String message) {
        super(message);
    }

    public CreditBureauHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}

