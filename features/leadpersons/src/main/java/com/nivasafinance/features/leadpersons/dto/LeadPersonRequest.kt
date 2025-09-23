package com.nivasafinance.features.leadpersons.dto

import java.util.*

data class LeadPersonRequest(
    val leadId: UUID,
    val personId: UUID,
    val isApplicant: Boolean,
    val applicantType: String,
    val relationshipToPrimary: String,
    val tags: List<String>?,
    val verificationStatus: String,
    val verificationNotes: String?,
    val extData: Map<String, Any>?
)
