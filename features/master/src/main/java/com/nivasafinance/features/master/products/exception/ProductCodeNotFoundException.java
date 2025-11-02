package com.nivasafinance.features.master.products.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class ProductCodeNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProductCodeNotFoundException(String messageKey, String code, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, new Object[]{code}, messageSource));
    }
}

