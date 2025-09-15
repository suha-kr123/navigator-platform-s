package com.nivasafinance.features.document.dto

import java.time.LocalDateTime
import java.util.*

data class DocumentResponse(
    val documentId: UUID,
    val entityId: UUID,
    val entityType: String,
    val documentType: String,
    val isVerified: Boolean,
    val verificationNotes: String?,
    val fileName: String,
    val fileType: String?,
    val fileSize: Long?,
    val storageKey: String,
    val fileUrl: String?,
    val category: String?,
    val docType: String?,
    val tags: List<String>?,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)
