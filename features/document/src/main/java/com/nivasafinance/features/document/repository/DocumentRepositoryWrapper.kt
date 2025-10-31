package com.nivasafinance.features.document.repository

import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.document.exception.DocumentExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.dao.DataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DocumentRepositoryWrapper(
    private val documentRepository: DocumentRepository,
    private val messageSource: MessageSource
) {
    private val documentExceptionFactory = DocumentExceptionFactory(messageSource)

    fun saveWithException(document: Document): Document {
        return try {
            documentRepository.save(document)
        } catch (e: DataAccessException) {
            throw documentExceptionFactory.createOperationException("create", e)
        }
    }

    fun findByIdWithException(id: Long): Document {
        return documentRepository.findById(id)
            .orElseThrow { documentExceptionFactory.createNotFoundException(java.util.UUID.randomUUID()) }
    }

    fun findAllWithException(pageable: Pageable): Page<Document> {
        return try {
            documentRepository.findAll(pageable)
        } catch (e: DataAccessException) {
            throw documentExceptionFactory.createOperationException("retrieve", e)
        }
    }

    fun findAllWithException(): List<Document> {
        return try {
            documentRepository.findAll()
        } catch (e: DataAccessException) {
            throw documentExceptionFactory.createOperationException("retrieve", e)
        }
    }

    fun deleteByIdWithException(id: Long) {
        if (!documentRepository.existsById(id)) {
            throw documentExceptionFactory.createNotFoundException(java.util.UUID.randomUUID())
        }

        try {
            documentRepository.deleteById(id)
        } catch (e: DataAccessException) {
            throw documentExceptionFactory.createOperationException("delete", e)
        }
    }

    fun findByIdentifierWithException(identifier: UUID): Document {
        return documentRepository.findByIdentifier(identifier)
            .orElseThrow { documentExceptionFactory.createNotFoundException(identifier) }
    }
}
