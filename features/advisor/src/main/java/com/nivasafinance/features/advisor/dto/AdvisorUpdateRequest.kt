package com.nivasafinance.features.advisor.dto

import annotations.NoArg
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import jakarta.validation.Valid

@NoArg
data class AdvisorUpdateRequest(
    val advisorCode: String? = null,
    val status: AdvisorStatus? = null,
    val isEmployee: Boolean? = null,
    val remarks: String? = null,
    val rejectionReason: String? = null,
    val advisorFeedback: String? = null,
    val welcomeKitSent: Boolean? = null,
    val attendedAdvisorMeeting: Boolean? = null,
    val extData: Map<String, Any>? = null,
    @field:Valid
    var personalDetails: PersonUpdateRequest = PersonUpdateRequest()
)
