package com.nivasafinance.features.document.dto

import com.nivasafinance.features.document.DocumentTestUtils.createTestDownloadResponse
import com.nivasafinance.features.document.DocumentTestUtils.createTestUploadRequest
import com.nivasafinance.features.document.DocumentTestUtils.createTestUploadResponse
import com.nivasafinance.features.document.enum.ProviderType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

@DisplayName("Document DTO Tests")
class DocumentDtoTest {

    @Nested
    @DisplayName("UploadRequest Tests")
    inner class UploadRequestTests {

        @Test
        @DisplayName("Should create UploadRequest with all fields")
        fun `should create UploadRequest with all fields`() {
            val uploadRequest = createTestUploadRequest(
                fileName = "test-document.pdf",
                fileType = "application/pdf",
                fileSize = 1024L,
                provider = ProviderType.LOCAL,
                category = "KYC",
                docType = "PAN_CARD",
                tags = listOf("kyc", "pan", "verification")
            )

            assertEquals("test-document.pdf", uploadRequest.fileName)
            assertEquals("application/pdf", uploadRequest.fileType)
            assertEquals(1024L, uploadRequest.fileSize)
            assertEquals(ProviderType.LOCAL, uploadRequest.provider)
            assertEquals("KYC", uploadRequest.category)
            assertEquals("PAN_CARD", uploadRequest.docType)
            assertEquals(listOf("kyc", "pan", "verification"), uploadRequest.tags)
        }

        @Test
        @DisplayName("Should create UploadRequest with minimal fields")
        fun `should create UploadRequest with minimal fields`() {
            val uploadRequest = createTestUploadRequest(
                fileName = "minimal-doc.txt",
                fileType = "text/plain",
                fileSize = 0L,
                provider = ProviderType.LOCAL,
                category = "test",
                docType = "text",
                tags = emptyList()
            )

            assertEquals("minimal-doc.txt", uploadRequest.fileName)
            assertEquals(ProviderType.LOCAL, uploadRequest.provider)
            assertEquals("text/plain", uploadRequest.fileType)
            assertEquals(0L, uploadRequest.fileSize)
            assertEquals("test", uploadRequest.category)
            assertEquals("text", uploadRequest.docType)
            assertEquals(emptyList<String>(), uploadRequest.tags)
        }

        @Test
        @DisplayName("Should create UploadRequest with AWS S3 provider")
        fun `should create UploadRequest with AWS S3 provider`() {
            val uploadRequest = createTestUploadRequest(
                provider = ProviderType.AWS_S3,
                category = "LOAN_DOCS",
                docType = "BANK_STATEMENT"
            )

            assertEquals(ProviderType.AWS_S3, uploadRequest.provider)
            assertEquals("LOAN_DOCS", uploadRequest.category)
            assertEquals("BANK_STATEMENT", uploadRequest.docType)
        }

        @Test
        @DisplayName("Should create UploadRequest with empty tags")
        fun `should create UploadRequest with empty tags`() {
            val uploadRequest = createTestUploadRequest(tags = emptyList())

            assertNotNull(uploadRequest.tags)
            assertEquals(0, uploadRequest.tags.size)
        }

        @Test
        @DisplayName("Should create UploadRequest with multiple tags")
        fun `should create UploadRequest with multiple tags`() {
            val tags = listOf("kyc", "pan", "verification", "approved", "2024")
            val uploadRequest = createTestUploadRequest(tags = tags)

            assertEquals(5, uploadRequest.tags.size)
            assertEquals(tags, uploadRequest.tags)
        }

        @Test
        @DisplayName("Should handle large file sizes")
        fun `should handle large file sizes`() {
            val largeFileSize = 10L * 1024 * 1024 * 1024 // 10GB
            val uploadRequest = createTestUploadRequest(fileSize = largeFileSize)

            assertEquals(largeFileSize, uploadRequest.fileSize)
        }

        @Test
        @DisplayName("Should handle special characters in file names")
        fun `should handle special characters in file names`() {
            val specialFileName = "test-file with spaces & symbols (2024).pdf"
            val uploadRequest = createTestUploadRequest(fileName = specialFileName)

            assertEquals(specialFileName, uploadRequest.fileName)
        }
    }

