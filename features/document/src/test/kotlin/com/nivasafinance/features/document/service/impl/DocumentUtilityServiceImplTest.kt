package com.nivasafinance.features.document.service.impl

import com.nivasafinance.features.document.enum.AllowedDocumentType
import com.nivasafinance.features.document.enum.ProviderType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("DocumentUtilityServiceImpl Tests")
class DocumentUtilityServiceImplTest {

    private lateinit var documentUtilityService: DocumentUtilityServiceImpl

    @BeforeEach
    fun setUp() {
        documentUtilityService = DocumentUtilityServiceImpl()
    }

    @Nested
    @DisplayName("Generate Storage Key Tests")
    inner class GenerateStorageKeyTests {

        @Test
        @DisplayName("Should generate storage key for AWS S3 provider")
        fun `should generate storage key for AWS S3 provider`() {
            val userId = "testuser123"
            val fileName = "document.pdf"
            val category = "KYC"
            val provider = ProviderType.AWS_S3

            val storageKey = documentUtilityService.generateStorageKey(userId, fileName, category, provider)

            assertNotNull(storageKey)
            assertTrue(storageKey.startsWith("documents/$userId/"))
            assertTrue(storageKey.contains("document.pdf"))
            assertTrue(storageKey.matches(Regex("documents/$userId/\\d{4}/\\d{2}/\\d{2}/\\d+-document\\.pdf")))
        }

        @Test
        @DisplayName("Should generate storage key for LOCAL provider")
        fun `should generate storage key for LOCAL provider`() {
            val userId = "testuser123"
            val fileName = "document.pdf"
            val category = "KYC"
            val provider = ProviderType.LOCAL

            val storageKey = documentUtilityService.generateStorageKey(userId, fileName, category, provider)

            assertNotNull(storageKey)
            assertTrue(storageKey.startsWith("docs/$userId/"))
            assertTrue(storageKey.contains("document.pdf"))
            assertTrue(storageKey.matches(Regex("docs/$userId/\\d+-document\\.pdf")))
        }

        @Test
        @DisplayName("Should generate storage key with null category")
        fun `should generate storage key with null category`() {
            val userId = "testuser123"
            val fileName = "document.pdf"
            val provider = ProviderType.AWS_S3

            val storageKey = documentUtilityService.generateStorageKey(userId, fileName, null, provider)

            assertNotNull(storageKey)
            assertTrue(storageKey.startsWith("documents/$userId/"))
            assertTrue(storageKey.contains("document.pdf"))
        }

        @Test
        @DisplayName("Should sanitize file name with special characters")
        fun `should sanitize file name with special characters`() {
            val userId = "testuser123"
            val fileName = "test file with spaces & symbols (2024).pdf"
            val provider = ProviderType.AWS_S3

            val storageKey = documentUtilityService.generateStorageKey(userId, fileName, null, provider)

            assertNotNull(storageKey)
            assertTrue(storageKey.contains("test_file_with_spaces___symbols__2024_.pdf"))
        }

        @Test
        @DisplayName("Should handle file name without extension")
        fun `should handle file name without extension`() {
            val userId = "testuser123"
            val fileName = "document"
            val provider = ProviderType.AWS_S3

            val storageKey = documentUtilityService.generateStorageKey(userId, fileName, null, provider)

            assertNotNull(storageKey)
            assertTrue(storageKey.contains("document"))
        }

        @Test
        @DisplayName("Should handle empty file name")
        fun `should handle empty file name`() {
            val userId = "testuser123"
            val fileName = ""
            val provider = ProviderType.AWS_S3

            val storageKey = documentUtilityService.generateStorageKey(userId, fileName, null, provider)

            assertNotNull(storageKey)
            assertTrue(storageKey.startsWith("documents/$userId/"))
        }

        @Test
        @DisplayName("Should generate unique storage keys for same input")
        fun `should generate unique storage keys for same input`() {
            val userId = "testuser123"
            val fileName = "document.pdf"
            val provider = ProviderType.AWS_S3

            val storageKey1 = documentUtilityService.generateStorageKey(userId, fileName, null, provider)
            // Add small delay to ensure different timestamps
            Thread.sleep(1)
            val storageKey2 = documentUtilityService.generateStorageKey(userId, fileName, null, provider)

            assertNotNull(storageKey1)
            assertNotNull(storageKey2)
            assertTrue(storageKey1 != storageKey2, "Storage keys should be unique due to timestamp")
        }

        @Test
        @DisplayName("Should handle very long file names")
        fun `should handle very long file names`() {
            val userId = "testuser123"
            val fileName = "a".repeat(1000) + ".pdf"
            val provider = ProviderType.AWS_S3

            val storageKey = documentUtilityService.generateStorageKey(userId, fileName, null, provider)

            assertNotNull(storageKey)
            assertTrue(storageKey.length > 0)
        }
    }

