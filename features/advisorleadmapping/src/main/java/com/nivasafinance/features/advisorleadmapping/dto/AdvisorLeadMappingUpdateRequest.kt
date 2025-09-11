package com.nivasafinance.features.advisorleadmapping.dto

import com.nivasafinance.features.advisorleadmapping.client.PaymentUpdateRequest

data class AdvisorLeadMappingUpdateRequest(
    val remarks: String? = null,
    val extData: Map<String, Any>? = null,
    val payment: PaymentUpdateRequest? = null
)
