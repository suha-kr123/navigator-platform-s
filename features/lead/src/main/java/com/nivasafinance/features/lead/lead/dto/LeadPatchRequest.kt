package com.nivasafinance.features.lead.lead.dto

import com.nivasafinance.features.lead.lead.enum.LeadStage
import com.nivasafinance.features.lead.lead.enum.LeadStatus
import java.math.BigDecimal

data class LeadPatchRequest(
    val requestedAmount: BigDecimal? = null,
    val purpose: String? = null,
    val productCode: String? = null,
    val sourcingChannel: String? = null,
    val preimerlyInformation: LeadPreimerlyInformation? = null,
    val stage: LeadStage? = null,
    val status: LeadStatus? = null,
    val leadContacts: LeadContacts? = null
)
