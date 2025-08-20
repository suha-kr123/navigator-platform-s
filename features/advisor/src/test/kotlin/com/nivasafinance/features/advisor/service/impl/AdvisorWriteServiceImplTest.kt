package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.TestUtils.createTestAdvisor
import com.nivasafinance.TestUtils.createTestAdvisorDto
import com.nivasafinance.TestUtils.createTestPersonDto
import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.exception.AdvisorMobileAlreadyExistsException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.person.service.PersonWriteService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.modelmapper.ModelMapper
import org.springframework.context.MessageSource
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorWriteServiceImpl Tests")
class AdvisorWriteServiceImplTest {

    private val advisorRepository = mockk<AdvisorRepository>()
    private val personWriteService = mockk<PersonWriteService>()
    private val advisorReadService = mockk<AdvisorReadService>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var advisorWriteService: AdvisorWriteServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val advisor = createTestAdvisor(id = advisorId, personId = personId)

    @BeforeEach
    fun setup() {
        advisorWriteService = AdvisorWriteServiceImpl(advisorRepository, personWriteService, advisorReadService).apply {
            this.modelMapper = this@AdvisorWriteServiceImplTest.modelMapper
            this.messageSource = this@AdvisorWriteServiceImplTest.messageSource
        }
    }

    @Nested
    @DisplayName("createAdvisor Tests")
    inner class CreateAdvisorTests {

        @Test
        @DisplayName("should create advisor successfully")
        fun `createAdvisor should create advisor successfully`() {
            // Given
            val newAdvisorDto = createTestAdvisorDto(
                id = null,
                personalDetails = createTestPersonDto(
                    id = null,
                    mobileNumber = com.nivasafinance.features.person.dto.MobileNumberDetails("1234567890")
                )
            )
            val savedPersonDto = createTestPersonDto(id = personId)
            val newAdvisor = createTestAdvisor(
                id = advisorId,
                personId = personId,
                advisorCode = "ADV001",
                status = AdvisorStatus.CREATED
            )
            val expectedDto = createTestAdvisorDto(
                id = advisorId,
                personalDetails = savedPersonDto
            )

            every { advisorReadService.getAdvisorByMobileNo("1234567890") } throws
                Exception("Advisor not found by mobile")
            every { personWriteService.savePerson(newAdvisorDto.personalDetails) } returns savedPersonDto
            every { advisorRepository.save(any()) } returns newAdvisor
            every { modelMapper.map(newAdvisor, AdvisorDto::class.java) } returns expectedDto

            // When
            val result = advisorWriteService.createAdvisor(newAdvisorDto)

            // Then
            assertNotNull(result)
            assertEquals(expectedDto.id, result.id)
            assertEquals(savedPersonDto.id, result.personalDetails.id)

            verify(exactly = 1) { advisorReadService.getAdvisorByMobileNo("1234567890") }
            verify(exactly = 1) { personWriteService.savePerson(newAdvisorDto.personalDetails) }
            verify(exactly = 1) { advisorRepository.save(any()) }
            verify(exactly = 1) { modelMapper.map(newAdvisor, AdvisorDto::class.java) }
        }

        @Test
        @DisplayName("should throw exception when mobile already exists")
        fun `createAdvisor should throw exception when mobile already exists`() {
            // Given
            val newAdvisorDto = createTestAdvisorDto(
                id = null,
                personalDetails = createTestPersonDto(
                    id = null,
                    mobileNumber = com.nivasafinance.features.person.dto.MobileNumberDetails("1234567890")
                )
            )
            val existingAdvisorDto = createTestAdvisorDto(id = UUID.randomUUID())

            every { advisorReadService.getAdvisorByMobileNo("1234567890") } returns existingAdvisorDto
            every { messageSource.getMessage("error.advisor.mobile.already.exists", any(), any()) } returns
                "Mobile already exists"

            // When & Then
            assertThrows<AdvisorMobileAlreadyExistsException> {
                advisorWriteService.createAdvisor(newAdvisorDto)
            }

            verify(exactly = 1) { advisorReadService.getAdvisorByMobileNo("1234567890") }
            verify(exactly = 0) { personWriteService.savePerson(any()) }
            verify(exactly = 0) { advisorRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("updateAdvisor Tests")
    inner class UpdateAdvisorTests {

        @Test
        @DisplayName("should update advisor successfully")
        fun `updateAdvisor should update advisor successfully`() {
            // Given
            val updateDto = createTestAdvisorDto(
                id = advisorId,
                personalDetails = createTestPersonDto(
                    id = personId,
                    firstName = "Updated",
                    mobileNumber = com.nivasafinance.features.person.dto.MobileNumberDetails("9876543210")
                )
            )
            val updatedPersonDto = createTestPersonDto(
                id = personId,
                firstName = "Updated",
                mobileNumber = com.nivasafinance.features.person.dto.MobileNumberDetails("9876543210")
            )
            val expectedDto = createTestAdvisorDto(
                id = advisorId,
                personalDetails = updatedPersonDto
            )

            every { advisorRepository.findById(advisorId) } returns Optional.of(advisor)
            every { personWriteService.updatePerson(personId, updateDto.personalDetails) } returns updatedPersonDto
            every { advisorRepository.save(advisor) } returns advisor
            every { modelMapper.map(advisor, AdvisorDto::class.java) } returns expectedDto

            // When
            val result = advisorWriteService.updateAdvisor(advisorId, updateDto)

            // Then
            assertNotNull(result)
            assertEquals(expectedDto.id, result.id)
            assertEquals(updatedPersonDto.id, result.personalDetails.id)
            assertEquals("Updated", result.personalDetails.firstName)

            verify(exactly = 1) { advisorRepository.findById(advisorId) }
            verify(exactly = 1) { personWriteService.updatePerson(personId, updateDto.personalDetails) }
            verify(exactly = 1) { advisorRepository.save(advisor) }
            verify(exactly = 1) { modelMapper.map(advisor, AdvisorDto::class.java) }
        }

        @Test
        @DisplayName("should throw exception when advisor not found")
        fun `updateAdvisor should throw exception when advisor not found`() {
            // Given
            val updateDto = createTestAdvisorDto(id = advisorId)

            every { advisorRepository.findById(advisorId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Advisor not found"

            // When & Then
            assertThrows<AdvisorNotFoundException> {
                advisorWriteService.updateAdvisor(advisorId, updateDto)
            }

            verify(exactly = 1) { advisorRepository.findById(advisorId) }
            verify(exactly = 0) { personWriteService.updatePerson(any(), any()) }
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
            val advisorDto = createTestAdvisorDto(
                id = advisorId,
                personalDetails = createTestPersonDto(id = personId)
            )

            every { advisorReadService.getAdvisor(advisorId) } returns advisorDto
            every { personWriteService.deletePerson(personId) } returns Unit
            every { advisorRepository.deleteById(advisorId) } returns Unit

            // When
            val result = advisorWriteService.deleteAdvisor(advisorId)

            // Then
            assertNotNull(result)
            assertEquals(advisorDto.id, result.id)

            verify(exactly = 1) { advisorReadService.getAdvisor(advisorId) }
            verify(exactly = 1) { personWriteService.deletePerson(personId) }
            verify(exactly = 1) { advisorRepository.deleteById(advisorId) }
        }
    }
}
