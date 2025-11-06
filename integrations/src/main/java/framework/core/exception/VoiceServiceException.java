package framework.core.exception;

import java.io.Serial;

public class VoiceServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public VoiceServiceException(String message) {
        super(message);
    }
}