    @Nested
    @DisplayName("Validate File Size Tests")
    inner class ValidateFileSizeTests {

        @Test
        @DisplayName("Should return true for valid file size")
        fun `should return true for valid file size`() {
            val fileSize = 5 * 1024 * 1024L // 5MB
            val maxSizeInMB = 10L

            val isValid = documentUtilityService.validateFileSize(fileSize, maxSizeInMB)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for file size equal to max size")
        fun `should return true for file size equal to max size`() {
            val fileSize = 10 * 1024 * 1024L // 10MB
            val maxSizeInMB = 10L

            val isValid = documentUtilityService.validateFileSize(fileSize, maxSizeInMB)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return false for file size exceeding max size")
        fun `should return false for file size exceeding max size`() {
            val fileSize = 15 * 1024 * 1024L // 15MB
            val maxSizeInMB = 10L

            val isValid = documentUtilityService.validateFileSize(fileSize, maxSizeInMB)

            assertFalse(isValid)
        }

        @Test
        @DisplayName("Should return true for zero file size")
        fun `should return true for zero file size`() {
            val fileSize = 0L
            val maxSizeInMB = 10L

            val isValid = documentUtilityService.validateFileSize(fileSize, maxSizeInMB)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should handle very large max size")
        fun `should handle very large max size`() {
            val fileSize = 100 * 1024 * 1024L // 100MB
            val maxSizeInMB = 1000L // 1GB

            val isValid = documentUtilityService.validateFileSize(fileSize, maxSizeInMB)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should handle very small max size")
        fun `should handle very small max size`() {
            val fileSize = 1024L // 1KB
            val maxSizeInMB = 1L // 1MB

            val isValid = documentUtilityService.validateFileSize(fileSize, maxSizeInMB)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should handle negative file size")
        fun `should handle negative file size`() {
            val fileSize = -1024L
            val maxSizeInMB = 10L

            val isValid = documentUtilityService.validateFileSize(fileSize, maxSizeInMB)

            assertTrue(isValid) // Negative size should be considered valid (edge case)
        }

        @Test
        @DisplayName("Should handle zero max size")
        fun `should handle zero max size`() {
            val fileSize = 1024L
            val maxSizeInMB = 0L

            val isValid = documentUtilityService.validateFileSize(fileSize, maxSizeInMB)

            assertFalse(isValid)
        }
    }

    @Nested
    @DisplayName("Validate File Type Tests")
    inner class ValidateFileTypeTests {

        @Test
        @DisplayName("Should return true for valid PDF file")
        fun `should return true for valid PDF file`() {
            val fileName = "document.pdf"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid DOC file")
        fun `should return true for valid DOC file`() {
            val fileName = "document.doc"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid DOCX file")
        fun `should return true for valid DOCX file`() {
            val fileName = "document.docx"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid TXT file")
        fun `should return true for valid TXT file`() {
            val fileName = "document.txt"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid JPG file")
        fun `should return true for valid JPG file`() {
            val fileName = "image.jpg"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid JPEG file")
        fun `should return true for valid JPEG file`() {
            val fileName = "image.jpeg"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid PNG file")
        fun `should return true for valid PNG file`() {
            val fileName = "image.png"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid GIF file")
        fun `should return true for valid GIF file`() {
            val fileName = "image.gif"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid XLSX file")
        fun `should return true for valid XLSX file`() {
            val fileName = "spreadsheet.xlsx"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return true for valid XLS file")
        fun `should return true for valid XLS file`() {
            val fileName = "spreadsheet.xls"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should return false for invalid file type")
        fun `should return false for invalid file type`() {
            val fileName = "document.xyz"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertFalse(isValid)
        }

        @Test
        @DisplayName("Should return false for file without extension")
        fun `should return false for file without extension`() {
            val fileName = "document"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertFalse(isValid)
        }

        @Test
        @DisplayName("Should return false for empty file name")
        fun `should return false for empty file name`() {
            val fileName = ""

            val isValid = documentUtilityService.validateFileType(fileName)

            assertFalse(isValid)
        }

        @Test
        @DisplayName("Should handle case insensitive extensions")
        fun `should handle case insensitive extensions`() {
            val fileName1 = "document.PDF"
            val fileName2 = "document.Pdf"
            val fileName3 = "document.pdf"

            assertTrue(documentUtilityService.validateFileType(fileName1))
            assertTrue(documentUtilityService.validateFileType(fileName2))
            assertTrue(documentUtilityService.validateFileType(fileName3))
        }

        @Test
        @DisplayName("Should handle file names with multiple dots")
        fun `should handle file names with multiple dots`() {
            val fileName = "document.backup.pdf"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }

        @Test
        @DisplayName("Should handle file names with spaces")
        fun `should handle file names with spaces`() {
            val fileName = "my document.pdf"

            val isValid = documentUtilityService.validateFileType(fileName)

            assertTrue(isValid)
        }
    }

    @Nested
    @DisplayName("Get Allowed Document Type Tests")
    inner class GetAllowedDocumentTypeTests {

        @Test
        @DisplayName("Should return PDF type for PDF file")
        fun `should return PDF type for PDF file`() {
            val fileName = "document.pdf"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.PDF, documentType)
        }

        @Test
        @DisplayName("Should return DOC type for DOC file")
        fun `should return DOC type for DOC file`() {
            val fileName = "document.doc"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.DOC, documentType)
        }

        @Test
        @DisplayName("Should return DOCX type for DOCX file")
        fun `should return DOCX type for DOCX file`() {
            val fileName = "document.docx"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.DOCX, documentType)
        }

        @Test
        @DisplayName("Should return TXT type for TXT file")
        fun `should return TXT type for TXT file`() {
            val fileName = "document.txt"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.TXT, documentType)
        }

        @Test
        @DisplayName("Should return JPG type for JPG file")
        fun `should return JPG type for JPG file`() {
            val fileName = "image.jpg"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.JPG, documentType)
        }

        @Test
        @DisplayName("Should return JPG type for JPEG file")
        fun `should return JPG type for JPEG file`() {
            val fileName = "image.jpeg"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.JPG, documentType)
        }

        @Test
        @DisplayName("Should return PNG type for PNG file")
        fun `should return PNG type for PNG file`() {
            val fileName = "image.png"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.PNG, documentType)
        }

        @Test
        @DisplayName("Should return GIF type for GIF file")
        fun `should return GIF type for GIF file`() {
            val fileName = "image.gif"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.GIF, documentType)
        }

        @Test
        @DisplayName("Should return XLSX type for XLSX file")
        fun `should return XLSX type for XLSX file`() {
            val fileName = "spreadsheet.xlsx"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.XLSX, documentType)
        }

        @Test
        @DisplayName("Should return XLS type for XLS file")
        fun `should return XLS type for XLS file`() {
            val fileName = "spreadsheet.xls"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertEquals(AllowedDocumentType.XLS, documentType)
        }

        @Test
        @DisplayName("Should return null for invalid file type")
        fun `should return null for invalid file type`() {
            val fileName = "document.xyz"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertNull(documentType)
        }

        @Test
        @DisplayName("Should return null for file without extension")
        fun `should return null for file without extension`() {
            val fileName = "document"

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertNull(documentType)
        }

        @Test
        @DisplayName("Should return null for empty file name")
        fun `should return null for empty file name`() {
            val fileName = ""

            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertNull(documentType)
        }

        @Test
        @DisplayName("Should handle case insensitive extensions")
        fun `should handle case insensitive extensions`() {
            val fileName1 = "document.PDF"
            val fileName2 = "document.Pdf"
            val fileName3 = "document.pdf"

            assertEquals(AllowedDocumentType.PDF, documentUtilityService.getAllowedDocumentType(fileName1))
            assertEquals(AllowedDocumentType.PDF, documentUtilityService.getAllowedDocumentType(fileName2))
            assertEquals(AllowedDocumentType.PDF, documentUtilityService.getAllowedDocumentType(fileName3))
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    inner class EdgeCasesTests {

        @Test
        @DisplayName("Should handle file names with special characters")
        fun `should handle file names with special characters`() {
            val fileName = "test@file#with\$special%chars.pdf"

            val isValid = documentUtilityService.validateFileType(fileName)
            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertTrue(isValid)
            assertEquals(AllowedDocumentType.PDF, documentType)
        }

        @Test
        @DisplayName("Should handle very long file names")
        fun `should handle very long file names`() {
            val longFileName = "a".repeat(1000) + ".pdf"

            val isValid = documentUtilityService.validateFileType(longFileName)
            val documentType = documentUtilityService.getAllowedDocumentType(longFileName)

            assertTrue(isValid)
            assertEquals(AllowedDocumentType.PDF, documentType)
        }

        @Test
        @DisplayName("Should handle file names with only extension")
        fun `should handle file names with only extension`() {
            val fileName = ".pdf"

            val isValid = documentUtilityService.validateFileType(fileName)
            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertTrue(isValid)
            assertEquals(AllowedDocumentType.PDF, documentType)
        }

        @Test
        @DisplayName("Should handle file names with multiple extensions")
        fun `should handle file names with multiple extensions`() {
            val fileName = "document.backup.pdf"

            val isValid = documentUtilityService.validateFileType(fileName)
            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertTrue(isValid)
            assertEquals(AllowedDocumentType.PDF, documentType)
        }

        @Test
        @DisplayName("Should handle file names with whitespace")
        fun `should handle file names with whitespace`() {
            val fileName = "  document.pdf  "

            val isValid = documentUtilityService.validateFileType(fileName)
            val documentType = documentUtilityService.getAllowedDocumentType(fileName)

            assertTrue(isValid)
            assertEquals(AllowedDocumentType.PDF, documentType)
        }
    }
}
