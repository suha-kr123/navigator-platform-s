package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal

data class LeadCreateRequest(

    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val pipelineKey: String?,
    val currentStage: String,
    val sourcingChannel: SourcingChannel?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val extData: Map<String, Any>?
)
