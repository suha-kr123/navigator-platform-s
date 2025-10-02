package com.nivasafinance.features.document.exception

import org.springframework.context.MessageSource
import java.util.Locale
import java.util.UUID

class DocumentExceptionFactory(private val messageSource: MessageSource) {

    fun createNotFoundException(documentId: UUID): DocumentNotFoundException {
        val message = messageSource.getMessage(
            "error.document.not.found",
            arrayOf(documentId.toString()),
            Locale.getDefault()
        )
        return DocumentNotFoundException(message)
    }
    fun createValidationException(reason: String): DocumentValidationException {
        val message = messageSource.getMessage(
            "error.document.validation",
            arrayOf(reason),
            Locale.getDefault()
        )
        return DocumentValidationException(message)
    }

    fun createOperationException(operation: String, cause: Throwable? = null): DocumentOperationException {
        val message = messageSource.getMessage(
            "error.document.operation.failed",
            arrayOf(operation),
            Locale.getDefault()
        )
        return DocumentOperationException(message, cause)
    }

    fun createOperationException(operation: String): DocumentOperationException {
        return createOperationException(operation, null)
    }

    fun validateDocumentForCreation(createRequest: com.nivasafinance.features.document.dto.DocumentCreateRequest) {
        val validationErrors = mutableListOf<String>()
        if (createRequest.file.isEmpty) {
            validationErrors.add("File is required")
        }
        if (createRequest.name.isBlank()) {
            validationErrors.add("Name is required")
        }
        if (validationErrors.isNotEmpty()) {
            throw createValidationException(validationErrors.joinToString(", "))
        }
    }
}
