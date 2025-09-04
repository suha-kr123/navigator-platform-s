package com.nivasafinance.features.document.enum

enum class AllowedDocumentType(
    val extensions: Set<String>,
    val mimeTypes: Set<String>,
    val maxSizeInMB: Long = 10
) {
    PDF(
        extensions = setOf("pdf"),
        mimeTypes = setOf("application/pdf"),
        maxSizeInMB = 1L
    ),
    DOC(
        extensions = setOf("doc"),
        mimeTypes = setOf("application/msword"),
        maxSizeInMB = 1L
    ),
    DOCX(
        extensions = setOf("docx"),
        mimeTypes = setOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        maxSizeInMB = 1L
    ),
    TXT(
        extensions = setOf("txt"),
        mimeTypes = setOf("text/plain"),
        maxSizeInMB = 1L
    ),
    JPG(
        extensions = setOf("jpg", "jpeg"),
        mimeTypes = setOf("image/jpeg"),
        maxSizeInMB = 1L
    ),
    PNG(
        extensions = setOf("png"),
        mimeTypes = setOf("image/png"),
        maxSizeInMB = 1L
    ),
    GIF(
        extensions = setOf("gif"),
        mimeTypes = setOf("image/gif"),
        maxSizeInMB = 1L
    ),
    XLSX(
        extensions = setOf("xlsx"),
        mimeTypes = setOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        maxSizeInMB = 1L
    ),
    XLS(
        extensions = setOf("xls"),
        mimeTypes = setOf("application/vnd.ms-excel"),
        maxSizeInMB = 1L
    );

    companion object {
        fun fromExtension(extension: String): AllowedDocumentType? {
            val normalizedExtension = extension.lowercase().trim()
            return values().find { normalizedExtension in it.extensions }
        }

        fun fromMimeType(mimeType: String): AllowedDocumentType? {
            val normalizedMimeType = mimeType.lowercase().trim()
            return values().find { normalizedMimeType in it.mimeTypes }
        }

        fun getAllowedExtensions(): Set<String> {
            return values().flatMap { it.extensions }.toSet()
        }

        fun getAllowedMimeTypes(): Set<String> {
            return values().flatMap { it.mimeTypes }.toSet()
        }
    }
}
