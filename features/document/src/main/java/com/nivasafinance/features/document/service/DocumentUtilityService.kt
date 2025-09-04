package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.enum.AllowedDocumentType
import com.nivasafinance.features.document.enum.ProviderType

interface DocumentUtilityService {

    /**
     * Generates a unique storage key for a document
     */
    fun generateStorageKey(
        userId: String,
        fileName: String,
        category: String? = null,
        provider: ProviderType
    ): String

    /**
     * Validates file size
     */
    fun validateFileSize(fileSize: Long, maxSizeInMB: Long = 10): Boolean

    /**
     * Validates file type against allowed document types
     */
    fun validateFileType(fileName: String): Boolean

    /**
     * Gets the allowed document type for a file
     */
    fun getAllowedDocumentType(fileName: String): AllowedDocumentType?
}
