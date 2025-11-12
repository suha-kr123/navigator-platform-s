package com.nivasafinance.integrations.framework.core.exception;

import java.io.Serial;

public class VoiceHandlerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public VoiceHandlerException(String message) {
        super(message);
    }
}

