package com.nivasafinance.features.master.products.exception;

import org.springframework.context.MessageSource;

public class ProductExceptionFactory {

    private ProductExceptionFactory() {
        // Private constructor to prevent instantiation
    }

    public static ProductOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new ProductOperationException("error.product.operation.retrieve", messageSource);
    }

    public static ProductNotFoundException productNotFound(MessageSource messageSource) {
        return new ProductNotFoundException("error.product.not.found", messageSource);
    }

    public static ProductOperationException productAlreadyExists(MessageSource messageSource) {
        return new ProductOperationException("error.product.already.exists", messageSource);
    }

    public static ProductOperationException createEntityFailed(MessageSource messageSource) {
        return new ProductOperationException("error.product.operation.create", messageSource);
    }

    public static ProductOperationException updateEntityFailed(MessageSource messageSource) {
        return new ProductOperationException("error.product.operation.update", messageSource);
    }

    public static ProductOperationException deleteEntityFailed(MessageSource messageSource) {
        return new ProductOperationException("error.product.operation.delete", messageSource);
    }
}

