package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.entity.RequestedAmountRange
import java.util.*

data class LeadUpdateRequest(
    val requestedAmountRange: RequestedAmountRange? = null,
    val purpose: String? = null,
    val productCode: String? = null,
    val pipelineKey: String? = null,
    val sourcingChannel: String? = null,
    val preliminaryInformation: Map<String, Any>? = null,
    val extData: Map<String, Any>? = null
)
