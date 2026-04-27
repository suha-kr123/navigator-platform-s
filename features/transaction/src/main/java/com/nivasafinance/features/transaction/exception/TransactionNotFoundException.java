package com.nivasafinance.features.transaction.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class TransactionNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TransactionNotFoundException(UUID identifier, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.transaction.not.found",
                new Object[]{identifier.toString()},
                messageSource
        ));
    }
}
