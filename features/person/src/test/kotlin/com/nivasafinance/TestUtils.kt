package com.nivasafinance

import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.Person
import data.enums.Gender
import java.util.UUID

object TestUtils {

    fun createTestPerson(
        id: UUID = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        gender: Gender = Gender.MALE
    ): Person {
        return Person(
            id = id,
            firstName = firstName,
            lastName = lastName,
            email = email,
            gender = gender
        )
    }

    fun createTestPersonDto(
        id: UUID? = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe",
        mobileNumber: com.nivasafinance.features.person.dto.MobileNumberDetails =
            com.nivasafinance.features.person.dto.MobileNumberDetails("1234567890"),
        gender: Gender = Gender.MALE
    ): PersonDto {
        return PersonDto(
            id = id,
            firstName = firstName,
            lastName = lastName,
            mobileNumber = mobileNumber,
            gender = gender
        )
    }
}
