package com.nivasafinance.features.offices.exception;

import com.nivasafinance.common.exception.BadRequestException;
import org.springframework.context.MessageSource;

public final class OfficeExceptionFactory {

    private OfficeExceptionFactory() {
    }

    public static BadRequestException duplicateKey(String key) {
        return new BadRequestException(
                String.format("Office with key '%s' already exists.", key));
    }

    public static OfficeOperationException retrieveFailed(MessageSource messageSource) {
        return new OfficeOperationException("error.office.retrieve.failed", messageSource);
    }

    public static OfficeOperationException saveFailed(MessageSource messageSource) {
        return new OfficeOperationException("error.office.save.failed", messageSource);
    }
}
