package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.storage.ContentRepository
import java.io.InputStream
import java.util.*

interface DocumentService {
    fun createDocument(documentRequest: DocumentRequest, fileInputStream: InputStream): DocumentResponse
    fun getAllDocuments(): List<DocumentResponse>
    fun getDocumentById(id: UUID): DocumentResponse
    fun deleteDocumentById(id: UUID)
    fun getDocumentDownloadUrl(id: UUID, expiresIn: Long = 3600): String
    fun getContentRepository(provider: String): ContentRepository
    fun getDocumentStream(id: UUID): InputStream
}
