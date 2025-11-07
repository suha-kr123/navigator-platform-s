package com.nivasafinance.features.staff.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;

public class StaffAlreadyExistsException extends StaffConflictException {

    @Serial
    private static final long serialVersionUID = 6248135792438571324L;

    public StaffAlreadyExistsException(Long userId, String officeKey, MessageSource messageSource) {
        super("error.staff.already.exists", new Object[]{userId, officeKey}, messageSource);
    }
}

