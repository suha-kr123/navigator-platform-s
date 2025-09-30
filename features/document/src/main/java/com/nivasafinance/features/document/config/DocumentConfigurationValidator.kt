package com.nivasafinance.features.document.config

import com.nivasafinance.features.document.health.LocalStorageHealthIndicator
import com.nivasafinance.features.document.health.S3HealthIndicator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Suppress("TooGenericExceptionCaught", "VarCouldBeVal")
class DocumentConfigurationValidator {

    private val logger = LoggerFactory.getLogger(DocumentConfigurationValidator::class.java)

    @Value("\${document.storage.provider:LOCAL}")
    private lateinit var storageProvider: String

    @Value("\${aws.s3.bucket-name:}")
    private val s3BucketName: String = ""

    @Value("\${aws.s3.region:}")
    private val s3Region: String = ""

    @Value("\${local.storage.base-path:/tmp/documents}")
    private lateinit var localBasePath: String

    @Autowired(required = false)
    private val s3HealthIndicator: S3HealthIndicator? = null

    @Autowired(required = false)
    private val localStorageHealthIndicator: LocalStorageHealthIndicator? = null

    @EventListener(ApplicationReadyEvent::class)
    fun validateConfiguration() {
        logger.info("Validating document storage configuration...")

        try {
            when (storageProvider.uppercase()) {
                "AWS_S3" -> validateS3Configuration()
                "LOCAL" -> validateLocalConfiguration()
                else -> error("Invalid storage provider: $storageProvider. Valid options: AWS_S3, LOCAL")
            }

            logger.info("Document storage configuration validation completed successfully")
        } catch (e: IllegalArgumentException) {
            logger.error(
                "Invalid storage provider: $storageProvider. Valid options: AWS_S3, LOCAL"
            )
            error("Invalid document storage configuration: ${e.message}")
        } catch (e: Exception) {
            logger.error("Document storage configuration validation failed", e)
            error("Document storage configuration validation failed: ${e.message}")
        }
    }

    private fun validateS3Configuration() {
        logger.info("Validating AWS S3 configuration...")

        if (s3BucketName.isBlank()) {
            error("AWS S3 bucket name is required when using AWS_S3 provider")
        }

        if (s3Region.isBlank()) {
            error("AWS S3 region is required when using AWS_S3 provider")
        }

        // Test S3 connectivity if health indicator is available
        s3HealthIndicator?.let { indicator ->
            if (!indicator.checkHealth()) {
                logger.warn("S3 health check failed, but continuing with configuration validation")
            }
        }

        logger.info("AWS S3 configuration validated: bucket=$s3BucketName, region=$s3Region")
    }

    private fun validateLocalConfiguration() {
        logger.info("Validating local storage configuration...")

        if (localBasePath.isBlank()) {
            error("Local storage base path cannot be blank")
        }

        // Test local storage accessibility if health indicator is available
        localStorageHealthIndicator?.let { indicator ->
            if (!indicator.checkHealth()) {
                logger.warn("Local storage health check failed, but continuing with configuration validation")
            }
        }

        logger.info("Local storage configuration validated: basePath=$localBasePath")
    }
}
