package com.nivasafinance.features.document.enum

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("AllowedDocumentType Enum Tests")
class AllowedDocumentTypeTest {

    @Nested
    @DisplayName("Enum Values Tests")
    inner class EnumValuesTests {

        @Test
        @DisplayName("Should have all expected document types")
        fun `should have all expected document types`() {
            val values = AllowedDocumentType.values()

            assertEquals(9, values.size)
            assertEquals(AllowedDocumentType.PDF, values[0])
            assertEquals(AllowedDocumentType.DOC, values[1])
            assertEquals(AllowedDocumentType.DOCX, values[2])
            assertEquals(AllowedDocumentType.TXT, values[3])
            assertEquals(AllowedDocumentType.JPG, values[4])
            assertEquals(AllowedDocumentType.PNG, values[5])
            assertEquals(AllowedDocumentType.GIF, values[6])
            assertEquals(AllowedDocumentType.XLSX, values[7])
            assertEquals(AllowedDocumentType.XLS, values[8])
        }

        @Test
        @DisplayName("Should have correct properties for PDF")
        fun `should have correct properties for PDF`() {
            val pdf = AllowedDocumentType.PDF

            assertEquals(setOf("pdf"), pdf.extensions)
            assertEquals(setOf("application/pdf"), pdf.mimeTypes)
            assertEquals(1L, pdf.maxSizeInMB)
        }

        @Test
        @DisplayName("Should have correct properties for DOC")
        fun `should have correct properties for DOC`() {
            val doc = AllowedDocumentType.DOC

            assertEquals(setOf("doc"), doc.extensions)
            assertEquals(setOf("application/msword"), doc.mimeTypes)
            assertEquals(1L, doc.maxSizeInMB)
        }

        @Test
        @DisplayName("Should have correct properties for DOCX")
        fun `should have correct properties for DOCX`() {
            val docx = AllowedDocumentType.DOCX

            assertEquals(setOf("docx"), docx.extensions)
            assertEquals(
                setOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                docx.mimeTypes
            )
            assertEquals(1L, docx.maxSizeInMB)
        }

        @Test
        @DisplayName("Should have correct properties for TXT")
        fun `should have correct properties for TXT`() {
            val txt = AllowedDocumentType.TXT

            assertEquals(setOf("txt"), txt.extensions)
            assertEquals(setOf("text/plain"), txt.mimeTypes)
            assertEquals(1L, txt.maxSizeInMB)
        }

        @Test
        @DisplayName("Should have correct properties for JPG")
        fun `should have correct properties for JPG`() {
            val jpg = AllowedDocumentType.JPG

            assertEquals(setOf("jpg", "jpeg"), jpg.extensions)
            assertEquals(setOf("image/jpeg"), jpg.mimeTypes)
            assertEquals(1L, jpg.maxSizeInMB)
        }

        @Test
        @DisplayName("Should have correct properties for PNG")
        fun `should have correct properties for PNG`() {
            val png = AllowedDocumentType.PNG

            assertEquals(setOf("png"), png.extensions)
            assertEquals(setOf("image/png"), png.mimeTypes)
            assertEquals(1L, png.maxSizeInMB)
        }

        @Test
        @DisplayName("Should have correct properties for GIF")
        fun `should have correct properties for GIF`() {
            val gif = AllowedDocumentType.GIF

            assertEquals(setOf("gif"), gif.extensions)
            assertEquals(setOf("image/gif"), gif.mimeTypes)
            assertEquals(1L, gif.maxSizeInMB)
        }

        @Test
        @DisplayName("Should have correct properties for XLSX")
        fun `should have correct properties for XLSX`() {
            val xlsx = AllowedDocumentType.XLSX

            assertEquals(setOf("xlsx"), xlsx.extensions)
            assertEquals(setOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), xlsx.mimeTypes)
            assertEquals(1L, xlsx.maxSizeInMB)
        }

