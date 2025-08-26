package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.TestUtils.createTestAdvisor
import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.person.dto.PersonData
import com.nivasafinance.features.person.service.PersonReadService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorWriteServiceImpl Tests")
class AdvisorWriteServiceImplTest {

    private val advisorRepository = mockk<AdvisorRepository>()
    private val personReadService = mockk<PersonReadService>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var advisorWriteService: AdvisorWriteServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val advisor = createTestAdvisor(id = advisorId, personId = personId)
    private val personData = PersonData(
        id = personId,
        firstName = "John",
        middleName = null,
        lastName = "Doe",
        mobileNumbers = null,
        email = null,
        dateOfBirth = null,
        gender = null,
        dataExt = null
    )

    @BeforeEach
    fun setup() {
        advisorWriteService = AdvisorWriteServiceImpl(advisorRepository, personReadService).apply {
            this.messageSource = this@AdvisorWriteServiceImplTest.messageSource
        }
    }

    @Nested
    @DisplayName("createAdvisorData Tests")
    inner class CreateAdvisorDataTests {

        @Test
        @DisplayName("should create advisor data successfully")
        fun `createAdvisorData should create advisor data successfully`() {
            // Given
            val createRequest = AdvisorCreateRequest(
                advisorCode = "ADV001",
                personId = personId,
                isEmployee = false,
                remarks = "Test advisor"
            )
            val newAdvisor = createTestAdvisor(
                id = advisorId,
                personId = personId,
                advisorCode = "ADV001",
                status = AdvisorStatus.CREATED
            )

            every { personReadService.getPerson(personId) } returns personData
            every { advisorRepository.findByPersonId(personId) } returns null
            every { advisorRepository.save(any()) } returns newAdvisor

            // When
            val result = advisorWriteService.createAdvisorData(createRequest)

            // Then
            assertNotNull(result)
            assertEquals(advisorId, result.id)
            assertEquals(personId, result.personId)
            assertEquals("ADV001", result.advisorCode)

            verify(exactly = 1) { personReadService.getPerson(personId) }
            verify(exactly = 1) { advisorRepository.findByPersonId(personId) }
            verify(exactly = 1) { advisorRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("updateAdvisorData Tests")
    inner class UpdateAdvisorDataTests {

        @Test
        @DisplayName("should update advisor data successfully")
        fun `updateAdvisorData should update advisor data successfully`() {
            // Given
            val updateRequest = AdvisorUpdateRequest(
                advisorCode = "ADV002",
                remarks = "Updated advisor"
            )
            val updatedAdvisor = createTestAdvisor(
                id = advisorId,
                personId = personId,
                advisorCode = "ADV002",
                status = AdvisorStatus.ACTIVE
            )

            every { advisorRepository.findById(advisorId) } returns Optional.of(advisor)
            every { advisorRepository.save(any()) } returns updatedAdvisor

            // When
            val result = advisorWriteService.updateAdvisorData(advisorId, updateRequest)

            // Then
            assertNotNull(result)
            assertEquals(advisorId, result.id)
            assertEquals(personId, result.personId)
            assertEquals("ADV002", result.advisorCode)

            verify(exactly = 1) { advisorRepository.findById(advisorId) }
            verify(exactly = 1) { advisorRepository.save(any()) }
        }

        @Test
        @DisplayName("should throw exception when advisor not found")
        fun `updateAdvisorData should throw exception when advisor not found`() {
            // Given
            val updateRequest = AdvisorUpdateRequest(
                advisorCode = "ADV002",
                remarks = "Updated advisor"
            )

            every { advisorRepository.findById(advisorId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Advisor not found"

            // When & Then
            assertThrows<AdvisorNotFoundException> {
                advisorWriteService.updateAdvisorData(advisorId, updateRequest)
            }

            verify(exactly = 1) { advisorRepository.findById(advisorId) }
            verify(exactly = 0) { advisorRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("deleteAdvisor Tests")
    inner class DeleteAdvisorTests {

        @Test
        @DisplayName("should delete advisor successfully")
        fun `deleteAdvisor should delete advisor successfully`() {
            // Given
            every { advisorRepository.findById(advisorId) } returns Optional.of(advisor)
            every { advisorRepository.delete(advisor) } returns Unit

            // When
            advisorWriteService.deleteAdvisor(advisorId)

            // Then
            verify(exactly = 1) { advisorRepository.findById(advisorId) }
            verify(exactly = 1) { advisorRepository.delete(advisor) }
        }
    }
}
