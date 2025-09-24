package com.nivasafinance.features.advisorleadmapping.dto

data class AdvisorLeadMappingUpdateRequest(
    val verificationStatus: String?,
    val verificationNotes: String?,
    val extData: Map<String, Any>?
)
