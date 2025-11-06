package framework.core.exception;

import java.io.Serial;

public class ServiceFactoryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceFactoryException(String message) {
        super(message);
    }
}

