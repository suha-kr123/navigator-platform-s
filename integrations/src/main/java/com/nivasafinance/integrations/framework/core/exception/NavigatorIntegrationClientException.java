package com.nivasafinance.integrations.framework.core.exception;

import java.io.Serial;

public class NavigatorIntegrationClientException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NavigatorIntegrationClientException(String message) {
        super(message);
    }
}

