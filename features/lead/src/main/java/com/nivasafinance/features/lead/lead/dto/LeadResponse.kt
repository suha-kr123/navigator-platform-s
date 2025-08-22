package com.nivasafinance.features.lead.lead.dto

import java.util.UUID

data class LeadResponse(
    val id: UUID,
    val status: String,
    val stage: String
)
