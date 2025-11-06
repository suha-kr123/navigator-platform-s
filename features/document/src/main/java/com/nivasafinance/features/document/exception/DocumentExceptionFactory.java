package com.nivasafinance.features.document.exception;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.UUID;

public class DocumentExceptionFactory {
    
    private final MessageSource messageSource;
    
    public DocumentExceptionFactory(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public DocumentNotFoundException createNotFoundException(UUID documentId) {
        String message = messageSource.getMessage(
                "error.document.not.found",
                new Object[]{documentId.toString()},
                Locale.getDefault()
        );
        return new DocumentNotFoundException(message);
    }
    
    public DocumentValidationException createValidationException(String reason) {
        String message = messageSource.getMessage(
                "error.document.validation",
                new Object[]{reason},
                Locale.getDefault()
        );
        return new DocumentValidationException(message);
    }
    
    public DocumentOperationException createOperationException(String operation, Throwable cause) {
        String message = messageSource.getMessage(
                "error.document.operation.failed",
                new Object[]{operation},
                Locale.getDefault()
        );
        return new DocumentOperationException(message, cause);
    }
    
    public DocumentOperationException createOperationException(String operation) {
        return createOperationException(operation, null);
    }
    
    public void validateDocumentForCreation(com.nivasafinance.features.document.dto.DocumentCreateRequest createRequest) {
        java.util.ArrayList<String> validationErrors = new java.util.ArrayList<>();
        if (createRequest.getFile() == null || createRequest.getFile().isEmpty()) {
            validationErrors.add("File is required");
        }
        if (createRequest.getName() == null || createRequest.getName().isBlank()) {
            validationErrors.add("Name is required");
        }
        if (!validationErrors.isEmpty()) {
            throw createValidationException(String.join(", ", validationErrors));
        }
    }
}

