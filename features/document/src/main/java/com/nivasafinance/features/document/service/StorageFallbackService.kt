package com.nivasafinance.features.document.service

import com.nivasafinance.features.document.enum.ProviderType
import com.nivasafinance.features.document.storage.ContentRepositoryFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
@Suppress("TooGenericExceptionCaught", "VarCouldBeVal")
class StorageFallbackService(
    private val contentRepositoryFactory: ContentRepositoryFactory
) {

    private val logger = LoggerFactory.getLogger(StorageFallbackService::class.java)

    @Value("\${document.storage.provider:LOCAL}")
    private lateinit var primaryProvider: String

    @Value("\${document.storage.fallback.enabled:true}")
    private val fallbackEnabled: Boolean = true

    fun saveFileWithFallback(inputStream: InputStream, documentPath: String): String {
        return try {
            val primaryRepo = contentRepositoryFactory.getRepository(ProviderType.valueOf(primaryProvider))
            primaryRepo.saveFile(inputStream, documentPath)
        } catch (e: Exception) {
            if (fallbackEnabled && primaryProvider != "LOCAL") {
                logger.warn("Primary storage failed, falling back to local storage: ${e.message}")
                val fallbackRepo = contentRepositoryFactory.getRepository(ProviderType.LOCAL)
                fallbackRepo.saveFile(inputStream, documentPath)
            } else {
                throw e
            }
        }
    }

    fun deleteFileWithFallback(documentPath: String) {
        try {
            val primaryRepo = contentRepositoryFactory.getRepository(ProviderType.valueOf(primaryProvider))
            primaryRepo.deleteFile(documentPath)
        } catch (e: Exception) {
            if (fallbackEnabled && primaryProvider != "LOCAL") {
                logger.warn("Primary storage delete failed, attempting local storage: ${e.message}")
                val fallbackRepo = contentRepositoryFactory.getRepository(ProviderType.LOCAL)
                fallbackRepo.deleteFile(documentPath)
            } else {
                throw e
            }
        }
    }

    fun fetchFileWithFallback(documentPath: String): InputStream {
        return try {
            val primaryRepo = contentRepositoryFactory.getRepository(ProviderType.valueOf(primaryProvider))
            primaryRepo.fetchFile(documentPath)
        } catch (e: Exception) {
            if (fallbackEnabled && primaryProvider != "LOCAL") {
                logger.warn("Primary storage fetch failed, attempting local storage: ${e.message}")
                val fallbackRepo = contentRepositoryFactory.getRepository(ProviderType.LOCAL)
                fallbackRepo.fetchFile(documentPath)
            } else {
                throw e
            }
        }
    }
}
