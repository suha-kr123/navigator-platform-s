package com.nivasafinance.features.wrapper.dto

import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import java.util.UUID

data class ApplicantWrapperResponse(
    val id: UUID,
    val status: LeadStatus,
    val stage: LeadStage
)
