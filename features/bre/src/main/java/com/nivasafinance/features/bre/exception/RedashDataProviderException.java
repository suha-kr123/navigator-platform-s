package com.nivasafinance.features.bre.exception;

import java.io.Serial;

public class RedashDataProviderException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -19094762340956216L;

    public RedashDataProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
