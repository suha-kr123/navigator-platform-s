package com.nivasafinance.services.creditbureau.provider.crifhighmark;

/**
 * Thrown when CRIF Stage-II (Authorization) returns status code S11, indicating a data mismatch.
 * The provider should map this to enquiry status DATA_MISMATCH instead of FAILED.
 */
public class CrifDataMismatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CrifDataMismatchException(String message) {
        super(message);
    }

    public CrifDataMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
