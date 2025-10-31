package com.nivasafinance.features.document.service.impl

import com.nivasafinance.features.document.dto.DocumentFileResponse
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.dto.toDocumentResponse
import com.nivasafinance.features.document.repository.DocumentRepositoryWrapper
import com.nivasafinance.features.document.service.DocumentReadService
import com.nivasafinance.features.document.storage.ContentRepositoryFactory
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes.DOCUMENT_MASTER
import com.nivasafinance.features.master.codemaster.service.CodeMasterService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DocumentReadServiceImpl(
    private val documentRepositoryWrapper: DocumentRepositoryWrapper,
    private val contentRepositoryFactory: ContentRepositoryFactory
) : DocumentReadService {
    override fun getDocumentById(id: Long): DocumentResponse {
        return documentRepositoryWrapper.findByIdWithException(id).toDocumentResponse()
    }

    override fun getDocumentByIdentifier(id: UUID): DocumentResponse {
        return documentRepositoryWrapper.findByIdentifierWithException(id).toDocumentResponse()
    }

    override fun getDocumentFile(id: UUID): DocumentFileResponse {
        val document = documentRepositoryWrapper.findByIdentifierWithException(id)
        val contentRepository = contentRepositoryFactory.getRepository(document.provider.name)
        val file = contentRepository.fetchFile(document.path)
        return DocumentFileResponse(file = file, data = document.toDocumentResponse())
    }
}
