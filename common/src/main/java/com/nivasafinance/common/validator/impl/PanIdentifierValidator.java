package com.nivasafinance.common.validator.impl;

import com.nivasafinance.common.enums.IdentifierType;
import com.nivasafinance.common.exception.ValidationException;
import com.nivasafinance.common.validator.IdentifierValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.regex.Pattern;

/**
 * Validates Indian PAN format: 5 letters (A-Z) + 4 digits + 1 letter (A-Z).
 */
public class PanIdentifierValidator implements IdentifierValidator {

    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");

    private static final String MESSAGE_KEY = "error.identifier.pan.invalid";

    private final MessageSource messageSource;

    public PanIdentifierValidator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public IdentifierType getType() {
        return IdentifierType.PAN;
    }

    @Override
    public void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(messageSource.getMessage(MESSAGE_KEY, null, LocaleContextHolder.getLocale()));
        }
        String normalized = value.trim().toUpperCase();
        if (!PAN_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException(messageSource.getMessage(MESSAGE_KEY, null, LocaleContextHolder.getLocale()));
        }
    }
}
