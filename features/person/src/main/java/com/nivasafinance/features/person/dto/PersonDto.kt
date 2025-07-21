package com.nivasafinance.features.person.dto

import annotations.NoArg
import data.enums.Gender
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.util.UUID

@NoArg
data class PersonDto(
    val id: UUID? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    @field:Valid
    val mobileNumber: MobileNumberDetails = MobileNumberDetails(),
    val displayName: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null
)

@NoArg
data class MobileNumberDetails(
    @field:NotBlank(message = "Mobile number is mandatory")
    @field:Pattern(regexp = "^[0-9]{10}$", message = "Mobile number should be 10 digits")
    val primary: String = "",
)
