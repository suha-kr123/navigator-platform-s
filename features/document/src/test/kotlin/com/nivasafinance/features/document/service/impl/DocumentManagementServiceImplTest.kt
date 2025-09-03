package com.nivasafinance.features.document.service.impl

import base.context.UserContext
import base.model.UserInfo
import com.nivasafinance.features.document.DocumentTestUtils.createTestDocument
import com.nivasafinance.features.document.DocumentTestUtils.createTestUploadRequest
import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.document.enum.ProviderType
import com.nivasafinance.features.document.exception.DocumentNotFoundException
import com.nivasafinance.features.document.exception.DocumentValidationException
import com.nivasafinance.features.document.repository.DocumentRepository
import com.nivasafinance.features.document.storage.ContentRepository
import com.nivasafinance.features.document.storage.ContentRepositoryFactory
import exception.UnauthorizedException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.context.MessageSource
import java.io.ByteArrayInputStream
import java.util.Optional
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("DocumentManagementServiceImpl Tests")
class DocumentManagementServiceImplTest {

    private val contentRepositoryFactory = mockk<ContentRepositoryFactory>()
    private val documentRepository = mockk<DocumentRepository>()
    private val messageSource = mockk<MessageSource>()
    private val contentRepository = mockk<ContentRepository>()

    private lateinit var documentManagementService: DocumentManagementServiceImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockkObject(UserContext)
        documentManagementService = DocumentManagementServiceImpl(
            contentRepositoryFactory = contentRepositoryFactory,
            documentRepository = documentRepository
        )

        // Mock the messageSource in BaseNavigatorService
        documentManagementService.messageSource = messageSource

