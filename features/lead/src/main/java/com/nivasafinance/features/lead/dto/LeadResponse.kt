package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal
import java.util.UUID

data class LeadResponse(
    val id: UUID,
    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val status: LeadStatus?,
    val stage: LeadStage?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val leadContacts: LeadContacts?,
    val sourcingChannel: SourcingChannel?,
    val extData: Map<String, Any>?
)
