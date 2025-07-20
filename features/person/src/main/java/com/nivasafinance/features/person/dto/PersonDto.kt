package com.nivasafinance.features.person.dto

import annotations.NoArg
import data.enums.Gender
import java.time.LocalDate
import java.util.UUID

@NoArg
data class PersonDto(
    val id: UUID? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val mobileNumber: MobileNumberDetails = MobileNumberDetails(),
    val displayName: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null
)

@NoArg
data class MobileNumberDetails(
    val primary: String = "",
)