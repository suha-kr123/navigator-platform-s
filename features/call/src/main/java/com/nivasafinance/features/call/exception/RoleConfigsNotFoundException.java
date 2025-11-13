package com.nivasafinance.features.call.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class RoleConfigsNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = -2849123749823749827L;

    public RoleConfigsNotFoundException(String role, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.role.configs.not.found",
                new Object[]{role},
                messageSource
        ));
    }
}

