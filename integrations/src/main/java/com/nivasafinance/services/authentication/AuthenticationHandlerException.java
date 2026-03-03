package com.nivasafinance.services.authentication;

import java.io.Serial;

public class AuthenticationHandlerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationHandlerException(String message) {
        super(message);
    }

    public AuthenticationHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