        @Test
        @DisplayName("Should have correct properties for XLS")
        fun `should have correct properties for XLS`() {
            val xls = AllowedDocumentType.XLS

            assertEquals(setOf("xls"), xls.extensions)
            assertEquals(setOf("application/vnd.ms-excel"), xls.mimeTypes)
            assertEquals(1L, xls.maxSizeInMB)
        }
    }

    @Nested
    @DisplayName("FromExtension Tests")
    inner class FromExtensionTests {

        @Test
        @DisplayName("Should find PDF from extension")
        fun `should find PDF from extension`() {
            assertEquals(AllowedDocumentType.PDF, AllowedDocumentType.fromExtension("pdf"))
            assertEquals(AllowedDocumentType.PDF, AllowedDocumentType.fromExtension("PDF"))
            assertEquals(AllowedDocumentType.PDF, AllowedDocumentType.fromExtension(" Pdf "))
        }

        @Test
        @DisplayName("Should find DOC from extension")
        fun `should find DOC from extension`() {
            assertEquals(AllowedDocumentType.DOC, AllowedDocumentType.fromExtension("doc"))
            assertEquals(AllowedDocumentType.DOC, AllowedDocumentType.fromExtension("DOC"))
            assertEquals(AllowedDocumentType.DOC, AllowedDocumentType.fromExtension(" Doc "))
        }

        @Test
        @DisplayName("Should find DOCX from extension")
        fun `should find DOCX from extension`() {
            assertEquals(AllowedDocumentType.DOCX, AllowedDocumentType.fromExtension("docx"))
            assertEquals(AllowedDocumentType.DOCX, AllowedDocumentType.fromExtension("DOCX"))
            assertEquals(AllowedDocumentType.DOCX, AllowedDocumentType.fromExtension(" Docx "))
        }

        @Test
        @DisplayName("Should find TXT from extension")
        fun `should find TXT from extension`() {
            assertEquals(AllowedDocumentType.TXT, AllowedDocumentType.fromExtension("txt"))
            assertEquals(AllowedDocumentType.TXT, AllowedDocumentType.fromExtension("TXT"))
            assertEquals(AllowedDocumentType.TXT, AllowedDocumentType.fromExtension(" Txt "))
        }

        @Test
        @DisplayName("Should find JPG from both jpg and jpeg extensions")
        fun `should find JPG from both jpg and jpeg extensions`() {
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromExtension("jpg"))
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromExtension("jpeg"))
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromExtension("JPG"))
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromExtension("JPEG"))
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromExtension(" Jpg "))
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromExtension(" Jpeg "))
        }

        @Test
        @DisplayName("Should find PNG from extension")
        fun `should find PNG from extension`() {
            assertEquals(AllowedDocumentType.PNG, AllowedDocumentType.fromExtension("png"))
            assertEquals(AllowedDocumentType.PNG, AllowedDocumentType.fromExtension("PNG"))
            assertEquals(AllowedDocumentType.PNG, AllowedDocumentType.fromExtension(" Png "))
        }

        @Test
        @DisplayName("Should find GIF from extension")
        fun `should find GIF from extension`() {
            assertEquals(AllowedDocumentType.GIF, AllowedDocumentType.fromExtension("gif"))
            assertEquals(AllowedDocumentType.GIF, AllowedDocumentType.fromExtension("GIF"))
            assertEquals(AllowedDocumentType.GIF, AllowedDocumentType.fromExtension(" Gif "))
        }

        @Test
        @DisplayName("Should find XLSX from extension")
        fun `should find XLSX from extension`() {
            assertEquals(AllowedDocumentType.XLSX, AllowedDocumentType.fromExtension("xlsx"))
            assertEquals(AllowedDocumentType.XLSX, AllowedDocumentType.fromExtension("XLSX"))
            assertEquals(AllowedDocumentType.XLSX, AllowedDocumentType.fromExtension(" Xlsx "))
        }

        @Test
        @DisplayName("Should find XLS from extension")
        fun `should find XLS from extension`() {
            assertEquals(AllowedDocumentType.XLS, AllowedDocumentType.fromExtension("xls"))
            assertEquals(AllowedDocumentType.XLS, AllowedDocumentType.fromExtension("XLS"))
            assertEquals(AllowedDocumentType.XLS, AllowedDocumentType.fromExtension(" Xls "))
        }

        @Test
        @DisplayName("Should return null for unsupported extensions")
        fun `should return null for unsupported extensions`() {
            assertNull(AllowedDocumentType.fromExtension("xyz"))
            assertNull(AllowedDocumentType.fromExtension("mp4"))
            assertNull(AllowedDocumentType.fromExtension("zip"))
            assertNull(AllowedDocumentType.fromExtension(""))
            assertNull(AllowedDocumentType.fromExtension("   "))
            assertNull(AllowedDocumentType.fromExtension("unknown"))
        }
    }

    @Nested
    @DisplayName("FromMimeType Tests")
    inner class FromMimeTypeTests {

        @Test
        @DisplayName("Should find PDF from MIME type")
        fun `should find PDF from MIME type`() {
            assertEquals(AllowedDocumentType.PDF, AllowedDocumentType.fromMimeType("application/pdf"))
            assertEquals(AllowedDocumentType.PDF, AllowedDocumentType.fromMimeType("APPLICATION/PDF"))
            assertEquals(AllowedDocumentType.PDF, AllowedDocumentType.fromMimeType(" Application/Pdf "))
        }

        @Test
        @DisplayName("Should find DOC from MIME type")
        fun `should find DOC from MIME type`() {
            assertEquals(AllowedDocumentType.DOC, AllowedDocumentType.fromMimeType("application/msword"))
            assertEquals(AllowedDocumentType.DOC, AllowedDocumentType.fromMimeType("APPLICATION/MSWORD"))
            assertEquals(AllowedDocumentType.DOC, AllowedDocumentType.fromMimeType(" Application/Msword "))
        }

        @Test
        @DisplayName("Should find DOCX from MIME type")
        fun `should find DOCX from MIME type`() {
            val mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            assertEquals(AllowedDocumentType.DOCX, AllowedDocumentType.fromMimeType(mimeType))
            assertEquals(AllowedDocumentType.DOCX, AllowedDocumentType.fromMimeType(mimeType.uppercase()))
            assertEquals(AllowedDocumentType.DOCX, AllowedDocumentType.fromMimeType(" $mimeType "))
        }

        @Test
        @DisplayName("Should find TXT from MIME type")
        fun `should find TXT from MIME type`() {
            assertEquals(AllowedDocumentType.TXT, AllowedDocumentType.fromMimeType("text/plain"))
            assertEquals(AllowedDocumentType.TXT, AllowedDocumentType.fromMimeType("TEXT/PLAIN"))
            assertEquals(AllowedDocumentType.TXT, AllowedDocumentType.fromMimeType(" Text/Plain "))
        }

        @Test
        @DisplayName("Should find JPG from MIME type")
        fun `should find JPG from MIME type`() {
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromMimeType("image/jpeg"))
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromMimeType("IMAGE/JPEG"))
            assertEquals(AllowedDocumentType.JPG, AllowedDocumentType.fromMimeType(" Image/Jpeg "))
        }

        @Test
        @DisplayName("Should find PNG from MIME type")
        fun `should find PNG from MIME type`() {
            assertEquals(AllowedDocumentType.PNG, AllowedDocumentType.fromMimeType("image/png"))
            assertEquals(AllowedDocumentType.PNG, AllowedDocumentType.fromMimeType("IMAGE/PNG"))
            assertEquals(AllowedDocumentType.PNG, AllowedDocumentType.fromMimeType(" Image/Png "))
        }

        @Test
        @DisplayName("Should find GIF from MIME type")
        fun `should find GIF from MIME type`() {
            assertEquals(AllowedDocumentType.GIF, AllowedDocumentType.fromMimeType("image/gif"))
            assertEquals(AllowedDocumentType.GIF, AllowedDocumentType.fromMimeType("IMAGE/GIF"))
            assertEquals(AllowedDocumentType.GIF, AllowedDocumentType.fromMimeType(" Image/Gif "))
        }

        @Test
        @DisplayName("Should find XLSX from MIME type")
        fun `should find XLSX from MIME type`() {
            val mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            assertEquals(AllowedDocumentType.XLSX, AllowedDocumentType.fromMimeType(mimeType))
            assertEquals(AllowedDocumentType.XLSX, AllowedDocumentType.fromMimeType(mimeType.uppercase()))
            assertEquals(AllowedDocumentType.XLSX, AllowedDocumentType.fromMimeType(" $mimeType "))
        }

        @Test
        @DisplayName("Should find XLS from MIME type")
        fun `should find XLS from MIME type`() {
            assertEquals(AllowedDocumentType.XLS, AllowedDocumentType.fromMimeType("application/vnd.ms-excel"))
            assertEquals(AllowedDocumentType.XLS, AllowedDocumentType.fromMimeType("APPLICATION/VND.MS-EXCEL"))
            assertEquals(AllowedDocumentType.XLS, AllowedDocumentType.fromMimeType(" Application/Vnd.Ms-Excel "))
        }

        @Test
        @DisplayName("Should return null for unsupported MIME types")
        fun `should return null for unsupported MIME types`() {
            assertNull(AllowedDocumentType.fromMimeType("video/mp4"))
            assertNull(AllowedDocumentType.fromMimeType("audio/mp3"))
            assertNull(AllowedDocumentType.fromMimeType("application/zip"))
            assertNull(AllowedDocumentType.fromMimeType(""))
            assertNull(AllowedDocumentType.fromMimeType("   "))
            assertNull(AllowedDocumentType.fromMimeType("unknown/type"))
        }
    }

    @Nested
    @DisplayName("GetAllowedExtensions Tests")
    inner class GetAllowedExtensionsTests {

        @Test
        @DisplayName("Should return all allowed extensions")
        fun `should return all allowed extensions`() {
            val allowedExtensions = AllowedDocumentType.getAllowedExtensions()

            val expectedExtensions = setOf(
                "pdf", "doc", "docx", "txt", "jpg", "jpeg", "png", "gif", "xlsx", "xls"
            )

            assertEquals(expectedExtensions, allowedExtensions)
        }

        @Test
        @DisplayName("Should return unique extensions only")
        fun `should return unique extensions only`() {
            val allowedExtensions = AllowedDocumentType.getAllowedExtensions()

            // Check that there are no duplicates
            assertEquals(allowedExtensions.size, allowedExtensions.toSet().size)
        }

        @Test
        @DisplayName("Should contain all individual enum extensions")
        fun `should contain all individual enum extensions`() {
            val allowedExtensions = AllowedDocumentType.getAllowedExtensions()

            AllowedDocumentType.values().forEach { documentType ->
                documentType.extensions.forEach { extension ->
                    assert(
                        allowedExtensions.contains(extension)
                    ) { "Extension $extension should be in allowed extensions" }
                }
            }
        }
    }

    @Nested
    @DisplayName("GetAllowedMimeTypes Tests")
    inner class GetAllowedMimeTypesTests {

        @Test
        @DisplayName("Should return all allowed MIME types")
        fun `should return all allowed MIME types`() {
            val allowedMimeTypes = AllowedDocumentType.getAllowedMimeTypes()

            val expectedMimeTypes = setOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain",
                "image/jpeg",
                "image/png",
                "image/gif",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
            )

            assertEquals(expectedMimeTypes, allowedMimeTypes)
        }

        @Test
        @DisplayName("Should return unique MIME types only")
        fun `should return unique MIME types only`() {
            val allowedMimeTypes = AllowedDocumentType.getAllowedMimeTypes()

            // Check that there are no duplicates
            assertEquals(allowedMimeTypes.size, allowedMimeTypes.toSet().size)
        }

        @Test
        @DisplayName("Should contain all individual enum MIME types")
        fun `should contain all individual enum MIME types`() {
            val allowedMimeTypes = AllowedDocumentType.getAllowedMimeTypes()

            AllowedDocumentType.values().forEach { documentType ->
                documentType.mimeTypes.forEach { mimeType ->
                    assert(
                        allowedMimeTypes.contains(mimeType)
                    ) { "MIME type $mimeType should be in allowed MIME types" }
                }
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    inner class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null-like extension strings")
        fun `should handle null-like extension strings`() {
            assertNull(AllowedDocumentType.fromExtension("null"))
            assertNull(AllowedDocumentType.fromExtension("NULL"))
        }

        @Test
        @DisplayName("Should handle extension with dots")
        fun `should handle extension with dots`() {
            assertNull(AllowedDocumentType.fromExtension(".pdf"))
            assertNull(AllowedDocumentType.fromExtension("..pdf"))
        }

        @Test
        @DisplayName("Should handle MIME type with parameters")
        fun `should handle MIME type with parameters`() {
            assertNull(AllowedDocumentType.fromMimeType("application/pdf; charset=utf-8"))
            assertNull(AllowedDocumentType.fromMimeType("image/jpeg; quality=0.8"))
        }

        @Test
        @DisplayName("Should handle very long extension strings")
        fun `should handle very long extension strings`() {
            val longExtension = "a".repeat(1000)
            assertNull(AllowedDocumentType.fromExtension(longExtension))
        }

        @Test
        @DisplayName("Should handle very long MIME type strings")
        fun `should handle very long MIME type strings`() {
            val longMimeType = "a".repeat(1000)
            assertNull(AllowedDocumentType.fromMimeType(longMimeType))
        }
    }
}
