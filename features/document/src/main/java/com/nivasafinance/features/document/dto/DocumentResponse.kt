package com.nivasafinance.features.document.dto

import com.nivasafinance.features.document.enum.VerificationStatus
import java.time.LocalDateTime
import java.util.*

data class DocumentResponse(
    val id: UUID,
    val entityId: UUID,
    val entityType: String,
    val documentType: String,
    val verificationStatus: VerificationStatus,
    val verificationNotes: String?,
    val fileName: String,
    val fileType: String?,
    val fileSize: Long?,
    val provider: String,
    val storageKey: String,
    val fileUrl: String?,
    val tags: List<String>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)
