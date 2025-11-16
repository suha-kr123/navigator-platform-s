package com.nivasafinance.features.rolemanagement.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public final class RoleManagementExceptionFactory {
    
    private RoleManagementExceptionFactory() {
    }
    
    public static RoleManagementNotFoundException notFound(String entityKey, Long id, MessageSource messageSource) {
        String messageKey = "error.rolemanagement." + entityKey + ".not.found";
        String message = ExceptionUtils.createLocalizedMessage(messageKey, new Object[]{id.toString()}, messageSource);
        return new RoleManagementNotFoundException(message);
    }
}

