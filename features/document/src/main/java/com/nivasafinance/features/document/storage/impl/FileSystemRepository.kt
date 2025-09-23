package com.nivasafinance.features.document.storage.impl

import com.nivasafinance.features.document.storage.ContentRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

@Component
class FileSystemRepository : ContentRepository {

    @Value("\${local.storage.base-path:/tmp/documents}")
    @Suppress("VarCouldBeVal") // lateinit requires var, not val
    private lateinit var basePath: String

    override fun saveFile(inputStream: InputStream, documentPath: String): String {
        val file = File(basePath, documentPath)
        file.parentFile.mkdirs()

        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        return "file://$basePath/$documentPath"
    }

    override fun deleteFile(storageKey: String) {
        val file = File(basePath, storageKey)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun fetchFile(storageKey: String): InputStream {
        val file = File(basePath, storageKey)
        require(file.exists()) { "File not found: $storageKey" }
        return FileInputStream(file)
    }

    override fun getSignedDownloadUrl(storageKey: String, expiresIn: Long): String? {
        // For local file system, we don't support signed URLs
        // Return null to indicate this provider doesn't support signed URLs
        return null
    }
}
