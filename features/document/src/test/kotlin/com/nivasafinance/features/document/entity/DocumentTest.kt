package com.nivasafinance.features.document.entity

import com.nivasafinance.features.document.DocumentTestUtils.createTestDocument
import com.nivasafinance.features.document.DocumentTestUtils.createTestDocumentWithMinimalData
import com.nivasafinance.features.document.DocumentTestUtils.createTestDocumentWithS3Provider
import com.nivasafinance.features.document.enum.ProviderType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("Document Entity Tests")
class DocumentTest {

    @Nested
    @DisplayName("Document Creation Tests")
    inner class DocumentCreationTests {

        @Test
        @DisplayName("Should create document with all fields")
        fun `should create document with all fields`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocument(
                documentId = documentId,
                fileName = "test-document.pdf",
                fileType = "application/pdf",
                fileSize = 1024L,
                provider = ProviderType.LOCAL,
                storageKey = "test/storage/key",
                fileUrl = "http://localhost:8080/files/test-document.pdf",
                category = "KYC",
                docType = "PAN_CARD",
                tags = listOf("kyc", "pan", "verification"),
                extData = mapOf("version" to "1.0", "encrypted" to false)
            )

            assertEquals(documentId, document.documentId)
            assertEquals("test-document.pdf", document.fileName)
            assertEquals("application/pdf", document.fileType)
            assertEquals(1024L, document.fileSize)
            assertEquals(ProviderType.LOCAL, document.provider)
            assertEquals("test/storage/key", document.storageKey)
            assertEquals("http://localhost:8080/files/test-document.pdf", document.fileUrl)
            assertEquals("KYC", document.category)
            assertEquals("PAN_CARD", document.docType)
            assertEquals(listOf("kyc", "pan", "verification"), document.tags)
            assertEquals(mapOf("version" to "1.0", "encrypted" to false), document.extData)
        }

        @Test
        @DisplayName("Should create document with minimal required fields")
        fun `should create document with minimal required fields`() {
            val documentId = UUID.randomUUID()
            val document = createTestDocumentWithMinimalData(
                documentId = documentId,
                fileName = "minimal-doc.txt",
                provider = ProviderType.LOCAL,
                storageKey = "minimal/storage/key"
            )

            assertEquals(documentId, document.documentId)
            assertEquals("minimal-doc.txt", document.fileName)
            assertEquals(ProviderType.LOCAL, document.provider)
            assertEquals("minimal/storage/key", document.storageKey)
            assertNull(document.fileType)
            assertNull(document.fileSize)
            assertNull(document.fileUrl)
            assertNull(document.category)
            assertNull(document.docType)
            assertNull(document.tags)
            assertNull(document.extData)
        }

        @Test
        @DisplayName("Should create document with AWS S3 provider")
        fun `should create document with AWS S3 provider`() {
            val document = createTestDocumentWithS3Provider()

            assertEquals(ProviderType.AWS_S3, document.provider)
            assertEquals("https://s3.amazonaws.com/bucket/s3-document.pdf", document.fileUrl)
            assertEquals("LOAN_DOCS", document.category)
            assertEquals("BANK_STATEMENT", document.docType)
            assertEquals(listOf("loan", "bank", "statement"), document.tags)
            assertEquals(mapOf("bucket" to "my-bucket", "region" to "us-east-1"), document.extData)
        }
    }

    @Nested
    @DisplayName("Document Field Validation Tests")
    inner class DocumentFieldValidationTests {

        @Test
        @DisplayName("Should handle null optional fields correctly")
        fun `should handle null optional fields correctly`() {
            val document = createTestDocumentWithMinimalData()

            assertNotNull(document.documentId)
            assertNotNull(document.fileName)
            assertNotNull(document.provider)
            assertNotNull(document.storageKey)
            assertNull(document.fileType)
            assertNull(document.fileSize)
            assertNull(document.fileUrl)
            assertNull(document.category)
            assertNull(document.docType)
            assertNull(document.tags)
            assertNull(document.extData)
        }

        @Test
        @DisplayName("Should handle empty tags list")
        fun `should handle empty tags list`() {
            val document = createTestDocument(tags = emptyList())

            assertNotNull(document.tags)
            assertEquals(0, document.tags!!.size)
        }

        @Test
        @DisplayName("Should handle empty extData map")
        fun `should handle empty extData map`() {
            val document = createTestDocument(extData = emptyMap())

            assertNotNull(document.extData)
            assertEquals(0, document.extData!!.size)
        }

        @Test
        @DisplayName("Should handle large file sizes")
        fun `should handle large file sizes`() {
            val largeFileSize = 10L * 1024 * 1024 * 1024 // 10GB
            val document = createTestDocument(fileSize = largeFileSize)

            assertEquals(largeFileSize, document.fileSize)
        }

        @Test
        @DisplayName("Should handle special characters in file names")
        fun `should handle special characters in file names`() {
            val specialFileName = "test-file with spaces & symbols (2024).pdf"
            val document = createTestDocument(fileName = specialFileName)

            assertEquals(specialFileName, document.fileName)
        }

        @Test
        @DisplayName("Should handle long storage keys")
        fun `should handle long storage keys`() {
            val longStorageKey = "very/long/storage/path/with/many/levels/and/subdirectories/file.pdf"
            val document = createTestDocument(storageKey = longStorageKey)

            assertEquals(longStorageKey, document.storageKey)
        }
    }

    @Nested
    @DisplayName("Document Provider Type Tests")
    inner class DocumentProviderTypeTests {

        @Test
        @DisplayName("Should support LOCAL provider type")
        fun `should support LOCAL provider type`() {
            val document = createTestDocument(provider = ProviderType.LOCAL)

            assertEquals(ProviderType.LOCAL, document.provider)
        }

        @Test
        @DisplayName("Should support AWS_S3 provider type")
        fun `should support AWS_S3 provider type`() {
            val document = createTestDocument(provider = ProviderType.AWS_S3)

            assertEquals(ProviderType.AWS_S3, document.provider)
        }
    }

    @Nested
    @DisplayName("Document ExtData Tests")
    inner class DocumentExtDataTests {

        @Test
        @DisplayName("Should handle complex extData with various data types")
        fun `should handle complex extData with various data types`() {
            val complexExtData = mapOf(
                "stringValue" to "test",
                "intValue" to 123,
                "doubleValue" to 45.67,
                "booleanValue" to true,
                "listValue" to listOf("item1", "item2"),
                "nestedMap" to mapOf("key" to "value")
            )
            val document = createTestDocument(extData = complexExtData)

            assertEquals(complexExtData, document.extData)
        }

        @Test
        @DisplayName("Should handle null values in extData")
        fun `should handle null values in extData`() {
            val extDataWithNulls = mapOf(
                "validKey" to "validValue",
                "nullKey" to "null"
            )
            val document = createTestDocument(extData = extDataWithNulls)

            assertEquals(extDataWithNulls, document.extData)
        }
    }
}
