package com.nivasafinance.features.wrapper.dto

import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import java.util.UUID

data class AdvisorWrapperResponse(
    val id: UUID?,
    val name: String?,
    val status: AdvisorStatus,
    val leads: List<LeadInfo>? = null
) {
    data class LeadInfo(
        val leadId: UUID?,
        val status: LeadStatus,
        val stage: LeadStage
    )
}
