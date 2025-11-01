package com.nivasafinance.features.master.codemaster.exception

import org.springframework.context.MessageSource

object CodeMasterExceptionFactory {

    fun notFound(codeName: String, messageSource: MessageSource): CodeMasterNotFoundException {
        return CodeMasterNotFoundException(codeName, messageSource)
    }

    fun codeValueKeyNotFound(key: String, messageSource: MessageSource): CodeValueKeyNotFoundException {
        return CodeValueKeyNotFoundException(key, messageSource)
    }

    fun notFoundById(id: java.util.UUID, messageSource: MessageSource): CodeMasterNotFoundException {
        return CodeMasterNotFoundException("ID: $id", messageSource)
    }

    fun createFailed(messageSource: MessageSource): CodeMasterOperationException {
        return CodeMasterOperationException("error.codemaster.operation.create.failed", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): CodeMasterOperationException {
        return CodeMasterOperationException("error.codemaster.operation.retrieve.failed", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): CodeMasterOperationException {
        return CodeMasterOperationException("error.codemaster.operation.update.failed", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): CodeMasterOperationException {
        return CodeMasterOperationException("error.codemaster.operation.delete.failed", messageSource)
    }
}
