package com.nivasafinance.features.document.service.impl

import com.nivasafinance.features.document.enum.AllowedDocumentType
import com.nivasafinance.features.document.enum.ProviderType
import com.nivasafinance.features.document.service.DocumentUtilityService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class DocumentUtilityServiceImpl : DocumentUtilityService {

    companion object {
        private const val BYTES_PER_KB = 1024
        private const val KB_PER_MB = 1024
    }

    override fun generateStorageKey(
        userId: String,
        fileName: String,
        category: String?,
        provider: ProviderType
    ): String {
        val timestamp = System.currentTimeMillis()
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val fileExtension = fileName.substringAfterLast('.', "")
        val baseFileName = fileName.substringBeforeLast('.')
        val sanitizedFileName = baseFileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")

        return when (provider) {
            ProviderType.AWS_S3 -> "documents/$userId/$date/$timestamp-$sanitizedFileName.$fileExtension"
            ProviderType.LOCAL -> "docs/$userId/$timestamp-$sanitizedFileName.$fileExtension"
        }
    }

    override fun validateFileSize(fileSize: Long, maxSizeInMB: Long): Boolean {
        val maxSizeInBytes = maxSizeInMB * BYTES_PER_KB * KB_PER_MB
        return fileSize <= maxSizeInBytes
    }

    override fun validateFileType(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return AllowedDocumentType.fromExtension(extension) != null
    }

    override fun getAllowedDocumentType(fileName: String): AllowedDocumentType? {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return AllowedDocumentType.fromExtension(extension)
    }
}
