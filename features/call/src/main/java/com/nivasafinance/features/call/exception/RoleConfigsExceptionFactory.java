package com.nivasafinance.features.call.exception;

import org.springframework.context.MessageSource;

public final class RoleConfigsExceptionFactory {

    private RoleConfigsExceptionFactory() {
        // Utility class
    }

    public static RoleConfigsNotFoundException notFound(String role, MessageSource messageSource) {
        return new RoleConfigsNotFoundException(role, messageSource);
    }
}

