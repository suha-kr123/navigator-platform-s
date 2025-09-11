package com.nivasafinance.features.applicant.dto

import annotations.NoArg
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import java.util.UUID

@NoArg
data class ApplicantData(
    val id: UUID?,
    val personId: UUID,
    val leadId: UUID,
    val applicantType: ApplicantType,
    val relationshipToPrimary: RelationshipToPrimary,
    val status: ApplicantStatus
)
