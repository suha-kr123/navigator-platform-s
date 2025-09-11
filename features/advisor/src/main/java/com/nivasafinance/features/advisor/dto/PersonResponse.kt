package com.nivasafinance.features.advisor.dto

import annotations.NoArg
import java.util.UUID

@NoArg
data class PersonResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val mobileNumbers: List<MobileNumber>,
    val emailAddresses: List<EmailAddress>,
    val dateOfBirth: String?,
    val gender: String?,
    val status: String
)

data class MobileNumber(
    val number: String,
    val isPrimary: Boolean,
    val isVerified: Boolean
)

data class EmailAddress(
    val email: String,
    val isPrimary: Boolean,
    val isVerified: Boolean
)
