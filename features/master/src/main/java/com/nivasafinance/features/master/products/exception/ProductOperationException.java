package com.nivasafinance.features.master.products.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public class ProductOperationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public ProductOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.INSTANCE.createLocalizedMessage(messageKey, null, messageSource));
    }
}

