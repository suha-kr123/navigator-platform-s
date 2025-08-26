package com.nivasafinance.features.advisor.service.impl

import com.nivasafinance.TestUtils.createTestAdvisor
import com.nivasafinance.features.advisor.exception.AdvisorMobileNotFoundException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
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

@DisplayName("AdvisorReadServiceImpl Tests")
class AdvisorReadServiceImplTest {

    private val advisorRepository = mockk<AdvisorRepository>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var advisorReadService: AdvisorReadServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val advisor = createTestAdvisor(id = advisorId, personId = personId)

    @BeforeEach
    fun setup() {
        advisorReadService = AdvisorReadServiceImpl(advisorRepository).apply {
            this.messageSource = this@AdvisorReadServiceImplTest.messageSource
        }
    }

    @Nested
    @DisplayName("getAdvisorData Tests")
    inner class GetAdvisorDataTests {

        @Test
        @DisplayName("should return advisor data when found")
        fun `getAdvisorData should return advisor data when found`() {
            // Given
            every { advisorRepository.findById(advisorId) } returns Optional.of(advisor)

            // When
            val result = advisorReadService.getAdvisorData(advisorId)

            // Then
            assertNotNull(result)
            assertEquals(advisorId, result.id)
            assertEquals(personId, result.personId)
            assertEquals("ADV001", result.advisorCode)

            verify(exactly = 1) { advisorRepository.findById(advisorId) }
        }

        @Test
        @DisplayName("should throw exception when advisor not found")
        fun `getAdvisorData should throw exception when not found`() {
            // Given
            every { advisorRepository.findById(advisorId) } returns Optional.empty()
            every { messageSource.getMessage(any(), any(), any()) } returns "Advisor not found"

            // When & Then
            assertThrows<AdvisorNotFoundException> {
                advisorReadService.getAdvisorData(advisorId)
            }

            verify(exactly = 1) { advisorRepository.findById(advisorId) }
            verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        }
    }

    @Nested
    @DisplayName("getAdvisorDataByMobileNo Tests")
    inner class GetAdvisorDataByMobileNoTests {

        @Test
        @DisplayName("should return advisor data when found by mobile number")
        fun `getAdvisorDataByMobileNo should return advisor data when found`() {
            // Given
            val mobileNo = "1234567890"
            every { advisorRepository.findByPrimaryMobileNo(mobileNo) } returns advisor

            // When
            val result = advisorReadService.getAdvisorDataByMobileNo(mobileNo)

            // Then
            assertNotNull(result)
            assertEquals(advisorId, result.id)
            assertEquals(personId, result.personId)
            assertEquals("ADV001", result.advisorCode)

            verify(exactly = 1) { advisorRepository.findByPrimaryMobileNo(mobileNo) }
        }

        @Test
        @DisplayName("should throw exception when advisor not found by mobile number")
        fun `getAdvisorDataByMobileNo should throw exception when not found`() {
            // Given
            val mobileNo = "1234567890"
            every { advisorRepository.findByPrimaryMobileNo(mobileNo) } returns null
            every { messageSource.getMessage(any(), any(), any()) } returns "Advisor not found"

            // When & Then
            assertThrows<AdvisorMobileNotFoundException> {
                advisorReadService.getAdvisorDataByMobileNo(mobileNo)
            }

            verify(exactly = 1) { advisorRepository.findByPrimaryMobileNo(mobileNo) }
            verify(exactly = 1) { messageSource.getMessage(any(), any(), any()) }
        }
    }
}
