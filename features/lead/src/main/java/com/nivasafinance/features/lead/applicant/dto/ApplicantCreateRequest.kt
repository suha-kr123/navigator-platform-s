package com.nivasafinance.features.lead.applicant.dto

import com.nivasafinance.features.lead.applicant.enum.ApplicantStatus
import com.nivasafinance.features.lead.applicant.enum.ApplicantType
import com.nivasafinance.features.lead.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.person.dto.PersonDto

data class ApplicantCreateRequest(
    var personalDetails: PersonDto = PersonDto(),
    val applicantType: ApplicantType = ApplicantType.PRIMARY,
    val relationshipToPrimary: RelationshipToPrimary = RelationshipToPrimary.SELF,
    val status: ApplicantStatus = ApplicantStatus.NEEDS_TO_BE_REVIEWED,
    val description: String? = null
)
