package com.nivasafinance.features.document.storage

import com.nivasafinance.features.document.enum.ProviderType
import com.nivasafinance.features.document.storage.impl.FileSystemRepository
import org.springframework.stereotype.Component

@Component
class ContentRepositoryFactory(
    private val fileSystemRepository: FileSystemRepository
) {

    fun getRepository(providerType: ProviderType): ContentRepository {
        return when (providerType) {
            ProviderType.AWS_S3 -> throw UnsupportedOperationException("S3 storage not available in local development")
            ProviderType.LOCAL -> fileSystemRepository
        }
    }
}
