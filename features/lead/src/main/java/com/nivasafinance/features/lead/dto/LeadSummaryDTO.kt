package com.nivasafinance.features.lead.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO class for optimized lead summary data retrieval.
 * Used by Spring Data JPA to map custom query results to typed objects.
 * Fetches only essential fields and APPLICANT person's primary contact info.
 */
data class LeadSummaryDTO(
    val id: UUID,
    val minAmount: BigDecimal?,
    val maxAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val currentStage: String?,
    val sourcingChannel: String?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime?,
    val updatedBy: String?,

    // APPLICANT person information only
    val personId: UUID?,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val mobileNumber: String?, // Primary mobile number for display
    val leadPersonType: String?,
    val relationshipToPrimary: String?
)
