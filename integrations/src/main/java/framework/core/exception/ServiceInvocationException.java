package framework.core.exception;

import java.io.Serial;

public class ServiceInvocationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceInvocationException(String message) {
        super(message);
    }
}

