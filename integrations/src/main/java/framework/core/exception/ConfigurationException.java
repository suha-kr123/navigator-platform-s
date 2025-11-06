package framework.core.exception;

import java.io.Serial;

public class ConfigurationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super(message);
    }
}

