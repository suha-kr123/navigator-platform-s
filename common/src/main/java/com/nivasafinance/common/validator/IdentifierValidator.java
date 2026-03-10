package com.nivasafinance.common.validator;

import com.nivasafinance.common.enums.IdentifierType;

/**
 * Validates identifier value format for a given identifier type (e.g. PAN, Aadhaar).
 */
public interface IdentifierValidator {

    IdentifierType getType();

    void validate(String value);
}
