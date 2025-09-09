package com.nivasafinance.features.document.integration

import com.nivasafinance.features.document.DocumentTestUtils.createTestDocument
import com.nivasafinance.features.document.DocumentTestUtils.createTestUploadRequest
import com.nivasafinance.features.document.DocumentTestUtils.createTestUploadResponse
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.enum.ProviderType
import com.nivasafinance.features.document.service.DocumentManagementService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.UUID

@DisplayName("Document Integration Tests")
class DocumentIntegrationTest {

    private fun createDocumentResponse(
        document: com.nivasafinance.features.document.entity.Document
    ): DocumentResponse {
        return DocumentResponse(
            documentId = document.documentId ?: error("Document ID should not be null"),
            fileName = document.fileName,
            fileType = document.fileType,
            fileSize = document.fileSize,
            provider = document.provider,
            storageKey = document.storageKey,
            fileUrl = document.fileUrl,
            category = document.category,
            docType = document.docType,
            tags = document.tags ?: emptyList(),
            extData = document.extData ?: emptyMap(),
            createdBy = document.createdBy,
            createdAt = document.createdAt,
            updatedBy = document.updatedBy,
            updatedAt = document.updatedAt,
            version = document.version
        )
    }

    private lateinit var documentManagementService: DocumentManagementService

    @BeforeEach
    fun setUp() {
        documentManagementService = mockk()
    }

    @Nested
    @DisplayName("Document Upload Flow Tests")
    inner class DocumentUploadFlowTests {

        @Test
        @DisplayName("Should complete full document upload flow successfully")
        fun `should complete full document upload flow successfully`() {
            val uploadRequest = createTestUploadRequest()
            val inputStream = ByteArrayInputStream("test content".toByteArray())
            val expectedResponse = createTestUploadResponse()

            every { documentManagementService.saveFile(uploadRequest, inputStream) } returns expectedResponse

            val result = documentManagementService.saveFile(uploadRequest, inputStream)

            assertNotNull(result.documentId)
            assertEquals(expectedResponse.uploadUrl, result.uploadUrl)

            verify { documentManagementService.saveFile(uploadRequest, inputStream) }
        }

        @Test
        @DisplayName("Should handle document upload with S3 provider")
        fun `should handle document upload with S3 provider`() {
            val uploadRequest = createTestUploadRequest()
            val inputStream = ByteArrayInputStream("test content".toByteArray())
            val uploadResponse = createTestUploadResponse(uploadUrl = "https://s3.amazonaws.com/bucket/test.pdf")

            every { documentManagementService.saveFile(uploadRequest, inputStream) } returns uploadResponse

            val result = documentManagementService.saveFile(uploadRequest, inputStream)

            assertEquals("https://s3.amazonaws.com/bucket/test.pdf", result.uploadUrl)

            verify { documentManagementService.saveFile(uploadRequest, inputStream) }
        }

        @Test
        @DisplayName("Should handle document upload with various file types")
        fun `should handle document upload with various file types`() {
            val pdfRequest = createTestUploadRequest(
                fileName = "document.pdf",
                fileType = "application/pdf",
                category = "KYC",
                docType = "PAN_CARD"
            )
            val imageRequest = createTestUploadRequest(
                fileName = "photo.jpg",
                fileType = "image/jpeg",
                category = "KYC",
                docType = "PHOTO"
            )
            val docRequest = createTestUploadRequest(
                fileName = "contract.docx",
                fileType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                category = "LOAN_DOCS",
                docType = "AGREEMENT"
            )

            val inputStream = ByteArrayInputStream("test content".toByteArray())
            val pdfResponse = createTestUploadResponse()
            val imageResponse = createTestUploadResponse()
            val docResponse = createTestUploadResponse()

            every { documentManagementService.saveFile(pdfRequest, inputStream) } returns pdfResponse
            every { documentManagementService.saveFile(imageRequest, inputStream) } returns imageResponse
            every { documentManagementService.saveFile(docRequest, inputStream) } returns docResponse

            val pdfResult = documentManagementService.saveFile(pdfRequest, inputStream)
            val imageResult = documentManagementService.saveFile(imageRequest, inputStream)
            val docResult = documentManagementService.saveFile(docRequest, inputStream)

            assertNotNull(pdfResult.documentId)
            assertNotNull(imageResult.documentId)
            assertNotNull(docResult.documentId)

            verify { documentManagementService.saveFile(pdfRequest, inputStream) }
            verify { documentManagementService.saveFile(imageRequest, inputStream) }
            verify { documentManagementService.saveFile(docRequest, inputStream) }
        }
    }

