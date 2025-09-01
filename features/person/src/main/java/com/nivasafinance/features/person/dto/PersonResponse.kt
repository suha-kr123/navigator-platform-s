package com.nivasafinance.features.person.dto

import annotations.NoArg
import com.nivasafinance.features.person.entity.MobileNumberDetails
import data.enums.Gender
import java.time.LocalDate
import java.util.UUID

@NoArg
data class PersonResponse(
    val id: UUID?,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val mobileNumbers: List<MobileNumberDetails>? = null,
    val email: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null
) {
    companion object {
        fun fromPersonData(personData: PersonData): PersonResponse {
            return PersonResponse(
                id = personData.id,
                firstName = personData.firstName,
                middleName = personData.middleName,
                lastName = personData.lastName,
                mobileNumbers = personData.mobileNumbers,
                email = personData.email,
                dateOfBirth = personData.dateOfBirth,
                gender = personData.gender
            )
        }
    }
}
