package com.nivasafinance.features.person.dto

import annotations.NoArg
import com.nivasafinance.features.person.entity.MobileNumberDetails
import data.enums.Gender
import jakarta.validation.Valid
import java.time.LocalDate

@NoArg
data class PersonCreateRequest(
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    @field:Valid
    var mobileNumbers: List<MobileNumberDetails> = emptyList(),
    var email: String? = null,
    var dateOfBirth: LocalDate? = null,
    var gender: Gender? = null
)
