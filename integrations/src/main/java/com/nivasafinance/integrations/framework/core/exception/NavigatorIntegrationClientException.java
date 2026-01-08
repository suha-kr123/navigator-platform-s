package com.nivasafinance.integrations.framework.core.exception;

import com.nivasafinance.common.exception.BadRequestException;

import java.io.Serial;

public class NavigatorIntegrationClientException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NavigatorIntegrationClientException(String message) {
        super(message);
    }
}

