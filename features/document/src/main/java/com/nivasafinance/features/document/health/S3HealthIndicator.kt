package com.nivasafinance.features.document.health

import com.amazonaws.services.s3.AmazonS3
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    name = ["document.storage.provider"],
    havingValue = "AWS_S3",
    matchIfMissing = false
)
@Suppress("TooGenericExceptionCaught", "VarCouldBeVal")
class S3HealthIndicator(
    private val amazonS3: AmazonS3
) {

    private val logger = LoggerFactory.getLogger(S3HealthIndicator::class.java)

    @Value("\${aws.s3.bucket-name}")
    private lateinit var bucketName: String

    fun checkHealth(): Boolean {
        return try {
            // Use a simple operation to test connectivity instead of deprecated method
            amazonS3.getBucketLocation(bucketName)
            logger.info("S3 health check passed: bucket=$bucketName, region=${amazonS3.regionName}")
            true
        } catch (e: Exception) {
            logger.error("S3 health check failed: ${e.message}", e)
            false
        }
    }
}
