package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.entity.RequestedAmountRange
import java.time.LocalDateTime
import java.util.UUID

/**
 * Lightweight response DTO for lead listing/summary views.
 * Contains only essential information, optimized for performance.
 */
data class LeadSummaryResponse(
    val id: UUID,
    val requestedAmountRange: RequestedAmountRange?,
    val purpose: String?,
    val productCode: String?,
    val currentStage: String?,
    val sourcingChannel: String?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
    val primaryPerson: PrimaryPersonSummary? // APPLICANT person info only
)

/**
 * Summary information for the APPLICANT person only.
 */
data class PrimaryPersonSummary(
    val personId: UUID?,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val primaryMobileNumber: String?, // Only primary number
    val leadPersonType: String?
)
