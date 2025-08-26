package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.entity.Details
import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.entity.Person
import data.enums.Gender
import java.time.LocalDate
import java.util.UUID

data class PersonData(
    val id: UUID?,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val email: String?,
    val dateOfBirth: LocalDate?,
    val gender: Gender?,
    val dataExt: Details?
) {
    companion object {
        fun fromEntity(person: Person): PersonData {
            return PersonData(
                id = person.id,
                firstName = person.firstName,
                middleName = person.middleName,
                lastName = person.lastName,
                mobileNumbers = person.mobileNumbers,
                email = person.email,
                dateOfBirth = person.dateOfBirth,
                gender = person.gender,
                dataExt = person.dataExt
            )
        }
    }
}
