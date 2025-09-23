package com.nivasafinance.features.advisor.dto

data class AdvisorUpdateRequest(
    val advisorCode: String?,
    val verificationStatus: String?,
    val verificationNotes: String?,
    val extData: Map<String, Any>?
)
