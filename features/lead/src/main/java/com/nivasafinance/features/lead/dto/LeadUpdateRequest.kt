package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal

data class LeadUpdateRequest(
    val requestedAmount: BigDecimal? = null,
    val purpose: String? = null,
    val productCode: String? = null,
    val sourcingChannel: SourcingChannel? = null,
    val preliminaryInformation: LeadPreliminaryInformation? = null,
    val stage: LeadStage? = null,
    val status: LeadStatus? = null,
    val leadContacts: LeadContacts? = null
)
