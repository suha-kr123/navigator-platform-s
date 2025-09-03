package com.nivasafinance.features.document.dto

import annotations.NoArg
import com.nivasafinance.features.document.enum.ProviderType
import java.time.LocalDateTime
import java.util.UUID

@NoArg
data class DocumentResponse(
    val documentId: UUID,
    val fileName: String,
    val fileType: String?,
    val fileSize: Long?,
    val provider: ProviderType,
    val storageKey: String,
    val fileUrl: String?,
    val category: String?,
    val docType: String?,
    val tags: List<String>,
    val extData: Map<String, Any>,
    val createdBy: String?,
    val createdAt: LocalDateTime?,
    val updatedBy: String?,
    val updatedAt: LocalDateTime?,
    val version: Long
)
