package com.nivasafinance.features.displayconfig.exception;

import org.springframework.context.MessageSource;

public class DisplayConfigExceptionFactory {

    private DisplayConfigExceptionFactory() {
    }

    public static DisplayConfigNotFoundException notFound(String appType, MessageSource messageSource) {
        return new DisplayConfigNotFoundException(appType, messageSource);
    }

    public static DisplayConfigOperationException retrieveFailed(MessageSource messageSource) {
        return new DisplayConfigOperationException("error.displayconfig.operation.retrieve", messageSource);
    }
}
