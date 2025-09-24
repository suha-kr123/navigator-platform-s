package com.nivasafinance.features.advisorleadmapping.dto

data class UpdateAdvisorLeadMappingRequest(
    val verificationStatus: String?,
    val verificationNotes: String?,
    val extData: Map<String, Any>?
)