        // Mock UserContext
        val userInfo = UserInfo(username = "testuser", email = "test@example.com", phoneNumber = "1234567890")
        every { UserContext.getUserInfo() } returns userInfo
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(UserContext)
    }

    @Nested
    @DisplayName("Save File Tests")
    inner class SaveFileTests {

        @Test
        @DisplayName("Should save file successfully with all data")
        fun `saveFile should save file successfully with all data`() {
            val uploadRequest = createTestUploadRequest()
            val inputStream = ByteArrayInputStream("test content".toByteArray())
            val documentId = UUID.randomUUID()
            val fileUrl = "http://localhost:8080/files/test-document.pdf"
            val storageKey = "docs/testuser/1234567890-test-document.pdf"

            every { contentRepositoryFactory.getRepository(ProviderType.LOCAL) } returns contentRepository
            every { contentRepository.saveFile(any(), any()) } returns fileUrl
            every { documentRepository.save(any()) } answers {
                val document = firstArg<Document>()
                document.documentId = documentId
                document
            }

            val result = documentManagementService.saveFile(uploadRequest, inputStream)

            assertNotNull(result.documentId)
            assertEquals(fileUrl, result.uploadUrl)

            verify { contentRepositoryFactory.getRepository(ProviderType.LOCAL) }
            verify { contentRepository.saveFile(any(), any()) }
            verify { documentRepository.save(any()) }
        }

        @Test
        @DisplayName("Should save file with minimal data")
        fun `saveFile should save file with minimal data`() {
            val uploadRequest = createTestUploadRequest(
                fileType = "text/plain",
                fileSize = 0L,
                category = "test",
                docType = "text",
                tags = emptyList()
            )
            val inputStream = ByteArrayInputStream("minimal content".toByteArray())
            val documentId = UUID.randomUUID()
            val fileUrl = "http://localhost:8080/files/minimal-doc.txt"

            every { contentRepositoryFactory.getRepository(ProviderType.LOCAL) } returns contentRepository
            every { contentRepository.saveFile(any(), any()) } returns fileUrl
            every { documentRepository.save(any()) } answers {
                val document = firstArg<Document>()
                document.documentId = documentId
                document
            }

            val result = documentManagementService.saveFile(uploadRequest, inputStream)

            assertNotNull(result.documentId)
            assertEquals(fileUrl, result.uploadUrl)

            verify { documentRepository.save(any()) }
        }

        @Test
        @DisplayName("Should throw DocumentValidationException when file name is blank")
        fun `saveFile should throw DocumentValidationException when file name is blank`() {
            val uploadRequest = createTestUploadRequest(fileName = "")
            val inputStream = ByteArrayInputStream("test content".toByteArray())

            every { messageSource.getMessage(any(), any(), any()) } returns "File name cannot be empty"

            assertThrows(DocumentValidationException::class.java) {
                documentManagementService.saveFile(uploadRequest, inputStream)
            }

            verify(exactly = 0) { documentRepository.save(any()) }
        }

        @Test
        @DisplayName("Should throw DocumentValidationException when file name is null")
        fun `saveFile should throw DocumentValidationException when file name is null`() {
            val uploadRequest = createTestUploadRequest(fileName = "")
            val inputStream = ByteArrayInputStream("test content".toByteArray())

            every { messageSource.getMessage(any(), any(), any()) } returns "File name cannot be empty"

            assertThrows(DocumentValidationException::class.java) {
                documentManagementService.saveFile(uploadRequest, inputStream)
            }

            verify(exactly = 0) { documentRepository.save(any()) }
        }

        @Test
        @DisplayName("Should save file with AWS S3 provider")
        fun `saveFile should save file with AWS S3 provider`() {
            val uploadRequest = createTestUploadRequest(provider = ProviderType.AWS_S3)
            val inputStream = ByteArrayInputStream("s3 content".toByteArray())
            val documentId = UUID.randomUUID()
            val fileUrl = "https://s3.amazonaws.com/bucket/test-document.pdf"

            every { contentRepositoryFactory.getRepository(ProviderType.AWS_S3) } returns contentRepository
            every { contentRepository.saveFile(any(), any()) } returns fileUrl
            every { documentRepository.save(any()) } answers {
                val document = firstArg<Document>()
                document.documentId = documentId
                document
            }

            val result = documentManagementService.saveFile(uploadRequest, inputStream)

            assertNotNull(result.documentId)
            assertEquals(fileUrl, result.uploadUrl)

            verify { contentRepositoryFactory.getRepository(ProviderType.AWS_S3) }
            verify { contentRepository.saveFile(any(), any()) }
            verify { documentRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("Get Document Tests")
    inner class GetDocumentTests {

        @Test
        @DisplayName("Should get document successfully when exists")
        fun `getDocument should get document successfully when exists`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(documentId = documentId, createdBy = "testuser")

            every { documentRepository.findById(documentId) } returns Optional.of(document)

            val result = documentManagementService.getDocument(documentId)

            assertNotNull(result)
            assertEquals(documentId, result.documentId)
            assertEquals(document.fileName, result.fileName)
            assertEquals(document.provider, result.provider)
            assertEquals(document.fileType, result.fileType)
            assertEquals(document.fileSize, result.fileSize)
            assertEquals(document.storageKey, result.storageKey)
            assertEquals(document.fileUrl, result.fileUrl)
            assertEquals(document.category, result.category)
            assertEquals(document.docType, result.docType)
            assertEquals(document.tags ?: emptyList<String>(), result.tags)
            assertEquals(document.extData ?: emptyMap<String, Any>(), result.extData)
            assertEquals(document.createdBy, result.createdBy)
            assertEquals(document.createdAt, result.createdAt)
            assertEquals(document.updatedBy, result.updatedBy)
            assertEquals(document.updatedAt, result.updatedAt)
            assertEquals(document.version, result.version)

            verify { documentRepository.findById(documentId) }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        fun `getDocument should throw DocumentNotFoundException when document not found`() {
            val documentId = UUID.randomUUID()

            every { documentRepository.findById(documentId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Document not found"

            assertThrows(DocumentNotFoundException::class.java) {
                documentManagementService.getDocument(documentId)
            }

            verify { documentRepository.findById(documentId) }
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user cannot view document")
        fun `getDocument should throw UnauthorizedException when user cannot view document`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(documentId = documentId, createdBy = "otheruser")

            every { documentRepository.findById(documentId) } returns Optional.of(document)

            assertThrows(UnauthorizedException::class.java) {
                documentManagementService.getDocument(documentId)
            }

            verify { documentRepository.findById(documentId) }
        }
    }

    @Nested
    @DisplayName("Get Download Tests")
    inner class GetDownloadTests {

        @Test
        @DisplayName("Should get download URL successfully for S3 provider")
        fun `getDownload should get download URL successfully for S3 provider`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(
                documentId = documentId,
                createdBy = "testuser",
                provider = ProviderType.AWS_S3
            )
            val expectedSignedUrl = "https://s3.amazonaws.com/bucket/signed-url?expires=600"

            every { documentRepository.findById(documentId) } returns Optional.of(document)
            every { contentRepositoryFactory.getRepository(ProviderType.AWS_S3) } returns contentRepository
            every { contentRepository.getSignedDownloadUrl(document.storageKey, 600) } returns expectedSignedUrl

            val result = documentManagementService.getDownload(documentId)

            assertNotNull(result)
            assertEquals(expectedSignedUrl, result.downloadUrl)

            verify { documentRepository.findById(documentId) }
            verify { contentRepositoryFactory.getRepository(ProviderType.AWS_S3) }
            verify { contentRepository.getSignedDownloadUrl(document.storageKey, 600) }
        }

        @Test
        @DisplayName("Should throw UnsupportedOperationException when provider doesn't support signed URLs")
        fun `getDownload should throw UnsupportedOperationException when provider doesn't support signed URLs`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(
                documentId = documentId,
                createdBy = "testuser",
                provider = ProviderType.LOCAL
            )

            every { documentRepository.findById(documentId) } returns Optional.of(document)
            every { contentRepositoryFactory.getRepository(ProviderType.LOCAL) } returns contentRepository
            every { contentRepository.getSignedDownloadUrl(document.storageKey, 600) } returns null

            assertThrows(UnsupportedOperationException::class.java) {
                documentManagementService.getDownload(documentId)
            }

            verify { documentRepository.findById(documentId) }
            verify { contentRepositoryFactory.getRepository(ProviderType.LOCAL) }
            verify { contentRepository.getSignedDownloadUrl(document.storageKey, 600) }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        fun `getDownload should throw DocumentNotFoundException when document not found`() {
            val documentId = UUID.randomUUID()

            every { documentRepository.findById(documentId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Document not found"

            assertThrows(DocumentNotFoundException::class.java) {
                documentManagementService.getDownload(documentId)
            }

            verify { documentRepository.findById(documentId) }
            verify(exactly = 0) { contentRepositoryFactory.getRepository(any()) }
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user cannot view document")
        fun `getDownload should throw UnauthorizedException when user cannot view document`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(documentId = documentId, createdBy = "otheruser")

            every { documentRepository.findById(documentId) } returns Optional.of(document)

            assertThrows(UnauthorizedException::class.java) {
                documentManagementService.getDownload(documentId)
            }

            verify { documentRepository.findById(documentId) }
            verify(exactly = 0) { contentRepositoryFactory.getRepository(any()) }
        }
    }

    @Nested
    @DisplayName("Fetch File Tests")
    inner class FetchFileTests {

        @Test
        @DisplayName("Should fetch file successfully")
        fun `fetchFile should fetch file successfully`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(documentId = documentId, createdBy = "testuser")
            val expectedInputStream = ByteArrayInputStream("file content".toByteArray())

            every { documentRepository.findById(documentId) } returns Optional.of(document)
            every { contentRepositoryFactory.getRepository(document.provider) } returns contentRepository
            every { contentRepository.fetchFile(document.storageKey) } returns expectedInputStream

            val result = documentManagementService.fetchFile(documentId)

            assertNotNull(result)
            assertEquals(expectedInputStream, result)

            verify { documentRepository.findById(documentId) }
            verify { contentRepositoryFactory.getRepository(document.provider) }
            verify { contentRepository.fetchFile(document.storageKey) }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        fun `fetchFile should throw DocumentNotFoundException when document not found`() {
            val documentId = UUID.randomUUID()

            every { documentRepository.findById(documentId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Document not found"

            assertThrows(DocumentNotFoundException::class.java) {
                documentManagementService.fetchFile(documentId)
            }

            verify { documentRepository.findById(documentId) }
            verify(exactly = 0) { contentRepositoryFactory.getRepository(any()) }
        }
    }

    @Nested
    @DisplayName("Delete Document Tests")
    inner class DeleteDocumentTests {

        @Test
        @DisplayName("Should delete document successfully")
        fun `deleteDocument should delete document successfully`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(documentId = documentId, createdBy = "testuser")

            every { documentRepository.findById(documentId) } returns Optional.of(document)
            every { contentRepositoryFactory.getRepository(document.provider) } returns contentRepository
            every { contentRepository.deleteFile(document.storageKey) } returns Unit
            every { documentRepository.delete(document) } returns Unit

            documentManagementService.deleteDocument(documentId)

            verify { documentRepository.findById(documentId) }
            verify { contentRepositoryFactory.getRepository(document.provider) }
            verify { contentRepository.deleteFile(document.storageKey) }
            verify { documentRepository.delete(document) }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        fun `deleteDocument should throw DocumentNotFoundException when document not found`() {
            val documentId = UUID.randomUUID()

            every { documentRepository.findById(documentId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Document not found"

            assertThrows(DocumentNotFoundException::class.java) {
                documentManagementService.deleteDocument(documentId)
            }

            verify { documentRepository.findById(documentId) }
            verify(exactly = 0) { documentRepository.delete(any()) }
        }

        @Test
        @DisplayName("Should throw UnauthorizedException when user cannot delete document")
        fun `deleteDocument should throw UnauthorizedException when user cannot delete document`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(documentId = documentId, createdBy = "otheruser")

            every { documentRepository.findById(documentId) } returns Optional.of(document)

            assertThrows(UnauthorizedException::class.java) {
                documentManagementService.deleteDocument(documentId)
            }

            verify { documentRepository.findById(documentId) }
            verify(exactly = 0) { documentRepository.delete(any()) }
        }
    }
}