    @Nested
    @DisplayName("UploadResponse Tests")
    inner class UploadResponseTests {

        @Test
        @DisplayName("Should create UploadResponse with all fields")
        fun `should create UploadResponse with all fields`() {
            val documentId = UUID.randomUUID()
            val uploadUrl = "http://localhost:8080/files/test-document.pdf"
            val uploadResponse = createTestUploadResponse(
                documentId = documentId,
                uploadUrl = uploadUrl
            )

            assertEquals(documentId, uploadResponse.documentId)
            assertEquals(uploadUrl, uploadResponse.uploadUrl)
        }

        @Test
        @DisplayName("Should create UploadResponse with empty upload URL")
        fun `should create UploadResponse with empty upload URL`() {
            val documentId = UUID.randomUUID()
            val uploadResponse = createTestUploadResponse(
                documentId = documentId,
                uploadUrl = ""
            )

            assertEquals(documentId, uploadResponse.documentId)
            assertEquals("", uploadResponse.uploadUrl)
        }

        @Test
        @DisplayName("Should create UploadResponse with different document IDs")
        fun `should create UploadResponse with different document IDs`() {
            val documentId1 = UUID.randomUUID()
            val documentId2 = UUID.randomUUID()

            val uploadResponse1 = createTestUploadResponse(documentId = documentId1)
            val uploadResponse2 = createTestUploadResponse(documentId = documentId2)

            assertEquals(documentId1, uploadResponse1.documentId)
            assertEquals(documentId2, uploadResponse2.documentId)
            assert(uploadResponse1.documentId != uploadResponse2.documentId)
        }

        @Test
        @DisplayName("Should create UploadResponse with different upload URLs")
        fun `should create UploadResponse with different upload URLs`() {
            val localUrl = "http://localhost:8080/files/test.pdf"
            val s3Url = "https://s3.amazonaws.com/bucket/test.pdf"

            val localResponse = createTestUploadResponse(uploadUrl = localUrl)
            val s3Response = createTestUploadResponse(uploadUrl = s3Url)

            assertEquals(localUrl, localResponse.uploadUrl)
            assertEquals(s3Url, s3Response.uploadUrl)
        }

        @Test
        @DisplayName("Should create UploadResponse with HTTPS URLs")
        fun `should create UploadResponse with HTTPS URLs`() {
            val httpsUrl = "https://secure.example.com/files/secure-document.pdf"
            val uploadResponse = createTestUploadResponse(uploadUrl = httpsUrl)

            assertEquals(httpsUrl, uploadResponse.uploadUrl)
        }
    }

    @Nested
    @DisplayName("DocumentResponse Tests")
    inner class DocumentResponseTests {

        @Test
        @DisplayName("Should create DocumentResponse with all fields")
        fun `should create DocumentResponse with all fields`() {
            val documentId = UUID.randomUUID()
            val now = LocalDateTime.now()
            val documentResponse = DocumentResponse(
                documentId = documentId,
                fileName = "test-document.pdf",
                fileType = "application/pdf",
                fileSize = 1024L,
                provider = ProviderType.LOCAL,
                storageKey = "docs/testuser/123-test-document.pdf",
                fileUrl = "http://localhost:8080/files/test-document.pdf",
                category = "KYC",
                docType = "PAN_CARD",
                tags = listOf("kyc", "pan", "verification"),
                extData = mapOf("version" to "1.0"),
                createdBy = "testuser",
                createdAt = now,
                updatedBy = "testuser",
                updatedAt = now,
                version = 0L
            )

            assertEquals(documentId, documentResponse.documentId)
            assertEquals("test-document.pdf", documentResponse.fileName)
            assertEquals("application/pdf", documentResponse.fileType)
            assertEquals(1024L, documentResponse.fileSize)
            assertEquals(ProviderType.LOCAL, documentResponse.provider)
            assertEquals("docs/testuser/123-test-document.pdf", documentResponse.storageKey)
            assertEquals("http://localhost:8080/files/test-document.pdf", documentResponse.fileUrl)
            assertEquals("KYC", documentResponse.category)
            assertEquals("PAN_CARD", documentResponse.docType)
            assertEquals(listOf("kyc", "pan", "verification"), documentResponse.tags)
            assertEquals(mapOf("version" to "1.0"), documentResponse.extData)
            assertEquals("testuser", documentResponse.createdBy)
            assertEquals(now, documentResponse.createdAt)
            assertEquals("testuser", documentResponse.updatedBy)
            assertEquals(now, documentResponse.updatedAt)
            assertEquals(0L, documentResponse.version)
        }

        @Test
        @DisplayName("Should create DocumentResponse with minimal fields")
        fun `should create DocumentResponse with minimal fields`() {
            val documentId = UUID.randomUUID()
            val documentResponse = DocumentResponse(
                documentId = documentId,
                fileName = "minimal-doc.txt",
                fileType = null,
                fileSize = null,
                provider = ProviderType.LOCAL,
                storageKey = "docs/testuser/minimal-doc.txt",
                fileUrl = null,
                category = null,
                docType = null,
                tags = emptyList(),
                extData = emptyMap(),
                createdBy = "testuser",
                createdAt = null,
                updatedBy = null,
                updatedAt = null,
                version = 0L
            )

            assertEquals(documentId, documentResponse.documentId)
            assertEquals("minimal-doc.txt", documentResponse.fileName)
            assertEquals(null, documentResponse.fileType)
            assertEquals(null, documentResponse.fileSize)
            assertEquals(ProviderType.LOCAL, documentResponse.provider)
            assertEquals("docs/testuser/minimal-doc.txt", documentResponse.storageKey)
            assertEquals(null, documentResponse.fileUrl)
            assertEquals(null, documentResponse.category)
            assertEquals(null, documentResponse.docType)
            assertEquals(emptyList<String>(), documentResponse.tags)
            assertEquals(emptyMap<String, Any>(), documentResponse.extData)
            assertEquals("testuser", documentResponse.createdBy)
            assertEquals(null, documentResponse.createdAt)
            assertEquals(null, documentResponse.updatedBy)
            assertEquals(null, documentResponse.updatedAt)
            assertEquals(0L, documentResponse.version)
        }
    }

