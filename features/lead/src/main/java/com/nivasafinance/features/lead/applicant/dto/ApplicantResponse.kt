package com.nivasafinance.features.lead.applicant.dto

import java.util.UUID

data class ApplicantResponse(
    val id: UUID?,
    val personId: UUID,
    val leadId: UUID,
    val status: String
)
