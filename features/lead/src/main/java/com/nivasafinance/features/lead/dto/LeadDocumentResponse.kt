package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.document.dto.DocumentResponse
import java.util.UUID

/**
 * Lead-specific document response that wraps the core DocumentResponse
 * with additional lead context information
 */
data class LeadDocumentResponse(
    val taskId: UUID? = null,
    val document: DocumentResponse,
)
