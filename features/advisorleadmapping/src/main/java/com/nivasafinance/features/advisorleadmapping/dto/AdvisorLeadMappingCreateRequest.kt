package com.nivasafinance.features.advisorleadmapping.dto

import com.nivasafinance.features.advisorleadmapping.client.PaymentCreateRequest

data class AdvisorLeadMappingCreateRequest(
    val remarks: String? = null,
    val extData: Map<String, Any>? = null,
    val payment: PaymentCreateRequest? = null
)
