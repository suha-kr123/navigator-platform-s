package com.nivasafinance.features.document.storage.impl

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.nivasafinance.features.document.exception.S3DeleteException
import com.nivasafinance.features.document.exception.S3DownloadException
import com.nivasafinance.features.document.exception.S3ExpirationException
import com.nivasafinance.features.document.exception.S3UploadException
import com.nivasafinance.features.document.exception.S3UrlGenerationException
import com.nivasafinance.features.document.storage.ContentRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.Date

@Component
@ConditionalOnProperty(
    name = ["document.storage.provider"],
    havingValue = "AWS_S3",
    matchIfMissing = false
)
class S3ContentRepository(
    private val amazonS3: AmazonS3,
    private val messageSource: MessageSource
) : ContentRepository {

    private val logger = LoggerFactory.getLogger(S3ContentRepository::class.java)

    companion object {
        private const val MILLISECONDS_PER_SECOND = 1000L
        private const val MIN_EXPIRATION_SECONDS = 1L
        private const val MAX_EXPIRATION_SECONDS = 604800L // 7 days
        private const val DEFAULT_EXPIRATION_SECONDS = 300L // 5 minutes
    }

    @Value("\${aws.s3.bucket-name}")
    @Suppress("VarCouldBeVal") // lateinit requires var, not val
    private lateinit var bucketName: String

    @Value("\${document.storage.signed-url.expiration-seconds:300}")
    private val defaultExpirationSeconds: Long = DEFAULT_EXPIRATION_SECONDS

    override fun saveFile(inputStream: InputStream, documentPath: String): String {
        try {
            logger.debug("Uploading file to S3: $documentPath")

            val metadata = ObjectMetadata().apply {
                // Set content length to prevent AWS SDK from buffering in memory
                contentLength = inputStream.available().toLong()
                // Set content type based on file extension
                contentType = getContentType(documentPath)
            }

            val putObjectRequest = PutObjectRequest(bucketName, documentPath, inputStream, metadata)
            amazonS3.putObject(putObjectRequest)

            logger.debug("Successfully uploaded file to S3: $documentPath")
            // Return just the document path as the storage key
            return documentPath
        } catch (e: AmazonServiceException) {
            logger.error("Failed to upload file to S3: $documentPath", e)
            val exception = S3UploadException(
                e.message ?: "Unknown error",
                messageSource
            )
            exception.initCause(e)
            throw exception
        } catch (e: AmazonClientException) {
            logger.error("Client error uploading file to S3: $documentPath", e)
            val exception = S3UploadException(
                e.message ?: "Unknown error",
                messageSource
            )
            exception.initCause(e)
            throw exception
        }
    }

    override fun deleteFile(storageKey: String) {
        try {
            logger.debug("Deleting file from S3: $storageKey")
            amazonS3.deleteObject(bucketName, storageKey)
            logger.debug("Successfully deleted file from S3: $storageKey")
        } catch (e: AmazonServiceException) {
            logger.error("Failed to delete file from S3: $storageKey", e)
            val exception = S3DeleteException(
                e.message ?: "Unknown error",
                messageSource
            )
            exception.initCause(e)
            throw exception
        } catch (e: AmazonClientException) {
            logger.error("Client error deleting file from S3: $storageKey", e)
            val exception = S3DeleteException(
                e.message ?: "Unknown error",
                messageSource
            )
            exception.initCause(e)
            throw exception
        }
    }

    override fun fetchFile(storageKey: String): InputStream {
        try {
            logger.debug("Fetching file from S3: $storageKey")
            val s3Object: S3Object = amazonS3.getObject(bucketName, storageKey)
            logger.debug("Successfully fetched file from S3: $storageKey")
            return s3Object.objectContent
        } catch (e: AmazonServiceException) {
            logger.error("Failed to fetch file from S3: $storageKey", e)
            val exception = S3DownloadException(
                e.message ?: "Unknown error",
                messageSource
            )
            exception.initCause(e)
            throw exception
        } catch (e: AmazonClientException) {
            logger.error("Client error fetching file from S3: $storageKey", e)
            val exception = S3DownloadException(
                e.message ?: "Unknown error",
                messageSource
            )
            exception.initCause(e)
            throw exception
        }
    }

    override fun getSignedDownloadUrl(storageKey: String, expiresIn: Long): String? {
        try {
            // Use default expiration if not specified or invalid
            val actualExpiration = if (expiresIn <= 0) defaultExpirationSeconds else expiresIn

            // Validate expiration time
            if (actualExpiration < MIN_EXPIRATION_SECONDS || actualExpiration > MAX_EXPIRATION_SECONDS) {
                throw S3ExpirationException(messageSource)
            }

            logger.debug("Generating signed URL for S3 object: $storageKey, expires in: ${actualExpiration}s")

            val expiration = Date(System.currentTimeMillis() + actualExpiration * MILLISECONDS_PER_SECOND)
            val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucketName, storageKey)
                .withMethod(com.amazonaws.HttpMethod.GET)
                .withExpiration(expiration)

            val signedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString()
            logger.debug("Successfully generated signed URL for S3 object: $storageKey")
            return signedUrl
        } catch (e: S3ExpirationException) {
            // Re-throw validation exceptions
            throw e
        } catch (e: AmazonServiceException) {
            logger.error("Failed to generate signed URL for S3 object: $storageKey", e)
            val exception = S3UrlGenerationException(
                e.message ?: "Unknown error",
                messageSource
            )
            exception.initCause(e)
            throw exception
        } catch (e: AmazonClientException) {
            logger.error("Client error generating signed URL for S3 object: $storageKey", e)
            val exception = S3UrlGenerationException(
                e.message ?: "Unknown error",
                messageSource
            )
            exception.initCause(e)
            throw exception
        }
    }

    private fun getContentType(filePath: String): String {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "txt" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "xls" -> "application/vnd.ms-excel"
            else -> "application/octet-stream"
        }
    }
}
