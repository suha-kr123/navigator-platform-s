package com.nivasafinance.features.lead.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class LeadResponse(
    val id: UUID,
    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val currentStage: String?,
    val sourcingChannel: String?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val extData: Map<String, Any>?,
    val taskData: Map<UUID, Map<String, List<UUID>>>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
)
