package com.nivasafinance.features.lead.applicant.controller

import com.nivasafinance.features.applicant.controller.ApplicantController
import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.applicant.service.ApplicantService
import com.nivasafinance.features.lead.TestUtils.createTestApplicantCreateRequest
import com.nivasafinance.features.lead.TestUtils.createTestApplicantResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

@DisplayName("ApplicantController Tests")
class ApplicantControllerTest {

    private lateinit var applicantService: ApplicantService
    private lateinit var applicantController: ApplicantController

    @BeforeEach
    fun setUp() {
        applicantService = mockk()
        applicantController = ApplicantController(applicantService)
    }

    @Test
    @DisplayName("Should create applicant successfully")
    fun `createApplicant should create applicant successfully`() {
        val request = createTestApplicantCreateRequest()
        val expectedResponse = createTestApplicantResponse()

        every { applicantService.createApplicant(request) } returns expectedResponse

        val result = applicantController.createApplicant(request)

        assertNotNull(result)
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify { applicantService.createApplicant(request) }
    }

    @Test
    @DisplayName("Should get applicant successfully")
    fun `getApplicant should get applicant successfully`() {
        val applicantId = UUID.randomUUID()
        val expectedResponse = createTestApplicantResponse()

        every { applicantService.getApplicant(applicantId) } returns expectedResponse

        val result = applicantController.getApplicant(applicantId)

        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify { applicantService.getApplicant(applicantId) }
    }

    @Test
    @DisplayName("Should update applicant successfully")
    fun `updateApplicant should update applicant successfully`() {
        val applicantId = UUID.randomUUID()
        val request = ApplicantUpdateRequest(
            applicantType = ApplicantType.CO_APPLICANT,
            relationshipToPrimary = RelationshipToPrimary.SPOUSE,
            status = ApplicantStatus.UNDER_REVIEW
        )
        val expectedResponse = createTestApplicantResponse()

        every { applicantService.updateApplicant(applicantId, request) } returns expectedResponse

        val result = applicantController.updateApplicant(applicantId, request)

        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify { applicantService.updateApplicant(applicantId, request) }
    }

    @Test
    @DisplayName("Should delete applicant successfully")
    fun `deleteApplicant should delete applicant successfully`() {
        val applicantId = UUID.randomUUID()

        every { applicantService.deleteApplicant(applicantId) } returns Unit

        val result = applicantController.deleteApplicant(applicantId)

        assertNotNull(result)
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
        verify { applicantService.deleteApplicant(applicantId) }
    }

    @Test
    @DisplayName("Should get applicants by lead successfully")
    fun `getApplicantsByLead should get applicants by lead successfully`() {
        val leadId = UUID.randomUUID()
        val expectedResponses = listOf(
            createTestApplicantResponse(),
            createTestApplicantResponse().copy(
                id = UUID.randomUUID(),
                applicantType = ApplicantType.CO_APPLICANT,
                relationshipToPrimary = RelationshipToPrimary.SPOUSE
            )
        )

        every { applicantService.getApplicantsByLeadId(leadId) } returns expectedResponses

        val result = applicantController.getApplicantsByLead(leadId)

        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(expectedResponses, result.body)
        assertEquals(2, result.body?.size)
        verify { applicantService.getApplicantsByLeadId(leadId) }
    }

    @Test
    @DisplayName("Should return empty list when no applicants found for lead")
    fun `getApplicantsByLead should return empty list when no applicants found`() {
        val leadId = UUID.randomUUID()

        every { applicantService.getApplicantsByLeadId(leadId) } returns emptyList()

        val result = applicantController.getApplicantsByLead(leadId)

        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(emptyList<ApplicantResponse>(), result.body)
        assertEquals(0, result.body?.size)
        verify { applicantService.getApplicantsByLeadId(leadId) }
    }
}
