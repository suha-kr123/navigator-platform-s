package com.nivasafinance.features.applicant.dto

import annotations.NoArg
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary

@NoArg
data class ApplicantUpdateRequest(
    var applicantType: ApplicantType? = null,
    var relationshipToPrimary: RelationshipToPrimary? = null,
    var status: ApplicantStatus? = null
)