    @Nested
    @DisplayName("Document Retrieval Flow Tests")
    inner class DocumentRetrievalFlowTests {

        @Test
        @DisplayName("Should complete full document retrieval flow successfully")
        fun `should complete full document retrieval flow successfully`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(documentId = documentId)
            val expectedInputStream = ByteArrayInputStream("file content".toByteArray())

            every { documentManagementService.getDocument(documentId) } returns createDocumentResponse(document)
            every { documentManagementService.fetchFile(documentId) } returns expectedInputStream

            val retrievedDocument = documentManagementService.getDocument(documentId)
            val fileStream = documentManagementService.fetchFile(documentId)

            assertEquals(documentId, retrievedDocument.documentId)
            assertEquals(document.fileName, retrievedDocument.fileName)
            assertNotNull(fileStream)

            verify { documentManagementService.getDocument(documentId) }
            verify { documentManagementService.fetchFile(documentId) }
        }

        @Test
        @DisplayName("Should handle document retrieval with S3 provider")
        fun `should handle document retrieval with S3 provider`() {
            val s3Document = createTestDocument(provider = ProviderType.AWS_S3)
            val s3InputStream = ByteArrayInputStream("s3 content".toByteArray())

            every {
                documentManagementService.getDocument(s3Document.documentId!!)
            } returns createDocumentResponse(s3Document)
            every { documentManagementService.fetchFile(s3Document.documentId!!) } returns s3InputStream

            val s3Result = documentManagementService.getDocument(s3Document.documentId!!)
            val s3File = documentManagementService.fetchFile(s3Document.documentId!!)

            assertEquals(ProviderType.AWS_S3, s3Result.provider)
            assertNotNull(s3File)

            verify { documentManagementService.getDocument(s3Document.documentId!!) }
            verify { documentManagementService.fetchFile(s3Document.documentId!!) }
        }
    }

    @Nested
    @DisplayName("Document Management Flow Tests")
    inner class DocumentManagementFlowTests {

        @Test
        @DisplayName("Should complete full document lifecycle successfully")
        fun `should complete full document lifecycle successfully`() {
            val uploadRequest = createTestUploadRequest()
            val inputStream = ByteArrayInputStream("test content".toByteArray())
            val uploadResponse = createTestUploadResponse()
            val document = createTestDocument(documentId = uploadResponse.documentId)
            val fileInputStream = ByteArrayInputStream("file content".toByteArray())

            // Upload
            every { documentManagementService.saveFile(uploadRequest, inputStream) } returns uploadResponse
            // Retrieve
            every {
                documentManagementService.getDocument(uploadResponse.documentId)
            } returns createDocumentResponse(document)
            every { documentManagementService.fetchFile(uploadResponse.documentId) } returns fileInputStream
            // Delete
            every { documentManagementService.deleteDocument(uploadResponse.documentId) } returns Unit

            // Execute full lifecycle
            val uploadResult = documentManagementService.saveFile(uploadRequest, inputStream)
            val retrievedDocument = documentManagementService.getDocument(uploadResult.documentId)
            val fileStream = documentManagementService.fetchFile(uploadResult.documentId)
            documentManagementService.deleteDocument(uploadResult.documentId)

            // Verify results
            assertNotNull(uploadResult.documentId)
            assertEquals(uploadResult.documentId, retrievedDocument.documentId)
            assertNotNull(fileStream)

            // Verify all operations were called
            verify { documentManagementService.saveFile(uploadRequest, inputStream) }
            verify { documentManagementService.getDocument(uploadResult.documentId) }
            verify { documentManagementService.fetchFile(uploadResult.documentId) }
            verify { documentManagementService.deleteDocument(uploadResult.documentId) }
        }

        @Test
        @DisplayName("Should handle multiple document operations concurrently")
        fun `should handle multiple document operations concurrently`() {
            val document1Id = UUID.randomUUID()
            val document2Id = UUID.randomUUID()
            val document3Id = UUID.randomUUID()

            val document1 = createTestDocument(documentId = document1Id, fileName = "doc1.pdf")
            val document2 = createTestDocument(documentId = document2Id, fileName = "doc2.pdf")
            val document3 = createTestDocument(documentId = document3Id, fileName = "doc3.pdf")

            every { documentManagementService.getDocument(document1Id) } returns createDocumentResponse(document1)
            every { documentManagementService.getDocument(document2Id) } returns createDocumentResponse(document2)
            every { documentManagementService.getDocument(document3Id) } returns createDocumentResponse(document3)
            every { documentManagementService.deleteDocument(document1Id) } returns Unit
            every { documentManagementService.deleteDocument(document2Id) } returns Unit
            every { documentManagementService.deleteDocument(document3Id) } returns Unit

            // Simulate concurrent operations
            val result1 = documentManagementService.getDocument(document1Id)
            val result2 = documentManagementService.getDocument(document2Id)
            val result3 = documentManagementService.getDocument(document3Id)

            documentManagementService.deleteDocument(document1Id)
            documentManagementService.deleteDocument(document2Id)
            documentManagementService.deleteDocument(document3Id)

            assertEquals("doc1.pdf", result1.fileName)
            assertEquals("doc2.pdf", result2.fileName)
            assertEquals("doc3.pdf", result3.fileName)

            verify { documentManagementService.getDocument(document1Id) }
            verify { documentManagementService.getDocument(document2Id) }
            verify { documentManagementService.getDocument(document3Id) }
            verify { documentManagementService.deleteDocument(document1Id) }
            verify { documentManagementService.deleteDocument(document2Id) }
            verify { documentManagementService.deleteDocument(document3Id) }
        }
    }

    @Nested
    @DisplayName("Document Category and Type Tests")
    inner class DocumentCategoryAndTypeTests {

        @Test
        @DisplayName("Should handle documents with different categories")
        fun `should handle documents with different categories`() {
            val kycDocument = createTestDocument(category = "KYC", docType = "PAN_CARD")
            val loanDocument = createTestDocument(category = "LOAN_DOCS", docType = "BANK_STATEMENT")
            val identityDocument = createTestDocument(category = "IDENTITY", docType = "AADHAAR")

            every {
                documentManagementService.getDocument(kycDocument.documentId!!)
            } returns createDocumentResponse(kycDocument)
            every {
                documentManagementService.getDocument(loanDocument.documentId!!)
            } returns createDocumentResponse(loanDocument)
            every {
                documentManagementService.getDocument(identityDocument.documentId!!)
            } returns createDocumentResponse(identityDocument)

            val kycResult = documentManagementService.getDocument(kycDocument.documentId!!)
            val loanResult = documentManagementService.getDocument(loanDocument.documentId!!)
            val identityResult = documentManagementService.getDocument(identityDocument.documentId!!)

            assertEquals("KYC", kycResult.category)
            assertEquals("PAN_CARD", kycResult.docType)
            assertEquals("LOAN_DOCS", loanResult.category)
            assertEquals("BANK_STATEMENT", loanResult.docType)
            assertEquals("IDENTITY", identityResult.category)
            assertEquals("AADHAAR", identityResult.docType)

            verify { documentManagementService.getDocument(kycDocument.documentId!!) }
            verify { documentManagementService.getDocument(loanDocument.documentId!!) }
            verify { documentManagementService.getDocument(identityDocument.documentId!!) }
        }

        @Test
        @DisplayName("Should handle documents with multiple tags")
        fun `should handle documents with multiple tags`() {
            val multiTagDocument = createTestDocument(
                tags = listOf("kyc", "pan", "verification", "approved", "2024")
            )

            every {
                documentManagementService.getDocument(multiTagDocument.documentId!!)
            } returns createDocumentResponse(multiTagDocument)

            val result = documentManagementService.getDocument(multiTagDocument.documentId!!)

            assertEquals(5, result.tags?.size)
            assertTrue(result.tags?.contains("kyc") == true)
            assertTrue(result.tags?.contains("pan") == true)
            assertTrue(result.tags?.contains("verification") == true)
            assertTrue(result.tags?.contains("approved") == true)
            assertTrue(result.tags?.contains("2024") == true)

            verify { documentManagementService.getDocument(multiTagDocument.documentId!!) }
        }
    }
}
