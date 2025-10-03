package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.enum.Gender

data class PersonCreateRequest(
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val dateOfBirth: String?,
    val gender: Gender?,
    val extData: Map<String, Any>?
)
