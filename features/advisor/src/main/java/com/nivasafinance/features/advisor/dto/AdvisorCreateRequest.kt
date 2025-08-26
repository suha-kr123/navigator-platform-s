package com.nivasafinance.features.advisor.dto

import java.util.UUID

data class AdvisorCreateRequest(
    val advisorCode: String? = null,
    val personId: UUID? = null,
    val isEmployee: Boolean = false,
    val remarks: String? = null,
    val rejectionReason: String? = null,
    val advisorFeedback: String? = null,
    val welcomeKitSent: Boolean = false,
    val attendedAdvisorMeeting: Boolean = false,
    val extData: Map<String, Any>? = null
)
