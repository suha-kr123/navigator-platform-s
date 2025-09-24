package com.nivasafinance.features.advisor.dto

import com.nivasafinance.features.person.dto.PersonResponse
import java.time.LocalDateTime
import java.util.UUID

data class AdvisorResponse(
    val id: UUID,
    val personId: UUID?,
    val person: PersonResponse?,
    val advisorCode: String?,
    val verificationStatus: String?,
    val verificationNotes: String?,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)
