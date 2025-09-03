package com.nivasafinance.features.document.service.impl

import base.BaseNavigatorService
import base.context.UserContext
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.dto.DownloadResponse
import com.nivasafinance.features.document.dto.UploadRequest
import com.nivasafinance.features.document.dto.UploadResponse
import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.document.enum.ProviderType
import com.nivasafinance.features.document.exception.DocumentNotFoundException
import com.nivasafinance.features.document.exception.DocumentValidationException
import com.nivasafinance.features.document.repository.DocumentRepository
import com.nivasafinance.features.document.service.DocumentManagementService
import com.nivasafinance.features.document.storage.ContentRepository
import com.nivasafinance.features.document.storage.ContentRepositoryFactory
import exception.UnauthorizedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.util.UUID

@Service
class DocumentManagementServiceImpl(
    private val contentRepositoryFactory: ContentRepositoryFactory,
    private val documentRepository: DocumentRepository
) : DocumentManagementService, BaseNavigatorService() {

    @Transactional
    override fun saveFile(documentData: UploadRequest, inputStream: InputStream): UploadResponse {
        // Basic validation
        if (documentData.fileName.isBlank()) {
            throw DocumentValidationException("File name cannot be empty", messageSource)
        }

        val currentUser = getCurrentUserId()
        val storageKey = generateStorageKey(currentUser, documentData.fileName)

        // Save file to storage using ContentRepository
        val contentRepository = contentRepositoryFactory.getRepository(documentData.provider)
        val fileUrl = contentRepository.saveFile(inputStream, storageKey)

        // Save document metadata to database
        val document = Document(
            fileName = documentData.fileName,
            fileType = documentData.fileType,
            fileSize = documentData.fileSize,
            provider = documentData.provider,
            storageKey = storageKey,
            fileUrl = fileUrl,
            category = documentData.category,
            docType = documentData.docType,
            tags = documentData.tags,
            extData = emptyMap()
            // Note: createdBy, createdAt, updatedBy, updatedAt, version are automatically set by AuditableEntity
        )

        val savedDocument = documentRepository.save(document)

        return UploadResponse(
            documentId = savedDocument.documentId ?: error("Document ID should not be null after save"),
            uploadUrl = fileUrl
        )
    }

    override fun getDocument(documentId: UUID): DocumentResponse {
        val document = documentRepository.findById(documentId).orElseThrow {
            DocumentNotFoundException(documentId, messageSource)
        }
        ensureCanView(document)

        return DocumentResponse(
            documentId = document.documentId ?: error("Document ID should not be null"),
            fileName = document.fileName,
            fileType = document.fileType,
            fileSize = document.fileSize,
            provider = document.provider,
            storageKey = document.storageKey,
            fileUrl = document.fileUrl,
            category = document.category,
            docType = document.docType,
            tags = document.tags.orEmpty(),
            extData = document.extData.orEmpty(),
            createdBy = document.createdBy,
            createdAt = document.createdAt,
            updatedBy = document.updatedBy,
            updatedAt = document.updatedAt,
            version = document.version
        )
    }

    override fun getDownload(documentId: UUID): DownloadResponse {
        val document = documentRepository.findById(documentId).orElseThrow {
            DocumentNotFoundException(documentId, messageSource)
        }
        ensureCanView(document)

        val contentRepository = contentRepositoryFactory.getRepository(document.provider)
        val signedUrl = contentRepository.getSignedDownloadUrl(document.storageKey, expiresIn = 600)

        return DownloadResponse(
            downloadUrl = signedUrl ?: throw UnsupportedOperationException("Provider doesn't support signed URLs")
        )
    }

    override fun fetchFile(documentId: UUID): InputStream {
        val document = documentRepository.findById(documentId).orElseThrow {
            DocumentNotFoundException(documentId, messageSource)
        }
        ensureCanView(document)

        val contentRepository = contentRepositoryFactory.getRepository(document.provider)
        return contentRepository.fetchFile(document.storageKey)
    }

    @Transactional
    override fun deleteDocument(documentId: UUID) {
        val document = documentRepository.findById(documentId).orElseThrow {
            DocumentNotFoundException(documentId, messageSource)
        }
        ensureCanDelete(document)

        // Delete file from storage
        val contentRepository = contentRepositoryFactory.getRepository(document.provider)
        contentRepository.deleteFile(document.storageKey)

        // Delete document from database
        documentRepository.delete(document)
    }

    override fun getContentRepository(providerType: ProviderType): ContentRepository {
        return contentRepositoryFactory.getRepository(providerType)
    }

    private fun getCurrentUserId(): String {
        return UserContext.getUserInfo()?.username ?: error("User context not available")
    }

    private fun getCurrentUserInfo() = UserContext.getUserInfo()
        ?: error("User context not available")

    private fun isDocumentOwner(document: Document): Boolean {
        return document.createdBy == getCurrentUserInfo().username
    }

    private fun ensureCanView(document: Document) {
        if (!isDocumentOwner(document)) {
            throw UnauthorizedException(
                "User ${getCurrentUserInfo().username} is not authorized to view document ${document.documentId}"
            )
        }
    }

    private fun ensureCanDelete(document: Document) {
        if (!isDocumentOwner(document)) {
            throw UnauthorizedException(
                "User ${getCurrentUserInfo().username} is not authorized to delete document ${document.documentId}"
            )
        }
    }

    private fun generateStorageKey(currentUser: String, fileName: String): String {
        val timestamp = System.currentTimeMillis()
        val fileExtension = fileName.substringAfterLast('.', "")
        val baseFileName = fileName.substringBeforeLast('.')
        return "docs/$currentUser/$timestamp-$baseFileName.$fileExtension"
    }
}
