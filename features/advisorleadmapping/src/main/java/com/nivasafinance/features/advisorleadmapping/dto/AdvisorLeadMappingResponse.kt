package com.nivasafinance.features.advisorleadmapping.dto

import com.nivasafinance.features.advisorleadmapping.client.PaymentResponse
import java.util.UUID

data class AdvisorLeadMappingResponse(
    val id: UUID,
    val advisorId: UUID,
    val leadId: UUID,
    val remarks: String? = null,
    val extData: Map<String, Any?>? = null,
    val payment: PaymentResponse? = null
)
