package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.enum.Gender
import java.util.UUID

data class UpdateLeadPersonRequest(
    val personId: UUID,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val dateOfBirth: String?,
    val gender: Gender?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val extData: Map<String, Any>?,
    val applicantType: String?,
    val relationshipToPrimary: String?
)
