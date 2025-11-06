package com.nivasafinance.features.lender.lenderoffice.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class LenderOfficeExceptionFactory {

    public static LenderOfficeOperationException createFailed(MessageSource messageSource) {
        return new LenderOfficeOperationException("error.lender.office.operation.failed", messageSource);
    }

    public static LenderOfficeNotFoundException lenderOfficeNotFound(UUID id, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.lender.office.not.found",
                new Object[]{id.toString()},
                messageSource
        );
        return new LenderOfficeNotFoundException(message);
    }

    public static LenderOfficeNotFoundException lenderOfficeKeyNotFound(String key, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.lender.office.key.not.found",
                new Object[]{key},
                messageSource
        );
        return new LenderOfficeNotFoundException(message);
    }
}

