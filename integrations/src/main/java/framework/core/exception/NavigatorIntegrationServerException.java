package framework.core.exception;

import java.io.Serial;

public class NavigatorIntegrationServerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NavigatorIntegrationServerException(String message) {
        super(message);
    }
}