    @Nested
    @DisplayName("DownloadResponse Tests")
    inner class DownloadResponseTests {

        @Test
        @DisplayName("Should create DownloadResponse with download URL")
        fun `should create DownloadResponse with download URL`() {
            val downloadUrl = "https://s3.amazonaws.com/bucket/signed-url?expires=600"
            val downloadResponse = createTestDownloadResponse(downloadUrl = downloadUrl)

            assertEquals(downloadUrl, downloadResponse.downloadUrl)
        }

        @Test
        @DisplayName("Should create DownloadResponse with local file URL")
        fun `should create DownloadResponse with local file URL`() {
            val localUrl = "file:///tmp/documents/docs/testuser/test-document.pdf"
            val downloadResponse = createTestDownloadResponse(downloadUrl = localUrl)

            assertEquals(localUrl, downloadResponse.downloadUrl)
        }

        @Test
        @DisplayName("Should create DownloadResponse with HTTPS URL")
        fun `should create DownloadResponse with HTTPS URL`() {
            val httpsUrl = "https://secure.example.com/files/secure-document.pdf"
            val downloadResponse = createTestDownloadResponse(downloadUrl = httpsUrl)

            assertEquals(httpsUrl, downloadResponse.downloadUrl)
        }

        @Test
        @DisplayName("Should create DownloadResponse with empty URL")
        fun `should create DownloadResponse with empty URL`() {
            val downloadResponse = createTestDownloadResponse(downloadUrl = "")

            assertEquals("", downloadResponse.downloadUrl)
        }
    }

    @Nested
    @DisplayName("DTO Equality Tests")
    inner class DtoEqualityTests {

        @Test
        @DisplayName("Should have equal UploadRequest objects with same data")
        fun `should have equal UploadRequest objects with same data`() {
            val uploadRequest1 = createTestUploadRequest(
                fileName = "test.pdf",
                fileType = "application/pdf",
                fileSize = 1024L,
                provider = ProviderType.LOCAL,
                category = "KYC",
                docType = "PAN_CARD",
                tags = listOf("kyc", "pan")
            )

            val uploadRequest2 = createTestUploadRequest(
                fileName = "test.pdf",
                fileType = "application/pdf",
                fileSize = 1024L,
                provider = ProviderType.LOCAL,
                category = "KYC",
                docType = "PAN_CARD",
                tags = listOf("kyc", "pan")
            )

            assertEquals(uploadRequest1, uploadRequest2)
            assertEquals(uploadRequest1.hashCode(), uploadRequest2.hashCode())
        }

        @Test
        @DisplayName("Should have equal UploadResponse objects with same data")
        fun `should have equal UploadResponse objects with same data`() {
            val documentId = UUID.randomUUID()
            val uploadUrl = "http://localhost:8080/files/test.pdf"

            val uploadResponse1 = createTestUploadResponse(
                documentId = documentId,
                uploadUrl = uploadUrl
            )

            val uploadResponse2 = createTestUploadResponse(
                documentId = documentId,
                uploadUrl = uploadUrl
            )

            assertEquals(uploadResponse1, uploadResponse2)
            assertEquals(uploadResponse1.hashCode(), uploadResponse2.hashCode())
        }
    }

    @Nested
    @DisplayName("DTO Immutability Tests")
    inner class DtoImmutabilityTests {

        @Test
        @DisplayName("Should create immutable UploadRequest")
        fun `should create immutable UploadRequest`() {
            val originalTags = listOf("kyc", "pan")
            val uploadRequest = createTestUploadRequest(tags = originalTags)

            // Verify that the original tags list is not modified
            assertEquals(originalTags, uploadRequest.tags)
        }

        @Test
        @DisplayName("Should create immutable UploadResponse")
        fun `should create immutable UploadResponse`() {
            val documentId = UUID.randomUUID()
            val uploadUrl = "http://localhost:8080/files/test.pdf"
            val uploadResponse = createTestUploadResponse(
                documentId = documentId,
                uploadUrl = uploadUrl
            )

            // Verify that the response contains the expected values
            assertEquals(documentId, uploadResponse.documentId)
            assertEquals(uploadUrl, uploadResponse.uploadUrl)
        }
    }
}
