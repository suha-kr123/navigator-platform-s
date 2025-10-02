package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.dto.DocumentFileResponse
import com.nivasafinance.features.document.dto.DocumentResponse
import java.util.UUID

/**
 * Service interface for read operations on documents.
 * Handles all query operations that don't modify document state.
 */
interface DocumentReadService {
    /**
     * Retrieves a document by its ID.
     * @param id The document ID
     * @return The document response
     * @throws DocumentNotFoundException if document not found
     */
    fun getDocumentById(id: UUID): DocumentResponse

    /**
     * Retrieves the file stream for a document.
     * @param id The document ID
     * @return The file input stream
     * @throws DocumentNotFoundException if document not found
     */
    fun getDocumentFile(id: UUID): DocumentFileResponse
}
