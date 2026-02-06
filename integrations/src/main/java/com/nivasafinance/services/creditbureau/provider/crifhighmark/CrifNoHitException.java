package com.nivasafinance.services.creditbureau.provider.crifhighmark;

/**
 * Thrown when CRIF Stage-II (Authorization) returns status code S09, indicating no hit.
 * The provider should map this to enquiry status NO_HIT instead of FAILED.
 */
public class CrifNoHitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CrifNoHitException(String message) {
        super(message);
    }

    public CrifNoHitException(String message, Throwable cause) {
        super(message, cause);
    }
}
