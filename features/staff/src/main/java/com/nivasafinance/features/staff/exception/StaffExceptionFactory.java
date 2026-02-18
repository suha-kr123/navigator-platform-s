package com.nivasafinance.features.staff.exception;

import org.springframework.context.MessageSource;

import java.util.UUID;

public final class StaffExceptionFactory {

    private StaffExceptionFactory() {
        // Utility class
    }

    public static StaffAlreadyExistsException alreadyExists(Long userId, String officeKey, MessageSource messageSource) {
        return new StaffAlreadyExistsException(userId, officeKey, messageSource);
    }

    public static StaffOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new StaffOperationException("error.staff.retrieve.entity.failed", messageSource);
    }

    public static StaffOperationException saveFailed(MessageSource messageSource) {
        return new StaffOperationException("error.staff.save.failed", messageSource);
    }

    public static StaffOperationException noCurrentUser(MessageSource messageSource) {
        return new StaffOperationException("error.staff.no.current.user", messageSource);
    }

    public static StaffNotFoundException notFoundByUserId(Long userId, MessageSource messageSource) {
        return new StaffNotFoundException(userId, messageSource);
    }

    public static StaffNotFoundException notFoundByIdentifier(UUID identifier, MessageSource messageSource) {
        return new StaffNotFoundException(identifier, messageSource);
    }
}

