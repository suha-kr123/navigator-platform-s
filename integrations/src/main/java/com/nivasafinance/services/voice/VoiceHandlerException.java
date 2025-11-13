package com.nivasafinance.services.voice;

import java.io.Serial;

public class VoiceHandlerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public VoiceHandlerException(String message) {
        super(message);
    }

    public VoiceHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}

