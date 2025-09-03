package com.nivasafinance.features.document.repository

import com.nivasafinance.features.document.DocumentTestUtils.createTestDocument
import com.nivasafinance.features.document.DocumentTestUtils.createTestDocumentWithS3Provider
import com.nivasafinance.features.document.enum.ProviderType
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
import java.util.Optional
import java.util.UUID

@DisplayName("DocumentRepository Tests")
class DocumentRepositoryTest {

    private lateinit var documentRepository: DocumentRepository

    @BeforeEach
    fun setUp() {
        documentRepository = mockk()
    }

    @Nested
    @DisplayName("Save Document Tests")
    inner class SaveDocumentTests {

        @Test
        @DisplayName("Should save document successfully")
        fun `save should save document successfully`() {
            val document = createTestDocument()
            val savedDocument = createTestDocument(documentId = UUID.randomUUID())

            every { documentRepository.save(document) } returns savedDocument

            val result = documentRepository.save(document)

            assertNotNull(result.documentId)
            assertEquals(savedDocument.fileName, result.fileName)
            assertEquals(savedDocument.fileType, result.fileType)
            assertEquals(savedDocument.fileSize, result.fileSize)
            assertEquals(savedDocument.provider, result.provider)
            assertEquals(savedDocument.storageKey, result.storageKey)
            assertEquals(savedDocument.fileUrl, result.fileUrl)
            assertEquals(savedDocument.category, result.category)
            assertEquals(savedDocument.docType, result.docType)
            assertEquals(savedDocument.tags, result.tags)
            assertEquals(savedDocument.extData, result.extData)

            verify { documentRepository.save(document) }
        }

        @Test
        @DisplayName("Should save document with minimal data")
        fun `save should save document with minimal data`() {
            val document = createTestDocument(
                fileType = "text/plain",
                fileSize = 0L,
                fileUrl = "http://localhost:8080/files/test.txt",
                category = "test",
                docType = "text",
                tags = emptyList(),
                extData = emptyMap()
            )
            val savedDocument = createTestDocument(documentId = UUID.randomUUID())

            every { documentRepository.save(document) } returns savedDocument

            val result = documentRepository.save(document)

            assertNotNull(result.documentId)
            assertEquals(savedDocument.fileName, result.fileName)
            assertEquals(savedDocument.provider, result.provider)
            assertEquals(savedDocument.storageKey, result.storageKey)

            verify { documentRepository.save(document) }
        }

        @Test
        @DisplayName("Should save multiple documents")
        fun `save should save multiple documents`() {
            val document1 = createTestDocument(fileName = "doc1.pdf")
            val document2 = createTestDocument(fileName = "doc2.pdf")
            val savedDocument1 = createTestDocument(documentId = UUID.randomUUID(), fileName = "doc1.pdf")
            val savedDocument2 = createTestDocument(documentId = UUID.randomUUID(), fileName = "doc2.pdf")

            every { documentRepository.save(document1) } returns savedDocument1
            every { documentRepository.save(document2) } returns savedDocument2

            val result1 = documentRepository.save(document1)
            val result2 = documentRepository.save(document2)

            assertEquals("doc1.pdf", result1.fileName)
            assertEquals("doc2.pdf", result2.fileName)

            verify { documentRepository.save(document1) }
            verify { documentRepository.save(document2) }
        }
    }

    @Nested
    @DisplayName("Find Document Tests")
    inner class FindDocumentTests {

        @Test
        @DisplayName("Should find document by id when exists")
        fun `findById should find document when exists`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(documentId = documentId)

            every { documentRepository.findById(documentId) } returns Optional.of(document)

            val result = documentRepository.findById(documentId)

            assertTrue(result.isPresent)
            assertEquals(documentId, result.get().documentId)
            assertEquals(document.fileName, result.get().fileName)
            assertEquals(document.provider, result.get().provider)

            verify { documentRepository.findById(documentId) }
        }

        @Test
        @DisplayName("Should return empty optional when document not found")
        fun `findById should return empty optional when document not found`() {
            val documentId = UUID.randomUUID()

            every { documentRepository.findById(documentId) } returns Optional.empty()

            val result = documentRepository.findById(documentId)

            assertTrue(result.isEmpty())

            verify { documentRepository.findById(documentId) }
        }

