package com.nivasafinance.features.advisorleadmapping.client

import java.math.BigDecimal
import java.util.UUID

interface LeadClient {
    fun getLead(id: UUID): LeadInfo
    fun validateLead(id: UUID): Boolean
}

data class LeadInfo(
    val id: UUID,
    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val status: String?,
    val stage: String?
)
