package com.nivasafinance.features.document.health

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.io.File

@Component
@ConditionalOnProperty(
    name = ["document.storage.provider"],
    havingValue = "LOCAL",
    matchIfMissing = true
)
@Suppress("TooGenericExceptionCaught", "VarCouldBeVal", "MaximumLineLength")
class LocalStorageHealthIndicator {

    private val logger = LoggerFactory.getLogger(LocalStorageHealthIndicator::class.java)

    @Value("\${local.storage.base-path:/tmp/documents}")
    private lateinit var basePath: String

    fun checkHealth(): Boolean {
        return try {
            val baseDir = File(basePath)
            val exists = baseDir.exists()
            val writable = baseDir.canWrite()
            val readable = baseDir.canRead()

            if (exists && writable && readable) {
                logger.info(
                    "Local storage health check passed: basePath=$basePath, writable=$writable, readable=$readable"
                )
                true
            } else {
                logger.error(
                    "Local storage health check failed: basePath=$basePath, " +
                        "exists=$exists, writable=$writable, readable=$readable"
                )
                false
            }
        } catch (e: Exception) {
            logger.error("Local storage health check failed: ${e.message}", e)
            false
        }
    }
}
