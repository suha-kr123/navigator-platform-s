package com.nivasafinance.features.transaction.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ConflictException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.UUID;

public final class TransactionExceptionFactory {

    private TransactionExceptionFactory() {
    }

    public static TransactionNotFoundException notFound(UUID identifier, MessageSource messageSource) {
        return new TransactionNotFoundException(identifier, messageSource);
    }

    public static ConflictException duplicateTransaction(String idempotencyKey, MessageSource messageSource) {
        return new ConflictException(
                ExceptionUtils.createLocalizedMessage(
                        "error.transaction.duplicate",
                        new Object[]{idempotencyKey},
                        messageSource
                )
        );
    }

    public static BadRequestException invalidStatusTransition(String from, String to, MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage(
                        "error.transaction.invalid.status.transition",
                        new Object[]{from, to},
                        messageSource
                )
        );
    }

    public static BadRequestException paymentDetailsRequired(MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage(
                        "error.transaction.payment.details.required",
                        new Object[]{},
                        messageSource
                )
        );
    }

    public static BadRequestException remarksRequired(MessageSource messageSource) {
        return new BadRequestException(
                ExceptionUtils.createLocalizedMessage(
                        "error.transaction.remarks.required",
                        new Object[]{},
                        messageSource
                )
        );
    }
}
