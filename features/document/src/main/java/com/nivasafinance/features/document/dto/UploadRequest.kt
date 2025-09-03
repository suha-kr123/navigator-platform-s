package com.nivasafinance.features.document.dto

import annotations.NoArg
import com.nivasafinance.features.document.enum.ProviderType

@NoArg
data class UploadRequest(
    val fileName: String,
    val fileType: String?,
    val fileSize: Long?,
    val provider: ProviderType,
    val category: String?,
    val docType: String?,
    val tags: List<String>
)
