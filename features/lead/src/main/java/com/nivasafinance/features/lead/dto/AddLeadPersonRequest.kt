package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails

data class AddLeadPersonRequest(
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val extData: Map<String, Any>?,
    val applicantType: String?,
    val relationshipToPrimary: String?
)
