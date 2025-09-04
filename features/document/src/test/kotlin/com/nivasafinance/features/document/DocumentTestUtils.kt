package com.nivasafinance.features.document

import com.nivasafinance.features.document.dto.DownloadResponse
import com.nivasafinance.features.document.dto.UploadRequest
import com.nivasafinance.features.document.dto.UploadResponse
import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.document.enum.ProviderType
import java.util.UUID

object DocumentTestUtils {

    fun createTestDocument(
        documentId: UUID = UUID.randomUUID(),
        fileName: String = "test-document.pdf",
        fileType: String = "application/pdf",
        fileSize: Long = 1024L,
        provider: ProviderType = ProviderType.LOCAL,
        storageKey: String = "test/storage/key",
        fileUrl: String = "http://localhost:8080/files/test-document.pdf",
        category: String = "KYC",
        docType: String = "PAN_CARD",
        tags: List<String> = listOf("kyc", "pan", "verification"),
        extData: Map<String, Any> = mapOf("version" to "1.0", "encrypted" to false),
        createdBy: String = "testuser"
    ): Document {
        return Document(
            documentId = documentId,
            fileName = fileName,
            fileType = fileType,
            fileSize = fileSize,
            provider = provider,
            storageKey = storageKey,
            fileUrl = fileUrl,
            category = category,
            docType = docType,
            tags = tags,
            extData = extData
        ).apply {
            this.createdBy = createdBy
        }
    }

    fun createTestUploadRequest(
        fileName: String = "test-document.pdf",
        fileType: String = "application/pdf",
        fileSize: Long = 1024L,
        category: String = "KYC",
        docType: String = "PAN_CARD",
        tags: List<String> = listOf("kyc", "pan", "verification")
    ): UploadRequest {
        return UploadRequest(
            fileName = fileName,
            fileType = fileType,
            fileSize = fileSize,
            category = category,
            docType = docType,
            tags = tags
        )
    }

    fun createTestUploadResponse(
        documentId: UUID = UUID.randomUUID(),
        uploadUrl: String = "http://localhost:8080/files/test-document.pdf"
    ): UploadResponse {
        return UploadResponse(
            documentId = documentId,
            uploadUrl = uploadUrl
        )
    }

    fun createTestDownloadResponse(
        downloadUrl: String = "https://s3.amazonaws.com/bucket/signed-url?expires=600"
    ): DownloadResponse {
        return DownloadResponse(
            downloadUrl = downloadUrl
        )
    }

    fun createTestDocumentWithMinimalData(
        documentId: UUID = UUID.randomUUID(),
        fileName: String = "minimal-doc.txt",
        provider: ProviderType = ProviderType.LOCAL,
        storageKey: String = "minimal/storage/key"
    ): Document {
        return Document(
            documentId = documentId,
            fileName = fileName,
            fileType = null,
            fileSize = null,
            provider = provider,
            storageKey = storageKey,
            fileUrl = null,
            category = null,
            docType = null,
            tags = null,
            extData = null
        )
    }

    fun createTestDocumentWithS3Provider(
        documentId: UUID = UUID.randomUUID(),
        fileName: String = "s3-document.pdf",
        storageKey: String = "s3/bucket/key"
    ): Document {
        return Document(
            documentId = documentId,
            fileName = fileName,
            fileType = "application/pdf",
            fileSize = 2048L,
            provider = ProviderType.AWS_S3,
            storageKey = storageKey,
            fileUrl = "https://s3.amazonaws.com/bucket/s3-document.pdf",
            category = "LOAN_DOCS",
            docType = "BANK_STATEMENT",
            tags = listOf("loan", "bank", "statement"),
            extData = mapOf("bucket" to "my-bucket", "region" to "us-east-1")
        )
    }

    fun createTestDocumentWithLargeFile(
        documentId: UUID = UUID.randomUUID(),
        fileName: String = "large-file.zip",
        fileSize: Long = 10 * 1024 * 1024L // 10MB
    ): Document {
        return Document(
            documentId = documentId,
            fileName = fileName,
            fileType = "application/zip",
            fileSize = fileSize,
            provider = ProviderType.AWS_S3,
            storageKey = "large/files/large-file.zip",
            fileUrl = "https://s3.amazonaws.com/bucket/large-file.zip",
            category = "BULK_DATA",
            docType = "ARCHIVE",
            tags = listOf("bulk", "archive", "large"),
            extData = mapOf("compressed" to true, "size_mb" to 10)
        )
    }

    fun createTestDocumentWithMultipleTags(
        documentId: UUID = UUID.randomUUID(),
        fileName: String = "multi-tag-doc.pdf",
        tags: List<String> = listOf("kyc", "pan", "verification", "approved", "2024")
    ): Document {
        return Document(
            documentId = documentId,
            fileName = fileName,
            fileType = "application/pdf",
            fileSize = 512L,
            provider = ProviderType.LOCAL,
            storageKey = "multi/tag/multi-tag-doc.pdf",
            fileUrl = "http://localhost:8080/files/multi-tag-doc.pdf",
            category = "KYC",
            docType = "PAN_CARD",
            tags = tags,
            extData = mapOf("tag_count" to tags.size)
        )
    }
}
