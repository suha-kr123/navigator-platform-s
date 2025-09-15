package com.nivasafinance.features.document.repository

import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.document.exception.DocumentExceptionFactory
import com.nivasafinance.features.document.exception.DocumentNotFoundException
import org.springframework.context.MessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.*

@Component
class DocumentRepositoryWrapper(
    private val documentRepository: DocumentRepository,
    private val messageSource: MessageSource
) {
    private val documentExceptionFactory = DocumentExceptionFactory(messageSource)

    fun saveWithException(document: Document): Document {
        return try {
            documentRepository.save(document)
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            throw documentExceptionFactory.createOperationException("retrieve", e)
        }
    }

    fun findByEntityIdAndEntityTypeWithException(entityId: UUID, entityType: String): List<Document> {
        return try {
            documentRepository.findByEntityIdAndEntityType(entityId, entityType)
        } catch (e: Exception) {
            throw documentExceptionFactory.createOperationException("retrieve.entity", e)
        }
    }

    fun deleteByIdWithException(id: UUID) {
        try {
            if (!documentRepository.existsById(id)) {
                throw documentExceptionFactory.createNotFoundException(id)
            }
            documentRepository.deleteById(id)
        } catch (e: DocumentNotFoundException) {
            throw e
        } catch (e: Exception) {
            throw documentExceptionFactory.createOperationException("delete", e)
        }
    }

    fun existsByEntityIdAndEntityTypeAndDocumentTypeWithException(
        entityId: UUID,
        entityType: String,
        documentType: String
    ): Boolean {
        return try {
            documentRepository.existsByEntityIdAndEntityTypeAndDocumentType(entityId, entityType, documentType)
        } catch (e: Exception) {
            throw documentExceptionFactory.createOperationException("check existence", e)
        }
    }
}
