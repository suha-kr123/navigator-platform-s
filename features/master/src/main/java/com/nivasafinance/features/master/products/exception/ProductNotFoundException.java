package com.nivasafinance.features.master.products.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class ProductNotFoundException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public ProductNotFoundException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}