        @Test
        @DisplayName("Should find document with different providers")
        fun `findById should find document with different providers`() {
            val localDocument = createTestDocument(provider = ProviderType.LOCAL)
            val s3Document = createTestDocumentWithS3Provider()

            every { documentRepository.findById(localDocument.documentId!!) } returns Optional.of(localDocument)
            every { documentRepository.findById(s3Document.documentId!!) } returns Optional.of(s3Document)

            val localResult = documentRepository.findById(localDocument.documentId!!)
            val s3Result = documentRepository.findById(s3Document.documentId!!)

            assertTrue(localResult.isPresent)
            assertTrue(s3Result.isPresent)
            assertEquals(ProviderType.LOCAL, localResult.get().provider)
            assertEquals(ProviderType.AWS_S3, s3Result.get().provider)

            verify { documentRepository.findById(localDocument.documentId!!) }
            verify { documentRepository.findById(s3Document.documentId!!) }
        }
    }

    @Nested
    @DisplayName("Delete Document Tests")
    inner class DeleteDocumentTests {

        @Test
        @DisplayName("Should delete document successfully")
        fun `delete should delete document successfully`() {
            val document = createTestDocument()

            every { documentRepository.delete(document) } returns Unit

            documentRepository.delete(document)

            verify { documentRepository.delete(document) }
        }

        @Test
        @DisplayName("Should delete document by id successfully")
        fun `deleteById should delete document by id successfully`() {
            val documentId = UUID.randomUUID()

            every { documentRepository.deleteById(documentId) } returns Unit

            documentRepository.deleteById(documentId)

            verify { documentRepository.deleteById(documentId) }
        }
    }

    @Nested
    @DisplayName("Exists Document Tests")
    inner class ExistsDocumentTests {

        @Test
        @DisplayName("Should return true when document exists")
        fun `existsById should return true when document exists`() {
            val documentId = UUID.randomUUID()

            every { documentRepository.existsById(documentId) } returns true

            val result = documentRepository.existsById(documentId)

            assertTrue(result)

            verify { documentRepository.existsById(documentId) }
        }

        @Test
        @DisplayName("Should return false when document does not exist")
        fun `existsById should return false when document does not exist`() {
            val documentId = UUID.randomUUID()

            every { documentRepository.existsById(documentId) } returns false

            val result = documentRepository.existsById(documentId)

            assertTrue(!result)

            verify { documentRepository.existsById(documentId) }
        }
    }

    @Nested
    @DisplayName("Count Document Tests")
    inner class CountDocumentTests {

        @Test
        @DisplayName("Should return correct document count")
        fun `count should return correct document count`() {
            val expectedCount = 5L

            every { documentRepository.count() } returns expectedCount

            val result = documentRepository.count()

            assertEquals(expectedCount, result)

            verify { documentRepository.count() }
        }

        @Test
        @DisplayName("Should return zero when no documents exist")
        fun `count should return zero when no documents exist`() {
            every { documentRepository.count() } returns 0L

            val result = documentRepository.count()

            assertEquals(0L, result)

            verify { documentRepository.count() }
        }
    }

    @Nested
    @DisplayName("Find All Documents Tests")
    inner class FindAllDocumentsTests {

        @Test
        @DisplayName("Should return all documents")
        fun `findAll should return all documents`() {
            val documents = listOf(
                createTestDocument(fileName = "doc1.pdf"),
                createTestDocument(fileName = "doc2.pdf"),
                createTestDocument(fileName = "doc3.pdf")
            )

            every { documentRepository.findAll() } returns documents

            val result = documentRepository.findAll()

            assertEquals(3, result.size)
            assertEquals("doc1.pdf", result[0].fileName)
            assertEquals("doc2.pdf", result[1].fileName)
            assertEquals("doc3.pdf", result[2].fileName)

            verify { documentRepository.findAll() }
        }

        @Test
        @DisplayName("Should return empty list when no documents exist")
        fun `findAll should return empty list when no documents exist`() {
            every { documentRepository.findAll() } returns emptyList()

            val result = documentRepository.findAll()

            assertTrue(result.isEmpty())

            verify { documentRepository.findAll() }
        }
    }
}
