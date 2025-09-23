package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.config.DocumentStorageProperties
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.dto.DocumentVerificationRequest
import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.document.exception.DocumentExceptionFactory
import com.nivasafinance.features.document.repository.DocumentRepositoryWrapper
import com.nivasafinance.features.document.storage.ContentRepositoryFactory
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class DocumentServiceImpl(
    private val documentRepositoryWrapper: DocumentRepositoryWrapper,
    private val contentRepositoryFactory: ContentRepositoryFactory,
    private val documentStorageProperties: DocumentStorageProperties,
    private val messageSource: MessageSource
) : DocumentService {

    private val logger = LoggerFactory.getLogger(DocumentServiceImpl::class.java)
    private val documentExceptionFactory = DocumentExceptionFactory(messageSource)

    override fun createDocument(documentRequest: DocumentRequest, fileInputStream: InputStream): DocumentResponse {
        documentExceptionFactory.validateDocumentForCreation(documentRequest)

        val contentRepository = contentRepositoryFactory.getRepository(documentStorageProperties.provider)
        val documentPath = generateDocumentPath(documentRequest)

        val storageKey = contentRepository.saveFile(fileInputStream, documentPath)

        val document = toDocument(documentRequest, storageKey)
        val savedDocument = documentRepositoryWrapper.saveWithException(document)
        return toDocumentResponse(savedDocument)
    }

    override fun getDocumentsByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<DocumentResponse> {
        val documents = documentRepositoryWrapper.findByEntityIdAndEntityTypeWithException(entityId, entityType)
        return documents.map { toDocumentResponse(it) }
    }

    override fun getDocumentById(id: UUID): DocumentResponse {
        val document = documentRepositoryWrapper.findByIdWithException(id)
        return toDocumentResponse(document)
    }

    override fun updateDocument(id: UUID, documentRequest: DocumentRequest): DocumentResponse {
        val existingDocument = documentRepositoryWrapper.findByIdWithException(id)

        // Update the document fields
        existingDocument.entityId = documentRequest.entityId
        existingDocument.entityType = documentRequest.entityType
        existingDocument.documentType = documentRequest.documentType
        existingDocument.verificationStatus = documentRequest.verificationStatus
        existingDocument.verificationNotes = documentRequest.verificationNotes
        existingDocument.fileName = documentRequest.fileName
        existingDocument.fileType = documentRequest.fileType
        existingDocument.fileSize = documentRequest.fileSize
        existingDocument.provider = documentRequest.provider
        existingDocument.fileUrl = documentRequest.fileUrl
        existingDocument.tags = documentRequest.tags

        val updatedDocument = documentRepositoryWrapper.saveWithException(existingDocument)
        return toDocumentResponse(updatedDocument)
    }

    override fun deleteDocumentById(id: UUID) {
        val document = documentRepositoryWrapper.findByIdWithException(id)
        val contentRepository = contentRepositoryFactory.getRepository(document.provider)

        try {
            contentRepository.deleteFile(document.storageKey)
        } catch (e: IOException) {
            logger.warn("Failed to delete file from storage: ${document.storageKey}", e)
        }
        documentRepositoryWrapper.deleteByIdWithException(id)
    }

    override fun downloadDocument(id: UUID): Resource {
        val document = documentRepositoryWrapper.findByIdWithException(id)
        val contentRepository = contentRepositoryFactory.getRepository(document.provider)
        val inputStream = contentRepository.fetchFile(document.storageKey)
        return InputStreamResource(inputStream)
    }

    override fun getDocumentDownloadUrl(id: UUID, expiresIn: Long): String {
        val document = documentRepositoryWrapper.findByIdWithException(id)
        val contentRepository = contentRepositoryFactory.getRepository(document.provider)
        return contentRepository.getSignedDownloadUrl(document.storageKey, expiresIn)
            ?: throw documentExceptionFactory.createOperationException("generate download URL")
    }

    override fun verifyDocument(id: UUID, verificationRequest: DocumentVerificationRequest): DocumentResponse {
        val document = documentRepositoryWrapper.findByIdWithException(id)

        document.verificationStatus = verificationRequest.verificationStatus
        document.verificationNotes = verificationRequest.verificationNotes

        val updatedDocument = documentRepositoryWrapper.saveWithException(document)
        return toDocumentResponse(updatedDocument)
    }

    private fun toDocument(documentRequest: DocumentRequest, storageKey: String): Document {
        return Document(
            entityId = documentRequest.entityId,
            entityType = documentRequest.entityType,
            documentType = documentRequest.documentType,
            verificationStatus = documentRequest.verificationStatus,
            verificationNotes = documentRequest.verificationNotes,
            fileName = documentRequest.fileName,
            fileType = documentRequest.fileType,
            fileSize = documentRequest.fileSize,
            provider = documentRequest.provider,
            storageKey = storageKey,
            fileUrl = documentRequest.fileUrl,
            tags = documentRequest.tags
        )
    }

    private fun generateDocumentPath(documentRequest: DocumentRequest): String {
        val timestamp = System.currentTimeMillis()
        return "${documentRequest.entityType}/${documentRequest.entityId}/" +
            "${documentRequest.documentType}/${timestamp}_${documentRequest.fileName}"
    }

    private fun toDocumentResponse(document: Document): DocumentResponse {
        return DocumentResponse(
            id = document.id ?: UUID.randomUUID(),
            entityId = document.entityId ?: UUID.randomUUID(),
            entityType = document.entityType.orEmpty(),
            documentType = document.documentType.orEmpty(),
            verificationStatus = document.verificationStatus,
            verificationNotes = document.verificationNotes,
            fileName = document.fileName,
            fileType = document.fileType,
            fileSize = document.fileSize,
            provider = document.provider.name,
            storageKey = document.storageKey,
            fileUrl = document.fileUrl,
            tags = document.tags,
            createdAt = document.createdAt ?: LocalDateTime.now(),
            createdBy = document.createdBy,
            updatedAt = document.updatedAt ?: LocalDateTime.now(),
            updatedBy = document.updatedBy
        )
    }
}
