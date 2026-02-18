package com.nivasafinance.features.staff.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class StaffNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public StaffNotFoundException(Long userId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.staff.not.found.by.user.id",
                new Object[]{userId},
                messageSource
        ));
    }

    public StaffNotFoundException(UUID identifier, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.staff.not.found.by.identifier",
                new Object[]{identifier.toString()},
                messageSource
        ));
    }
}
