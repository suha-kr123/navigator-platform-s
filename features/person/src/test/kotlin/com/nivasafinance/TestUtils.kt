package com.nivasafinance

import com.nivasafinance.features.person.dto.PersonData
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

    fun createTestPersonData(
        id: UUID = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        gender: Gender = Gender.MALE
    ): PersonData {
        return PersonData(
            id = id,
            firstName = firstName,
            middleName = null,
            lastName = lastName,
            mobileNumbers = null,
            email = email,
            dateOfBirth = null,
            gender = gender,
            dataExt = null
        )
    }
}
