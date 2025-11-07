package com.nivasafinance.features.staff.exception;

import com.nivasafinance.common.exception.ConflictException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class StaffConflictException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 6219345872134956213L;

    public StaffConflictException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }
}

