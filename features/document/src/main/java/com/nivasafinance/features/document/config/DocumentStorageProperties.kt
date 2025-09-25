package com.nivasafinance.features.document.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "document.storage")
data class DocumentStorageProperties(
    val provider: String = "LOCAL",

    val signedUrl: SignedUrlProperties = SignedUrlProperties(),

    val fallback: FallbackProperties = FallbackProperties()
)

data class SignedUrlProperties(
    val expirationSeconds: Long = 300L
)

data class FallbackProperties(
    val enabled: Boolean = true
)

@ConfigurationProperties(prefix = "aws.s3")
data class S3Properties(
    val region: String = "",

    val bucketName: String = "",

    val useIamRoles: Boolean = false,

    val accessKeyId: String = "",

    val secretAccessKey: String = ""
)

@ConfigurationProperties(prefix = "local.storage")
data class LocalStorageProperties(
    val basePath: String = "/tmp/documents"
)
