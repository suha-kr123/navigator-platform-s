package framework.core.exception;

import java.io.Serial;

public class ServiceConfigurationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceConfigurationException(String message) {
        super(message);
    }
}

