package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.document.dto.DocumentResponse
import java.util.*

data class LeadTaskDocumentsResponse(
    val leadId: UUID,
    val taskId: UUID,
    val documents: List<DocumentResponse>
)
