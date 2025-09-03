package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.dto.DownloadResponse
import com.nivasafinance.features.document.dto.UploadRequest
import com.nivasafinance.features.document.dto.UploadResponse
import com.nivasafinance.features.document.enum.ProviderType
import com.nivasafinance.features.document.storage.ContentRepository
import java.io.InputStream
import java.util.UUID

interface DocumentManagementService {
    fun saveFile(documentData: UploadRequest, inputStream: InputStream): UploadResponse
    fun getDocument(documentId: UUID): DocumentResponse
    fun getDownload(documentId: UUID): DownloadResponse
    fun fetchFile(documentId: UUID): InputStream
    fun deleteDocument(documentId: UUID)
    fun getContentRepository(providerType: ProviderType): ContentRepository
}
