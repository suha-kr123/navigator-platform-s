package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class LeadResponse(
    val id: UUID,
    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val status: LeadStatus?,
    val sourcingChannel: SourcingChannel?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val leadContacts: LeadContacts?,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
)
