package com.nivasafinance.features.advisor.dto

data class PersonUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val mobileNumbers: List<MobileNumber>? = null,
    val emailAddresses: List<EmailAddress>? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val status: String? = null
)
