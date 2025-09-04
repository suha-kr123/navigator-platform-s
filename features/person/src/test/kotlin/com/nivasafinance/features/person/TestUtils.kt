package com.nivasafinance.features.person

import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.dto.PersonAddressMappingResponse
import com.nivasafinance.features.person.dto.PersonIdentifierResponse
import com.nivasafinance.features.person.dto.EmploymentDetailsResponse
import com.nivasafinance.features.person.dto.EmploymentDetailsCreateRequest
import com.nivasafinance.features.person.dto.EmploymentDetailsUpdateRequest
import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.entity.EmploymentDetails
import com.nivasafinance.features.person.enum.EmployerType
import com.nivasafinance.features.person.enum.EmploymentType
import data.enums.Gender
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

object TestUtils {

    fun createTestPersonResponse(
        id: UUID? = UUID.randomUUID(),
        firstName: String = "John",
        middleName: String? = null,
        lastName: String = "Doe",
        mobileNumbers: List<MobileNumberDetails> = listOf(MobileNumberDetails("1234567890", true)),
        email: String = "john.doe@example.com",
        dateOfBirth: LocalDate = LocalDate.of(1990, 1, 1),
        gender: Gender = Gender.MALE
    ): PersonResponse {
        return PersonResponse(
            id = id,
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            mobileNumbers = mobileNumbers,
            email = email,
            dateOfBirth = dateOfBirth,
            gender = gender
        )
    }

    fun createTestPersonAddressMappingResponse(
        id: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        addressId: UUID = UUID.randomUUID(),
        addressType: String = "HOME"
    ): PersonAddressMappingResponse {
        val address = com.nivasafinance.features.address.dto.AddressResponse(
            id = addressId,
            pincode = "123456"
        )
        return PersonAddressMappingResponse(
            id = id,
            personId = personId,
            address = address,
            addressType = addressType
        )
    }

    fun createTestPersonIdentifierResponse(
        id: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        identifierType: com.nivasafinance.features.person.enum.IdentifierType = com.nivasafinance.features.person.enum.IdentifierType.PAN,
        identifierValue: String = "ABCDE1234F"
    ): PersonIdentifierResponse {
        return PersonIdentifierResponse(
            id = id,
            personId = personId,
            identifier = identifierValue,
            type = identifierType
        )
    }

    fun createTestMobileNumberDetails(
        number: String = "1234567890",
        isPrimary: Boolean = true
    ): MobileNumberDetails {
        return MobileNumberDetails(
            number = number,
            isPrimary = isPrimary
        )
    }

    fun createTestPersonWithMultipleMobileNumbers(
        id: UUID? = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe"
    ): PersonResponse {
        val mobileNumbers = listOf(
            MobileNumberDetails("1234567890", true),
            MobileNumberDetails("0987654321", false)
        )
        return createTestPersonResponse(
            id = id,
            firstName = firstName,
            lastName = lastName,
            mobileNumbers = mobileNumbers
        )
    }

    fun createTestPersonWithAddresses(
        id: UUID? = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe"
    ): PersonResponse {
        return createTestPersonResponse(
            id = id,
            firstName = firstName,
            lastName = lastName
        )
    }

    fun createTestPersonWithIdentifiers(
        id: UUID? = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe"
    ): PersonResponse {
        return createTestPersonResponse(
            id = id,
            firstName = firstName,
            lastName = lastName
        )
    }

    fun createTestEmploymentDetailsResponse(
        employmentId: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        employerName: String = "Test Company",
        employerType: EmployerType = EmployerType.PRIVATE,
        jobTitle: String = "Software Engineer",
        department: String = "Engineering",
        employmentType: EmploymentType = EmploymentType.FULL_TIME,
        location: String = "Bangalore",
        salary: BigDecimal = BigDecimal("100000.00"),
        documents: Map<String, Any> = mapOf("offer_letter" to "doc123"),
        extData: Map<String, Any> = mapOf("bonus" to "50000")
    ): EmploymentDetailsResponse {
        return EmploymentDetailsResponse(
            employmentId = employmentId,
            personId = personId,
            employerName = employerName,
            employerType = employerType,
            jobTitle = jobTitle,
            department = department,
            employmentType = employmentType,
            location = location,
            salary = salary,
            documents = documents,
            extData = extData
        )
    }

    fun createTestEmploymentDetailsCreateRequest(
        employerName: String = "Test Company",
        employerType: EmployerType = EmployerType.PRIVATE,
        jobTitle: String = "Software Engineer",
        department: String = "Engineering",
        employmentType: EmploymentType = EmploymentType.FULL_TIME,
        location: String = "Bangalore",
        salary: BigDecimal = BigDecimal("100000.00"),
        documents: Map<String, Any> = mapOf("offer_letter" to "doc123"),
        extData: Map<String, Any> = mapOf("bonus" to "50000")
    ): EmploymentDetailsCreateRequest {
        return EmploymentDetailsCreateRequest(
            employerName = employerName,
            employerType = employerType,
            jobTitle = jobTitle,
            department = department,
            employmentType = employmentType,
            location = location,
            salary = salary,
            documents = documents,
            extData = extData
        )
    }

    fun createTestEmploymentDetailsUpdateRequest(
        employerName: String? = "Updated Company",
        employerType: EmployerType? = EmployerType.GOVERNMENT,
        jobTitle: String? = "Senior Software Engineer",
        department: String? = "IT",
        employmentType: EmploymentType? = EmploymentType.CONTRACT,
        location: String? = "Mumbai",
        salary: BigDecimal? = BigDecimal("150000.00"),
        documents: Map<String, Any>? = mapOf("contract" to "doc456"),
        extData: Map<String, Any>? = mapOf("benefits" to "health_insurance")
    ): EmploymentDetailsUpdateRequest {
        return EmploymentDetailsUpdateRequest(
            employerName = employerName,
            employerType = employerType,
            jobTitle = jobTitle,
            department = department,
            employmentType = employmentType,
            location = location,
            salary = salary,
            documents = documents,
            extData = extData
        )
    }

    fun createTestEmploymentDetailsEntity(
        employmentId: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        employerName: String = "Test Company",
        employerType: EmployerType = EmployerType.PRIVATE,
        jobTitle: String = "Software Engineer",
        department: String = "Engineering",
        employmentType: EmploymentType = EmploymentType.FULL_TIME,
        location: String = "Bangalore",
        salary: BigDecimal = BigDecimal("100000.00"),
        documents: Map<String, Any> = mapOf("offer_letter" to "doc123"),
        extData: Map<String, Any> = mapOf("bonus" to "50000")
    ): EmploymentDetails {
        return EmploymentDetails(
            employmentId = employmentId,
            personId = personId,
            employerName = employerName,
            employerType = employerType,
            jobTitle = jobTitle,
            department = department,
            employmentType = employmentType,
            location = location,
            salary = salary,
            documents = documents,
            extData = extData
        )
    }
}
