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

    fun createEntityNotFoundException(entityId: UUID, entityType: String): DocumentNotFoundException {
        val message = messageSource.getMessage(
            "error.document.entity.not.found",
            arrayOf(entityId.toString(), entityType),
            Locale.getDefault()
        )
        return DocumentNotFoundException(message)
    }

    fun createConflictException(entityId: UUID, entityType: String, documentType: String): DocumentConflictException {
        val message = messageSource.getMessage(
            "error.document.already.exists",
            arrayOf(entityId.toString(), entityType, documentType),
            Locale.getDefault()
        )
        return DocumentConflictException(message)
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

    fun validateDocumentForCreation(documentRequest: com.nivasafinance.features.document.dto.DocumentRequest) {
        val validationErrors = mutableListOf<String>()

        if (documentRequest.entityType.isBlank()) {
            validationErrors.add("Entity type is required")
        }
        if (documentRequest.documentType.isBlank()) {
            validationErrors.add("Document type is required")
        }
        if (documentRequest.fileName.isBlank()) {
            validationErrors.add("File name is required")
        }

        if (validationErrors.isNotEmpty()) {
            throw createValidationException(validationErrors.joinToString(", "))
        }
    }
}
