package com.nivasafinance.integrations.framework.core.exception;

import java.io.Serial;

public class HttpClientException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public HttpClientException(String message) {
        super(message);
    }
}

