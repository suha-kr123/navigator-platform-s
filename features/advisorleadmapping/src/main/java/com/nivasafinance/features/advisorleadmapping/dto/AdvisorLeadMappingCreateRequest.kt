package com.nivasafinance.features.advisorleadmapping.dto

import java.util.UUID

data class AdvisorLeadMappingCreateRequest(
    val advisorId: UUID?,
    val leadId: UUID?,
    val verificationStatus: String?,
    val verificationNotes: String?,
    val extData: Map<String, Any>?
)
