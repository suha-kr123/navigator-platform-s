package com.nivasafinance.features.document.config

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    name = ["document.storage.provider"],
    havingValue = "AWS_S3",
    matchIfMissing = false
)
class S3Configuration {

    private val logger = LoggerFactory.getLogger(S3Configuration::class.java)

    companion object {
        // Removed unused constant
    }

    @Value("\${aws.access-key-id:}")
    private val accessKeyId: String = ""

    @Value("\${aws.secret-access-key:}")
    private val secretAccessKey: String = ""

    @Value("\${aws.s3.region}")
    private val region: String = ""

    @Value("\${aws.s3.use-iam-roles:false}")
    private val useIamRoles: Boolean = false

    @Bean
    fun amazonS3(): AmazonS3 {
        logger.info("Initializing AWS S3 Configuration")

        val clientConfiguration = ClientConfiguration().apply {
            // Remove signer override to use default AWS signature version
            // signerOverride = "AWSS3V4SignerType"
        }

        val builder = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.fromName(region))
            .withForceGlobalBucketAccessEnabled(true)
            .withClientConfiguration(clientConfiguration)

        // Use IAM roles if enabled and no access keys provided, otherwise use access keys
        if (useIamRoles && (accessKeyId.isBlank() || secretAccessKey.isBlank())) {
            logger.debug("Using IAM Role-based authentication for region: $region")
            builder.withCredentials(
                WebIdentityTokenCredentialsProvider.builder()
                    .roleSessionName("navigator-platform-session")
                    .build()
            )
        } else {
            logger.debug("Using Access Key-based authentication for region: $region")
            val credentials = BasicAWSCredentials(accessKeyId, secretAccessKey)
            val credentialsProvider = AWSStaticCredentialsProvider(credentials)
            builder.withCredentials(credentialsProvider)
        }

        val s3Client = builder.build()
        logger.info("S3 Client initialized successfully for region: ${s3Client.regionName}")
        return s3Client
    }
}
