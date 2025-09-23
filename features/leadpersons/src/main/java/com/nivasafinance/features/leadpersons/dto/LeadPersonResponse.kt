package com.nivasafinance.features.leadpersons.dto

import java.time.LocalDateTime
import java.util.*

data class LeadPersonResponse(
    val id: UUID,
    val leadId: UUID,
    val personId: UUID,
    val isApplicant: Boolean,
    val applicantType: String,
    val relationshipToPrimary: String,
    val tags: List<String>?,
    val verificationStatus: String,
    val verificationNotes: String?,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)
