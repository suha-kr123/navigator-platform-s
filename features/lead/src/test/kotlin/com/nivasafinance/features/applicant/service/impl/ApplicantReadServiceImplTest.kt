package com.nivasafinance.features.applicant.service.impl

import com.nivasafinance.features.TestUtils.createTestApplicant
import com.nivasafinance.features.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.applicant.repository.ApplicantRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.Optional
import java.util.UUID

@DisplayName("ApplicantReadServiceImpl Tests")
class ApplicantReadServiceImplTest {

    private lateinit var applicantRepository: ApplicantRepository
    private lateinit var messageSource: MessageSource
    private lateinit var applicantReadService: ApplicantReadServiceImpl

    @BeforeEach
    fun setUp() {
        applicantRepository = mockk()
        messageSource = mockk()
        applicantReadService = ApplicantReadServiceImpl(applicantRepository)

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = applicantReadService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(applicantReadService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    @Nested
    @DisplayName("getApplicant")
    inner class GetApplicant {

        @Test
        @DisplayName("should return applicant data when applicant exists")
        fun `should return applicant data when applicant exists`() {
            val applicantId = UUID.randomUUID()
            val applicant = createTestApplicant(id = applicantId)

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)

            val result = applicantReadService.getApplicant(applicantId)

            assertEquals(applicantId, result.id)
            assertEquals(applicant.personId, result.personId)
            assertEquals(applicant.leadId, result.leadId)
            assertEquals(applicant.applicantType, result.applicantType)
            assertEquals(applicant.relationshipToPrimary, result.relationshipToPrimary)
            assertEquals(applicant.status, result.status)

            verify { applicantRepository.findById(applicantId) }
        }

        @Test
        @DisplayName("should throw ApplicantNotFoundException when applicant does not exist")
        fun `should throw ApplicantNotFoundException when applicant does not exist`() {
            val applicantId = UUID.randomUUID()

            every { applicantRepository.findById(applicantId) } returns Optional.empty()

            assertThrows(ApplicantNotFoundException::class.java) {
                applicantReadService.getApplicant(applicantId)
            }

            verify { applicantRepository.findById(applicantId) }
        }
    }

    @Nested
    @DisplayName("getApplicantsByLeadId")
    inner class GetApplicantsByLeadId {

        @Test
        @DisplayName("should return list of applicants when lead has applicants")
        fun `should return list of applicants when lead has applicants`() {
            val leadId = UUID.randomUUID()
            val applicants = listOf(
                createTestApplicant(leadId = leadId),
                createTestApplicant(leadId = leadId)
            )

            every { applicantRepository.findByLeadId(leadId) } returns applicants

            val result = applicantReadService.getApplicantsByLeadId(leadId)

            assertEquals(2, result.size)
            assertEquals(applicants[0].id, result[0].id)
            assertEquals(applicants[1].id, result[1].id)

            verify { applicantRepository.findByLeadId(leadId) }
        }

        @Test
        @DisplayName("should return empty list when lead has no applicants")
        fun `should return empty list when lead has no applicants`() {
            val leadId = UUID.randomUUID()

            every { applicantRepository.findByLeadId(leadId) } returns emptyList()

            val result = applicantReadService.getApplicantsByLeadId(leadId)

            assertEquals(0, result.size)

            verify { applicantRepository.findByLeadId(leadId) }
        }
    }
}
