package com.nivasafinance

import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.Details
import java.util.UUID

object TestUtils {

    fun createTestAdvisor(
        id: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        advisorCode: String = "ADV001",
        status: AdvisorStatus = AdvisorStatus.ACTIVE,
        dataExt: Details? = null
    ): Advisor {
        return Advisor(
            id = id,
            personId = personId,
            advisorCode = advisorCode,
            status = status,
            dataExt = dataExt
        )
    }

    fun createTestAdvisorDto(
        id: UUID? = UUID.randomUUID(),
        advisorCode: String = "ADV001",
        status: AdvisorStatus = AdvisorStatus.ACTIVE,
        personalDetails: PersonDto = createTestPersonDto()
    ): AdvisorDto {
        return AdvisorDto(
            id = id,
            advisorCode = advisorCode,
            status = status,
            personalDetails = personalDetails
        )
    }

    fun createTestPersonDto(
        id: UUID? = UUID.randomUUID(),
        firstName: String = "John",
        lastName: String = "Doe",
        mobileNumber: com.nivasafinance.features.person.dto.MobileNumberDetails =
            com.nivasafinance.features.person.dto.MobileNumberDetails("1234567890")
    ): PersonDto {
        return PersonDto(
            id = id,
            firstName = firstName,
            lastName = lastName,
            mobileNumber = mobileNumber
        )
    }
}
