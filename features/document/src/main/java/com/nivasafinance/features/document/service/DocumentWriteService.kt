package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.dto.DocumentCreateRequest
import com.nivasafinance.features.document.dto.DocumentCreateResponse
import java.util.UUID

/**
 * Service interface for write operations on documents.
 * Handles all operations that modify document state.
 */
interface DocumentWriteService {

    /**
     * Creates a new document.
     * @param createRequest The document creation request
     * @return The created document response
     * @throws DocumentValidationException if validation fails
     * @throws DocumentConflictException if document already exists
     */
    fun createDocument(createRequest: DocumentCreateRequest): DocumentCreateResponse

    /**
     * Deletes a document by its ID.
     * @param id The document ID
     * @throws DocumentNotFoundException if document not found
     */
    fun deleteDocumentById(id: UUID)
}
