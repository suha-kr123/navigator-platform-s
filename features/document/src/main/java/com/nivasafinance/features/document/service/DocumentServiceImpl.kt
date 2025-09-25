package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.config.DocumentStorageProperties
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.document.exception.DocumentExceptionFactory
import com.nivasafinance.features.document.repository.DocumentRepositoryWrapper
import com.nivasafinance.features.document.storage.ContentRepositoryFactory
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.util.*

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

    override fun getAllDocuments(): List<DocumentResponse> {
        val documents = documentRepositoryWrapper.findAllWithException()
        return documents.map { toDocumentResponse(it) }
    }

    override fun getDocumentById(id: UUID): DocumentResponse {
        val document = documentRepositoryWrapper.findByIdWithException(id)
        return toDocumentResponse(document)
    }

    override fun deleteDocumentById(id: UUID) {
        val document = documentRepositoryWrapper.findByIdWithException(id)
        val contentRepository = contentRepositoryFactory.getRepository(document.provider)

        try {
            contentRepository.deleteFile(document.storageKey)
        } catch (e: FileNotFoundException) {
            logger.warn("File not found during deletion: ${document.storageKey}", e)
        } catch (e: IOException) {
            logger.warn("IO error during file deletion: ${document.storageKey}", e)
        }
        documentRepositoryWrapper.deleteByIdWithException(id)
    }
    override fun getDocumentDownloadUrl(id: UUID, expiresIn: Long): String {
        val document = documentRepositoryWrapper.findByIdWithException(id)
        val contentRepository = contentRepositoryFactory.getRepository(document.provider)
        return contentRepository.getSignedDownloadUrl(document.storageKey, expiresIn)
            ?: "/api/documents/$id/download" // Return direct download URL for local storage
    }

    override fun getContentRepository(provider: String): com.nivasafinance.features.document.storage.ContentRepository {
        return contentRepositoryFactory.getRepository(provider)
    }

    override fun getDocumentStream(id: UUID): InputStream {
        val document = documentRepositoryWrapper.findByIdWithException(id)
        val contentRepository = contentRepositoryFactory.getRepository(document.provider)
        return contentRepository.fetchFile(document.storageKey)
    }
    private fun toDocument(documentRequest: DocumentRequest, storageKey: String): Document {
        return Document(
            documentType = documentRequest.documentType,
            verificationStatus = "PENDING",
            verificationNotes = null,
            fileName = documentRequest.fileName,
            fileType = documentRequest.fileType,
            fileSize = documentRequest.fileSize,
            provider = documentStorageProperties.provider,
            storageKey = storageKey,
            fileUrl = documentRequest.fileUrl.orEmpty(),
            category = documentRequest.category,
            docType = documentRequest.docType,
            tags = documentRequest.tags,
            extData = documentRequest.extData
        )
    }

    private fun generateDocumentPath(documentRequest: DocumentRequest): String {
        val timestamp = System.currentTimeMillis()
        return "documents/${documentRequest.documentType}/${timestamp}_${documentRequest.fileName}"
    }

    private fun toDocumentResponse(document: Document): DocumentResponse {
        return DocumentResponse(
            documentId = document.documentId ?: UUID.randomUUID(),
            documentType = document.documentType.orEmpty(),
            verificationStatus = document.verificationStatus,
            verificationNotes = document.verificationNotes,
            fileName = document.fileName,
            fileType = document.fileType,
            fileSize = document.fileSize,
            storageKey = document.storageKey,
            fileUrl = document.fileUrl,
            category = document.category,
            docType = document.docType,
            tags = document.tags,
            extData = document.extData,
            createdAt = document.createdAt ?: LocalDateTime.now(),
            createdBy = document.createdBy,
            updatedAt = document.updatedAt ?: LocalDateTime.now(),
            updatedBy = document.updatedBy
        )
    }
}
