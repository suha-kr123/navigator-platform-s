package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.dto.DocumentVerificationRequest
import org.springframework.core.io.Resource
import java.io.InputStream
import java.util.*

interface DocumentService {
    fun createDocument(documentRequest: DocumentRequest, fileInputStream: InputStream): DocumentResponse
    fun getDocumentsByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<DocumentResponse>
    fun getDocumentById(id: UUID): DocumentResponse
    fun deleteDocumentById(id: UUID)
    fun downloadDocument(id: UUID): Resource
    fun getDocumentDownloadUrl(id: UUID, expiresIn: Long = 3600): String
    fun verifyDocument(id: UUID, verificationRequest: DocumentVerificationRequest): DocumentResponse
}
