package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails
import java.util.UUID

data class UpdateLeadPersonRequest(
    val personId: UUID,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val email: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val extData: Map<String, Any>?,
    val applicantType: String?,
    val relationshipToPrimary: String?,
    val tags: List<String>?,
    val verificationStatus: String?,
    val verificationNotes: String?
)