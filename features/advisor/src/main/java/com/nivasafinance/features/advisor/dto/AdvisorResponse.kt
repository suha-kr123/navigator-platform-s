package com.nivasafinance.features.advisor.dto

import annotations.NoArg
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.person.dto.PersonResponse
import java.util.UUID

@NoArg
data class AdvisorResponse(
    val id: UUID,
    val personId: UUID,
    val advisorCode: String,
    val isEmployee: Boolean = false,
    val status: AdvisorStatus? = null,
    val remarks: String? = null,
    val rejectionReason: String? = null,
    val advisorFeedback: String? = null,
    val welcomeKitSent: Boolean = false,
    val attendedAdvisorMeeting: Boolean = false,
    val extData: Map<String, Any>? = null,
    val personalDetails: PersonResponse
)
