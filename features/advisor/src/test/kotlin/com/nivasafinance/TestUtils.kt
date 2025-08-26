package com.nivasafinance

import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.entity.MobileNumberDetails
import data.enums.Gender
import java.time.LocalDate
import java.util.UUID

object TestUtils {

    fun createTestAdvisor(
        id: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        advisorCode: String = "ADV001",
        status: AdvisorStatus = AdvisorStatus.ACTIVE,
        isEmployee: Boolean = false,
        isExperiencedDsa: Boolean = false,
        remarks: String? = null,
        rejectionReason: String? = null,
        advisorFeedback: String? = null,
        welcomeKitSent: Boolean = false,
        attendedAdvisorMeeting: Boolean = false,
        extData: Map<String, Any>? = null
    ): Advisor {
        return Advisor(
            id = id,
            personId = personId,
            advisorCode = advisorCode,
            isEmployee = isEmployee,
            status = status,
            isExperiencedDsa = isExperiencedDsa,
            remarks = remarks,
            rejectionReason = rejectionReason,
            advisorFeedback = advisorFeedback,
            welcomeKitSent = welcomeKitSent,
            attendedAdvisorMeeting = attendedAdvisorMeeting,
            extData = extData
        )
    }

    fun createTestAdvisorResponse(
        id: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        advisorCode: String = "ADV001",
        status: AdvisorStatus = AdvisorStatus.ACTIVE,
        personalDetails: PersonResponse = createTestPersonResponse()
    ): AdvisorResponse {
        return AdvisorResponse(
            id = id,
            personId = personId,
            advisorCode = advisorCode,
            isEmployee = false,
            status = status,
            remarks = "Test advisor",
            rejectionReason = null,
            advisorFeedback = null,
            welcomeKitSent = false,
            attendedAdvisorMeeting = false,
            extData = null,
            personalDetails = personalDetails
        )
    }

    fun createTestPersonResponse(
        id: UUID? = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe",
        mobileNumbers: List<MobileNumberDetails> = listOf(MobileNumberDetails("1234567890", true))
    ): PersonResponse {
        return PersonResponse(
            id = id,
            firstName = firstName,
            middleName = null,
            lastName = lastName,
            mobileNumbers = mobileNumbers,
            email = "john.doe@example.com",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            gender = Gender.MALE
        )
    }
}
