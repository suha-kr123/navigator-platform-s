package com.nivasafinance.features.staff.exception;

import org.springframework.context.MessageSource;

public final class StaffExceptionFactory {

    private StaffExceptionFactory() {
        // Utility class
    }

    public static StaffAlreadyExistsException alreadyExists(Long userId, String officeKey, MessageSource messageSource) {
        return new StaffAlreadyExistsException(userId, officeKey, messageSource);
    }
}

