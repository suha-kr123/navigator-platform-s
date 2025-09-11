package com.nivasafinance.features.document.dto

import annotations.NoArg
import java.util.UUID

@NoArg
data class UploadRequest(
    val entityId: UUID,
    val entityType: String,
    val documentType: String,
    val isRequired: Boolean = false,
    val fileName: String,
    val fileType: String?,
    val fileSize: Long?,
    val category: String?,
    val docType: String?,
    val tags: List<String>
)
