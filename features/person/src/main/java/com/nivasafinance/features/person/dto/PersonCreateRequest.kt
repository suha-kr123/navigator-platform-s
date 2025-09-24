package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails

data class PersonCreateRequest(
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val email: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val extData: Map<String, Any>?
)
