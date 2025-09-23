package com.nivasafinance.features.advisorleadmapping.dto

import java.time.LocalDateTime
import java.util.UUID

data class AdvisorLeadMappingResponse(
    val id: UUID,
    val advisorId: UUID?,
    val leadId: UUID?,
    val verificationStatus: String?,
    val verificationNotes: String?,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)
