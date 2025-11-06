package framework.core.exception;

import java.io.Serial;

public class PhoneNumberValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PhoneNumberValidationException(String message) {
        super(message);
    }
}

