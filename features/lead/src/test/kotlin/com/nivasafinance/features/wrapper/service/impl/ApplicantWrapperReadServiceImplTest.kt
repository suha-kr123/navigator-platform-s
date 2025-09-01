package com.nivasafinance.features.wrapper.service.impl

import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("ApplicantWrapperReadServiceImpl Tests")
class ApplicantWrapperReadServiceImplTest {

    private val leadService = mockk<com.nivasafinance.features.lead.service.LeadService>()
    private val applicantService = mockk<com.nivasafinance.features.applicant.service.ApplicantService>()

    private lateinit var applicantWrapperReadService: ApplicantWrapperReadServiceImpl

    private val applicantId = UUID.randomUUID()
    private val leadId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        applicantWrapperReadService = ApplicantWrapperReadServiceImpl(
            leadService,
            applicantService
        )
    }

    @Test
    @DisplayName("Should get applicant successfully")
    fun `getApplicant should get applicant successfully`() {
        // Given
        val applicantResponse = createTestApplicantResponse()
        val leadResponse = createTestLeadResponse()

        every { applicantService.getApplicant(applicantId) } returns applicantResponse
        every { leadService.getLeadById(leadId) } returns leadResponse

        // When
        val result = applicantWrapperReadService.getApplicant(applicantId)

        // Then
        assertNotNull(result)
        assertEquals(applicantId, result.id)
        assertEquals(LeadStatus.ACTIVE, result.status)
        assertEquals(LeadStage.INQUIRY, result.stage)

        verify(exactly = 1) { applicantService.getApplicant(applicantId) }
        verify(exactly = 1) { leadService.getLeadById(leadId) }
    }

    @Test
    @DisplayName("Should get applicant with null lead status and stage")
    fun `getApplicant should get applicant with null lead status and stage`() {
        // Given
        val applicantResponse = createTestApplicantResponse()
        val leadResponse = createTestLeadResponseWithNullValues()

        every { applicantService.getApplicant(applicantId) } returns applicantResponse
        every { leadService.getLeadById(leadId) } returns leadResponse

        // When
        val result = applicantWrapperReadService.getApplicant(applicantId)

        // Then
        assertNotNull(result)
        assertEquals(applicantId, result.id)
        assertEquals(LeadStatus.ACTIVE, result.status) // Default value
        assertEquals(LeadStage.INQUIRY, result.stage) // Default value

        verify(exactly = 1) { applicantService.getApplicant(applicantId) }
        verify(exactly = 1) { leadService.getLeadById(leadId) }
    }

    @Test
    @DisplayName("Should handle applicant service exception")
    fun `getApplicant should handle applicant service exception`() {
        // Given
        every { applicantService.getApplicant(applicantId) } throws RuntimeException("Applicant not found")

        // When & Then
        try {
            applicantWrapperReadService.getApplicant(applicantId)
        } catch (e: RuntimeException) {
            assertEquals("Applicant not found", e.message)
        }

        verify(exactly = 1) { applicantService.getApplicant(applicantId) }
        verify(exactly = 0) { leadService.getLeadById(any<UUID>()) }
    }

    @Test
    @DisplayName("Should handle lead service exception")
    fun `getApplicant should handle lead service exception`() {
        // Given
        val applicantResponse = createTestApplicantResponse()

        every { applicantService.getApplicant(applicantId) } returns applicantResponse
        every { leadService.getLeadById(leadId) } throws RuntimeException("Lead not found")

        // When & Then
        try {
            applicantWrapperReadService.getApplicant(applicantId)
        } catch (e: RuntimeException) {
            assertEquals("Lead not found", e.message)
        }

        verify(exactly = 1) { applicantService.getApplicant(applicantId) }
        verify(exactly = 1) { leadService.getLeadById(leadId) }
    }

    private fun createTestApplicantResponse(): ApplicantResponse {
        return ApplicantResponse(
            id = applicantId,
            leadId = leadId,
            personId = UUID.randomUUID(),
            applicantType = com.nivasafinance.features.applicant.enum.ApplicantType.PRIMARY,
            relationshipToPrimary = com.nivasafinance.features.applicant.enum.RelationshipToPrimary.SELF,
            status = com.nivasafinance.features.applicant.enum.ApplicantStatus.NEEDS_TO_BE_REVIEWED
        )
    }

    private fun createTestLeadResponse(): LeadResponse {
        return LeadResponse(
            id = leadId,
            requestedAmount = java.math.BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            sourcingChannel = SourcingChannel.DIRECT,
            status = LeadStatus.ACTIVE,
            stage = LeadStage.INQUIRY,
            preliminaryInformation = null,
            leadContacts = null
        )
    }

    private fun createTestLeadResponseWithNullValues(): LeadResponse {
        return LeadResponse(
            id = leadId,
            requestedAmount = java.math.BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            sourcingChannel = SourcingChannel.DIRECT,
            status = null,
            stage = null,
            preliminaryInformation = null,
            leadContacts = null
        )
    }
}
