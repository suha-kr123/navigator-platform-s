package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.TestUtils.createTestAdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorData
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.advisor.service.AdvisorWriteService
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.service.PersonService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorServiceImpl Tests")
class AdvisorServiceImplTest {

    private val advisorReadService = mockk<AdvisorReadService>()
    private val advisorWriteService = mockk<AdvisorWriteService>()
    private val personService = mockk<PersonService>()
    private lateinit var advisorService: AdvisorServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val advisorData = AdvisorData(
        id = advisorId,
        personId = personId,
        advisorCode = "ADV001",
        isEmployee = false,
        status = AdvisorStatus.ACTIVE,
        isExperiencedDsa = false,
        remarks = "Test advisor",
        rejectionReason = null,
        advisorFeedback = null,
        welcomeKitSent = false,
        attendedAdvisorMeeting = false,
        extData = null
    )
    private val personResponse = PersonResponse(
        id = personId,
        firstName = "John",
        lastName = "Doe"
    )
    private val expectedResponse = createTestAdvisorResponse(
        id = advisorId,
        personId = personId,
        personalDetails = personResponse
    )

    @BeforeEach
    fun setup() {
        advisorService = AdvisorServiceImpl(advisorReadService, advisorWriteService, personService)
    }

    @Nested
    @DisplayName("getAdvisor Tests")
    inner class GetAdvisorTests {

        @Test
        @DisplayName("should return advisor when found")
        fun `getAdvisor should return advisor when found`() {
            // Given
            every { advisorReadService.getAdvisorData(advisorId) } returns advisorData
            every { personService.getPerson(personId) } returns personResponse

            // When
            val result = advisorService.getAdvisor(advisorId)

            // Then
            assertNotNull(result)
            assertEquals(expectedResponse.id, result.id)
            assertEquals(expectedResponse.advisorCode, result.advisorCode)
            assertEquals(expectedResponse.status, result.status)
            assertEquals(personResponse.id, result.personalDetails.id)

            verify(exactly = 1) { advisorReadService.getAdvisorData(advisorId) }
            verify(exactly = 1) { personService.getPerson(personId) }
        }
    }

    @Nested
    @DisplayName("getAdvisorByMobileNo Tests")
    inner class GetAdvisorByMobileNoTests {

        @Test
        @DisplayName("should return advisor when found by mobile number")
        fun `getAdvisorByMobileNo should return advisor when found`() {
            // Given
            val mobileNo = "1234567890"
            every { advisorReadService.getAdvisorDataByMobileNo(mobileNo) } returns advisorData
            every { personService.getPerson(personId) } returns personResponse

            // When
            val result = advisorService.getAdvisorByMobileNo(mobileNo)

            // Then
            assertNotNull(result)
            assertEquals(expectedResponse.id, result.id)
            assertEquals(expectedResponse.advisorCode, result.advisorCode)
            assertEquals(expectedResponse.status, result.status)
            assertEquals(personResponse.id, result.personalDetails.id)

            verify(exactly = 1) { advisorReadService.getAdvisorDataByMobileNo(mobileNo) }
            verify(exactly = 1) { personService.getPerson(personId) }
        }
    }

    @Nested
    @DisplayName("createAdvisor Tests")
    inner class CreateAdvisorTests {

        @Test
        @DisplayName("should create advisor successfully")
        fun `createAdvisor should create advisor successfully`() {
            // Given
            val createRequest = AdvisorCreateRequest(
                advisorCode = "ADV001",
                isEmployee = false,
                remarks = "Test advisor"
            )
            every { advisorWriteService.createAdvisorData(createRequest) } returns advisorData
            every { personService.getPerson(personId) } returns personResponse

            // When
            val result = advisorService.createAdvisor(createRequest)

            // Then
            assertNotNull(result)
            assertEquals(expectedResponse.id, result.id)
            assertEquals(expectedResponse.advisorCode, result.advisorCode)
            assertEquals(expectedResponse.status, result.status)
            assertEquals(personResponse.id, result.personalDetails.id)

            verify(exactly = 1) { advisorWriteService.createAdvisorData(createRequest) }
            verify(exactly = 1) { personService.getPerson(personId) }
        }
    }

    @Nested
    @DisplayName("updateAdvisor Tests")
    inner class UpdateAdvisorTests {

        @Test
        @DisplayName("should update advisor successfully")
        fun `updateAdvisor should update advisor successfully`() {
            // Given
            val updateRequest = AdvisorUpdateRequest(
                advisorCode = "ADV002",
                remarks = "Updated advisor"
            )
            every { advisorWriteService.updateAdvisorData(advisorId, updateRequest) } returns advisorData
            every { personService.getPerson(personId) } returns personResponse

            // When
            val result = advisorService.updateAdvisor(advisorId, updateRequest)

            // Then
            assertNotNull(result)
            assertEquals(expectedResponse.id, result.id)
            assertEquals(expectedResponse.advisorCode, result.advisorCode)
            assertEquals(expectedResponse.status, result.status)
            assertEquals(personResponse.id, result.personalDetails.id)

            verify(exactly = 1) { advisorWriteService.updateAdvisorData(advisorId, updateRequest) }
            verify(exactly = 1) { personService.getPerson(personId) }
        }
    }

    @Nested
    @DisplayName("deleteAdvisor Tests")
    inner class DeleteAdvisorTests {

        @Test
        @DisplayName("should delete advisor successfully")
        fun `deleteAdvisor should delete advisor successfully`() {
            // Given
            every { advisorWriteService.deleteAdvisor(advisorId) } returns Unit

            // When
            advisorService.deleteAdvisor(advisorId)

            // Then
            verify(exactly = 1) { advisorWriteService.deleteAdvisor(advisorId) }
        }
    }
}
