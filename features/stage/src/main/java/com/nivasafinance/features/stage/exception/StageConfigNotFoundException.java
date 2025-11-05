package com.nivasafinance.features.stage.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class StageConfigNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1234567890123456789L;

    private StageConfigNotFoundException(String message) {
        super(message);
    }

    public static StageConfigNotFoundException stageConfigNotFound(String key, MessageSource messageSource) {
        return new StageConfigNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.stage.config.not.found",
                new Object[]{key},
                messageSource
            )
        );
    }
}

