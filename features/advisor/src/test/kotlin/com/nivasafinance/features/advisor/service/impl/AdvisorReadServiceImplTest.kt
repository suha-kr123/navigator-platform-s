package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.TestUtils.createTestAdvisor
import com.nivasafinance.TestUtils.createTestAdvisorDto
import com.nivasafinance.TestUtils.createTestPersonDto
import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.exception.AdvisorMobileNotFoundException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.person.service.PersonReadService
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

@DisplayName("AdvisorReadServiceImpl Tests")
class AdvisorReadServiceImplTest {

    private val personReadService = mockk<PersonReadService>()
    private val advisorRepository = mockk<AdvisorRepository>()
    private val modelMapper = mockk<ModelMapper>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var advisorReadService: AdvisorReadServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val advisor = createTestAdvisor(id = advisorId, personId = personId)
    private val personDto = createTestPersonDto(id = personId)
    private val expectedDto = createTestAdvisorDto(id = advisorId, personalDetails = personDto)

    @BeforeEach
    fun setup() {
        advisorReadService = AdvisorReadServiceImpl(personReadService, advisorRepository).apply {
            this.modelMapper = this@AdvisorReadServiceImplTest.modelMapper
            this.messageSource = this@AdvisorReadServiceImplTest.messageSource
        }
    }

    @Nested
    @DisplayName("getAdvisor Tests")
    inner class GetAdvisorTests {

        @Test
        @DisplayName("should return advisor when found")
        fun `getAdvisor should return advisor when found`() {
            // Given
            every { advisorRepository.findById(advisorId) } returns Optional.of(advisor)
            every { modelMapper.map(advisor, AdvisorDto::class.java) } returns expectedDto
            every { personReadService.getPerson(personId) } returns personDto

            // When
            val result = advisorReadService.getAdvisor(advisorId)

            // Then
            assertNotNull(result)
            assertEquals(expectedDto.id, result.id)
            assertEquals(expectedDto.advisorCode, result.advisorCode)
            assertEquals(expectedDto.status, result.status)
            assertEquals(personDto.id, result.personalDetails.id)

            verify(exactly = 1) { advisorRepository.findById(advisorId) }
            verify(exactly = 1) { modelMapper.map(advisor, AdvisorDto::class.java) }
            verify(exactly = 1) { personReadService.getPerson(personId) }
        }

        @Test
        @DisplayName("should throw exception when advisor not found")
        fun `getAdvisor should throw exception when not found`() {
            // Given
            every { advisorRepository.findById(advisorId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Advisor not found"

            // When & Then
            assertThrows<AdvisorNotFoundException> {
                advisorReadService.getAdvisor(advisorId)
            }

            verify(exactly = 1) { advisorRepository.findById(advisorId) }
            verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
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
            every { advisorRepository.findByPrimaryMobileNo(mobileNo) } returns advisor
            every { modelMapper.map(advisor, AdvisorDto::class.java) } returns expectedDto
            every { personReadService.getPerson(personId) } returns personDto

            // When
            val result = advisorReadService.getAdvisorByMobileNo(mobileNo)

            // Then
            assertNotNull(result)
            assertEquals(expectedDto.id, result.id)
            assertEquals(expectedDto.advisorCode, result.advisorCode)
            assertEquals(expectedDto.status, result.status)
            assertEquals(personDto.id, result.personalDetails.id)

            verify(exactly = 1) { advisorRepository.findByPrimaryMobileNo(mobileNo) }
            verify(exactly = 1) { modelMapper.map(advisor, AdvisorDto::class.java) }
            verify(exactly = 1) { personReadService.getPerson(personId) }
        }

        @Test
        @DisplayName("should throw exception when advisor not found by mobile number")
        fun `getAdvisorByMobileNo should throw exception when not found`() {
            // Given
            val mobileNo = "1234567890"
            every { advisorRepository.findByPrimaryMobileNo(mobileNo) } returns null
            every { messageSource.getMessage(any(), any(), any()) } returns "Advisor not found"

            // When & Then
            assertThrows<AdvisorMobileNotFoundException> {
                advisorReadService.getAdvisorByMobileNo(mobileNo)
            }

            verify(exactly = 1) { advisorRepository.findByPrimaryMobileNo(mobileNo) }
            verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        }
    }
}
