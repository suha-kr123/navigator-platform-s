package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.document.dto.DocumentResponse
import java.time.LocalDateTime
import java.util.UUID

data class LeadTaskDocumentsResponse(
    val leadId: UUID,
    val taskId: UUID,
    val documentId: UUID,
    val documentName: String,
    val documentUrl: String,
    val documentType: String,
    val documentVerificationStatus: String,
    val documentVerificationNotes: String?,
    val documentFileSize: Long?,
    val documentStorageKey: String,
    val documentFileUrl: String?,
    val documentCategory: String?,
    val documentDocType: String?,
    val documentTags: List<String>?,
    val documentExtData: Map<String, Any>?,
    val documentCreatedAt: LocalDateTime,
    val documentCreatedBy: String,
    val documentUpdatedAt: LocalDateTime,
    val documentUpdatedBy: String
)
