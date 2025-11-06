package com.nivasafinance.features.master.codemaster.exception;

import org.springframework.context.MessageSource;

public class CodeMasterExceptionFactory {
    
    public CodeMasterExceptionFactory(MessageSource messageSource) {
    }
    
    public CodeMasterNotFoundException notFound(String codeName, MessageSource messageSource) {
        return new CodeMasterNotFoundException(codeName, messageSource);
    }
    
    public CodeValueKeyNotFoundException codeValueKeyNotFound(String key, MessageSource messageSource) {
        return new CodeValueKeyNotFoundException(key, messageSource);
    }
    
    public CodeMasterNotFoundException notFoundById(java.util.UUID id, MessageSource messageSource) {
        return new CodeMasterNotFoundException("ID: " + id, messageSource);
    }
    
    public CodeMasterOperationException createFailed(MessageSource messageSource) {
        return new CodeMasterOperationException("error.codemaster.operation.create.failed", messageSource);
    }
    
    public CodeMasterOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new CodeMasterOperationException("error.codemaster.operation.retrieve.failed", messageSource);
    }
    
    public CodeMasterOperationException updateFailed(MessageSource messageSource) {
        return new CodeMasterOperationException("error.codemaster.operation.update.failed", messageSource);
    }
    
    public CodeMasterOperationException deleteFailed(MessageSource messageSource) {
        return new CodeMasterOperationException("error.codemaster.operation.delete.failed", messageSource);
    }
}

