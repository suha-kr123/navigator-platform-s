package com.nivasafinance.features.applicant.dto

import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import java.util.UUID

data class ApplicantCreateRequest(
    val leadId: UUID,
    val personId: UUID,
    val applicantType: ApplicantType = ApplicantType.PRIMARY,
    val relationshipToPrimary: RelationshipToPrimary = RelationshipToPrimary.SELF,
    val status: ApplicantStatus = ApplicantStatus.NEEDS_TO_BE_REVIEWED
)
