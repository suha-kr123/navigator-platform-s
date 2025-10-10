package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.entity.RequestedAmountRange
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
) {
    
    /**
     * Maps LeadSummaryDTO to LeadSummaryResponse.
     * This keeps the mapping logic close to the DTO and prevents service classes from becoming huge.
     */
    fun toLeadSummaryResponse(): LeadSummaryResponse {
        val primaryPerson = if (personId != null) {
            PrimaryPersonSummary(
                personId = personId,
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                mobileNumber = mobileNumber,
                leadPersonType = leadPersonType,
                relationshipToPrimary = relationshipToPrimary
            )
        } else null

        return LeadSummaryResponse(
            id = id,
            requestedAmountRange = createRequestedAmountRange(minAmount, maxAmount),
            purpose = purpose,
            productCode = productCode,
            currentStage = currentStage,
            sourcingChannel = sourcingChannel,
            createdAt = createdAt,
            createdBy = createdBy,
            updatedAt = updatedAt ?: LocalDateTime.now(),
            updatedBy = updatedBy,
            primaryPerson = primaryPerson
        )
    }
    
    /**
     * Creates RequestedAmountRange from min and max amounts.
     */
    private fun createRequestedAmountRange(min: BigDecimal?, max: BigDecimal?): RequestedAmountRange? {
        return if (min != null && max != null) {
            RequestedAmountRange(min = min, max = max)
        } else null
    }
}
