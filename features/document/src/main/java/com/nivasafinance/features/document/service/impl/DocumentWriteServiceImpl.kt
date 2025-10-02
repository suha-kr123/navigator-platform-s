package com.nivasafinance.features.document.service.impl

import com.nivasafinance.features.document.config.DocumentStorageProperties
import com.nivasafinance.features.document.dto.DocumentCreateRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.dto.toDocumentResponse
import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.document.enum.DocumentStorageProvider
import com.nivasafinance.features.document.exception.DocumentExceptionFactory
import com.nivasafinance.features.document.repository.DocumentRepositoryWrapper
import com.nivasafinance.features.document.service.DocumentWriteService
import com.nivasafinance.features.document.storage.ContentRepositoryFactory
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes.DOCUMENT_MASTER
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse
import com.nivasafinance.features.master.codemaster.service.CodeMasterService
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.FileNotFoundException
import java.io.IOException
import java.util.UUID

@Service
@Transactional
class DocumentWriteServiceImpl(
    private val documentRepositoryWrapper: DocumentRepositoryWrapper,
    private val contentRepositoryFactory: ContentRepositoryFactory,
    private val documentStorageProperties: DocumentStorageProperties,
    private val codeMasterService: CodeMasterService,
    private val messageSource: MessageSource
) : DocumentWriteService {
    private val logger = LoggerFactory.getLogger(DocumentWriteServiceImpl::class.java)
    private val documentExceptionFactory = DocumentExceptionFactory(messageSource)

    @Transactional
    override fun createDocument(createRequest: DocumentCreateRequest): DocumentResponse {
        // Validate the creation request
        documentExceptionFactory.validateDocumentForCreation(createRequest)
        createRequest.tags?.let { tags ->
            val codeList = codeMasterService.getMasterCodeChildrenWithValues(parentCodeKey = DOCUMENT_MASTER)
            validateTagsWithMasters(codeList, tags)
        }
        val contentRepository = contentRepositoryFactory.getRepository(documentStorageProperties.provider)
        val documentPath = createRequest.customPath ?: generateDocumentPath(createRequest.name)

        val storageKey = contentRepository.saveFile(createRequest.file.inputStream, documentPath)

        val document = Document(
            name = createRequest.name,
            type = createRequest.file.contentType,
            size = createRequest.file.size,
            provider = DocumentStorageProvider.valueOf(documentStorageProperties.provider),
            path = storageKey,
            tags = createRequest.tags
        )

        val savedDocument = documentRepositoryWrapper.saveWithException(document)
        return savedDocument.toDocumentResponse()
    }

    @Transactional
    override fun deleteDocumentById(id: UUID) {

        val document = documentRepositoryWrapper.findByIdWithException(id)
        val contentRepository = contentRepositoryFactory.getRepository(document.provider.name)
        try {
            contentRepository.deleteFile(document.path)
            logger.debug("Successfully deleted file from storage: ${document.path}")
        } catch (e: FileNotFoundException) {
            logger.warn("File not found during deletion: ${document.path}", e)
        } catch (e: IOException) {
            logger.warn("IO error during file deletion: ${document.path}", e)
        }
        documentRepositoryWrapper.deleteByIdWithException(id)
    }

    private fun generateDocumentPath(fileName: String): String {
        val timestamp = System.currentTimeMillis()
        return "documents/${timestamp}_$fileName"
    }

    private fun validateTagsWithMasters(codeList: List<MasterCodeWithValuesResponse>, tags: List<String>) {
        val availableTags = codeList.map { it.values.map { it.key } }.flatten()
        val invalidTags = tags.filter { !availableTags.contains(it) }
        if (invalidTags.isNotEmpty()) {
            throw documentExceptionFactory.createValidationException(
                "Invalid tags: ${invalidTags.joinToString(", ")}"
            )
        }
    }
}
