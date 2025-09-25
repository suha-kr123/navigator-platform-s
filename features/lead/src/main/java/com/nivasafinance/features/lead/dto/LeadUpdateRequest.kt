package com.nivasafinance.features.lead.dto

import java.math.BigDecimal
import java.util.*

data class LeadUpdateRequest(
    val requestedAmount: BigDecimal? = null,
    val purpose: String? = null,
    val productCode: String? = null,
    val pipelineKey: String? = null,
    val currentStage: String? = null,
    val sourcingChannel: String? = null,
    val preliminaryInformation: Map<String, Any>? = null,
    val extData: Map<String, Any>? = null
)
