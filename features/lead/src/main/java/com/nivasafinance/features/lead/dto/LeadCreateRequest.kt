package com.nivasafinance.features.lead.dto

import java.math.BigDecimal

data class LeadCreateRequest(

    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val pipelineKey: String?,
    val currentStage: String,
    val sourcingChannel: String?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val extData: Map<String, Any>?
)
