package com.nivasafinance.common.awssecretmanager.exception;

public class AwsSecretManagerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AwsSecretManagerException(String message) {
        super(message);
    }

    public AwsSecretManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}

