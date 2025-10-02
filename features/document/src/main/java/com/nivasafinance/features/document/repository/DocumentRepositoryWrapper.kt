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

    fun findByIdWithException(id: UUID): Document {
        return documentRepository.findById(id)
            .orElseThrow { documentExceptionFactory.createNotFoundException(id) }
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

    fun deleteByIdWithException(id: UUID) {
        if (!documentRepository.existsById(id)) {
            throw documentExceptionFactory.createNotFoundException(id)
        }

        try {
            documentRepository.deleteById(id)
        } catch (e: DataAccessException) {
            throw documentExceptionFactory.createOperationException("delete", e)
        }
    }
}
