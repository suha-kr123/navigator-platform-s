package com.nivasafinance.features.advisor.dto

import com.nivasafinance.features.person.dto.PersonCreateRequest

data class AdvisorRequest(
    val person: PersonCreateRequest,
    val advisorCode: String?,
    val verificationStatus: String?,
    val verificationNotes: String?,
    val extData: Map<String, Any>?
)
