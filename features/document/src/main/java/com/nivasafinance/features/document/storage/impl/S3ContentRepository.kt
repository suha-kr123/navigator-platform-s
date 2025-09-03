package com.nivasafinance.features.document.storage.impl

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.nivasafinance.features.document.storage.ContentRepository
import org.springframework.beans.factory.annotation.Value
import java.io.InputStream
import java.util.Date

// @Component  // Commented out for local development
class S3ContentRepository(
    private val amazonS3: AmazonS3
) : ContentRepository {

    companion object {
        private const val MILLISECONDS_PER_SECOND = 1000L
    }

    @Value("\${aws.s3.bucket-name}")
    @Suppress("VarCouldBeVal") // lateinit requires var, not val
    private lateinit var bucketName: String

    override fun saveFile(inputStream: InputStream, documentPath: String): String {
        val metadata = ObjectMetadata()
        val putObjectRequest = PutObjectRequest(bucketName, documentPath, inputStream, metadata)

        amazonS3.putObject(putObjectRequest)

        return "s3://$bucketName/$documentPath"
    }

    override fun deleteFile(documentPath: String) {
        amazonS3.deleteObject(bucketName, documentPath)
    }

    override fun fetchFile(documentPath: String): InputStream {
        val s3Object: S3Object = amazonS3.getObject(bucketName, documentPath)
        return s3Object.objectContent
    }

    override fun getSignedDownloadUrl(documentPath: String, expiresIn: Long): String? {
        val expiration = Date(System.currentTimeMillis() + expiresIn * MILLISECONDS_PER_SECOND)
        val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucketName, documentPath)
            .withMethod(com.amazonaws.HttpMethod.GET)
            .withExpiration(expiration)

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString()
    }
}
