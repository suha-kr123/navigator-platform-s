package com.nivasafinance.features.document.storage

import com.nivasafinance.features.document.exception.ConfigurationException
import com.nivasafinance.features.document.storage.impl.FileSystemRepository
import com.nivasafinance.features.document.storage.impl.S3ContentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component

@Component
class ContentRepositoryFactory(
    private val fileSystemRepository: FileSystemRepository,
    @Autowired(required = false)
    private val s3ContentRepository: S3ContentRepository?,
    private val messageSource: MessageSource
) {

    fun getRepository(providerType: String): ContentRepository {
        return when (providerType.uppercase()) {
            "AWS_S3" -> {
                s3ContentRepository ?: throw ConfigurationException(
                    "AWS_S3 provider is not configured. " +
                        "Please set document.storage.provider=AWS_S3 and configure AWS credentials.",
                    messageSource
                )
            }
            "LOCAL" -> fileSystemRepository
            else -> throw ConfigurationException(
                "Invalid provider type: $providerType. Valid options: AWS_S3, LOCAL",
                messageSource
            )
        }
    }
}
