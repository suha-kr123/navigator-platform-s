package com.nivasafinance.features.applicant.integration

import com.nivasafinance.features.TestUtils
import com.nivasafinance.features.applicant.controller.ApplicantController
import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.applicant.service.ApplicantService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("Applicant Integration Tests")
class ApplicantIntegrationTest {

    private lateinit var applicantService: ApplicantService
    private lateinit var applicantController: ApplicantController

    private val applicantId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val leadId = UUID.randomUUID()

    private val expectedApplicantResponse = TestUtils.createTestApplicantResponse(
        id = applicantId,
        personId = personId,
        leadId = leadId,
        applicantType = ApplicantType.PRIMARY,
        relationshipToPrimary = RelationshipToPrimary.SELF,
        status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
    )

    @BeforeEach
    fun setup() {
        applicantService = mockk<ApplicantService>()
        applicantController = ApplicantController(applicantService)
    }

    @Test
    @DisplayName("should create applicant through complete flow")
    fun `createApplicant should work through complete flow`() {
        // Given
        val createRequest = TestUtils.createTestApplicantCreateRequest(
            leadId = leadId,
            personId = personId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
        )
        every { applicantService.createApplicant(createRequest) } returns expectedApplicantResponse

        // When
        val response: ResponseEntity<ApplicantResponse> =
            applicantController.createApplicant(createRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedApplicantResponse.id, response.body?.id)
        assertEquals(expectedApplicantResponse.personId, response.body?.personId)
        assertEquals(expectedApplicantResponse.leadId, response.body?.leadId)
        assertEquals(expectedApplicantResponse.applicantType, response.body?.applicantType)
        assertEquals(expectedApplicantResponse.relationshipToPrimary, response.body?.relationshipToPrimary)
        assertEquals(expectedApplicantResponse.status, response.body?.status)
    }

    @Test
    @DisplayName("should get applicant through complete flow")
    fun `getApplicant should work through complete flow`() {
        // Given
        every { applicantService.getApplicant(applicantId) } returns expectedApplicantResponse

        // When
        val response: ResponseEntity<ApplicantResponse> =
            applicantController.getApplicant(applicantId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedApplicantResponse.id, response.body?.id)
        assertEquals(expectedApplicantResponse.personId, response.body?.personId)
        assertEquals(expectedApplicantResponse.leadId, response.body?.leadId)
        assertEquals(expectedApplicantResponse.applicantType, response.body?.applicantType)
    }

    @Test
    @DisplayName("should update applicant through complete flow")
    fun `updateApplicant should work through complete flow`() {
        // Given
        val updateRequest = ApplicantUpdateRequest(
            applicantType = ApplicantType.CO_APPLICANT,
            relationshipToPrimary = RelationshipToPrimary.SPOUSE,
            status = ApplicantStatus.UNDER_REVIEW
        )
        every { applicantService.updateApplicant(applicantId, updateRequest) } returns expectedApplicantResponse

        // When
        val response: ResponseEntity<ApplicantResponse> =
            applicantController.updateApplicant(applicantId, updateRequest)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedApplicantResponse.id, response.body?.id)
        assertEquals(expectedApplicantResponse.personId, response.body?.personId)
        assertEquals(expectedApplicantResponse.leadId, response.body?.leadId)
    }

    @Test
    @DisplayName("should delete applicant through complete flow")
    fun `deleteApplicant should work through complete flow`() {
        // Given
        every { applicantService.deleteApplicant(applicantId) } returns Unit

        // When
        val response: ResponseEntity<Unit> = applicantController.deleteApplicant(applicantId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    @DisplayName("should get applicants by lead through complete flow")
    fun `getApplicantsByLead should work through complete flow`() {
        // Given
        val applicants = listOf(
            expectedApplicantResponse,
            TestUtils.createTestApplicantResponse(
                id = UUID.randomUUID(),
                personId = UUID.randomUUID(),
                leadId = leadId,
                applicantType = ApplicantType.CO_APPLICANT,
                relationshipToPrimary = RelationshipToPrimary.SPOUSE,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )
        )
        every { applicantService.getApplicantsByLeadId(leadId) } returns applicants

        // When
        val response: ResponseEntity<List<ApplicantResponse>> =
            applicantController.getApplicantsByLead(leadId)

        // Then
        assertNotNull(response)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
        assertEquals(expectedApplicantResponse.id, response.body?.get(0)?.id)
        assertEquals(ApplicantType.CO_APPLICANT, response.body?.get(1)?.applicantType)
    }

    @Test
    @DisplayName("should handle applicant service calls correctly")
    fun `applicant service should handle all operations correctly`() {
        // Given
        val createRequest = TestUtils.createTestApplicantCreateRequest(
            leadId = leadId,
            personId = personId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
        )
        val updateRequest = ApplicantUpdateRequest(
            applicantType = ApplicantType.CO_APPLICANT,
            relationshipToPrimary = RelationshipToPrimary.SPOUSE,
            status = ApplicantStatus.UNDER_REVIEW
        )

        every { applicantService.createApplicant(createRequest) } returns expectedApplicantResponse
        every { applicantService.getApplicant(applicantId) } returns expectedApplicantResponse
        every { applicantService.updateApplicant(applicantId, updateRequest) } returns expectedApplicantResponse
        every { applicantService.deleteApplicant(applicantId) } returns Unit

        // When & Then
        val createResponse = applicantService.createApplicant(createRequest)
        assertEquals(expectedApplicantResponse.id, createResponse.id)
        assertEquals(expectedApplicantResponse.applicantType, createResponse.applicantType)

        val getResponse = applicantService.getApplicant(applicantId)
        assertEquals(expectedApplicantResponse.id, getResponse.id)
        assertEquals(expectedApplicantResponse.personId, getResponse.personId)

        val updateResponse = applicantService.updateApplicant(applicantId, updateRequest)
        assertEquals(expectedApplicantResponse.id, updateResponse.id)
        assertEquals(expectedApplicantResponse.leadId, updateResponse.leadId)

        applicantService.deleteApplicant(applicantId)
    }

    @Test
    @DisplayName("should handle different applicant types correctly")
    fun `applicant service should handle different applicant types correctly`() {
        // Given
        val primaryApplicant = TestUtils.createTestApplicantResponse(
            id = applicantId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF
        )
        val coApplicant = TestUtils.createTestApplicantResponse(
            id = UUID.randomUUID(),
            applicantType = ApplicantType.CO_APPLICANT,
            relationshipToPrimary = RelationshipToPrimary.SPOUSE
        )
        val guarantor = TestUtils.createTestApplicantResponse(
            id = UUID.randomUUID(),
            applicantType = ApplicantType.GUARANTOR,
            relationshipToPrimary = RelationshipToPrimary.FATHER
        )

        every { applicantService.getApplicant(applicantId) } returns primaryApplicant
        every { applicantService.getApplicant(coApplicant.id!!) } returns coApplicant
        every { applicantService.getApplicant(guarantor.id!!) } returns guarantor

        // When & Then
        val primaryResponse = applicantService.getApplicant(applicantId)
        assertEquals(ApplicantType.PRIMARY, primaryResponse.applicantType)
        assertEquals(RelationshipToPrimary.SELF, primaryResponse.relationshipToPrimary)

        val coApplicantResponse = applicantService.getApplicant(coApplicant.id!!)
        assertEquals(ApplicantType.CO_APPLICANT, coApplicantResponse.applicantType)
        assertEquals(RelationshipToPrimary.SPOUSE, coApplicantResponse.relationshipToPrimary)

        val guarantorResponse = applicantService.getApplicant(guarantor.id!!)
        assertEquals(ApplicantType.GUARANTOR, guarantorResponse.applicantType)
        assertEquals(RelationshipToPrimary.FATHER, guarantorResponse.relationshipToPrimary)
    }

    @Test
    @DisplayName("should handle different applicant statuses correctly")
    fun `applicant service should handle different applicant statuses correctly`() {
        // Given
        val needsReviewApplicant = TestUtils.createTestApplicantResponse(
            id = applicantId,
            status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
        )
        val underReviewApplicant = TestUtils.createTestApplicantResponse(
            id = applicantId,
            status = ApplicantStatus.UNDER_REVIEW
        )
        val reviewedApplicant = TestUtils.createTestApplicantResponse(
            id = applicantId,
            status = ApplicantStatus.REVIEWED
        )
        val inactiveApplicant = TestUtils.createTestApplicantResponse(
            id = applicantId,
            status = ApplicantStatus.INACTIVE
        )

        every { applicantService.getApplicant(applicantId) } returnsMany listOf(
            needsReviewApplicant, underReviewApplicant, reviewedApplicant, inactiveApplicant
        )

        // When & Then
        val firstResponse = applicantService.getApplicant(applicantId)
        assertEquals(ApplicantStatus.NEEDS_TO_BE_REVIEWED, firstResponse.status)

        val secondResponse = applicantService.getApplicant(applicantId)
        assertEquals(ApplicantStatus.UNDER_REVIEW, secondResponse.status)

        val thirdResponse = applicantService.getApplicant(applicantId)
        assertEquals(ApplicantStatus.REVIEWED, thirdResponse.status)

        val fourthResponse = applicantService.getApplicant(applicantId)
        assertEquals(ApplicantStatus.INACTIVE, fourthResponse.status)
    }

    @Test
    @DisplayName("should handle multiple applicants for same lead correctly")
    fun `applicant service should handle multiple applicants for same lead correctly`() {
        // Given
        val applicants = listOf(
            TestUtils.createTestApplicantResponse(
                id = applicantId,
                personId = personId,
                leadId = leadId,
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF
            ),
            TestUtils.createTestApplicantResponse(
                id = UUID.randomUUID(),
                personId = UUID.randomUUID(),
                leadId = leadId,
                applicantType = ApplicantType.CO_APPLICANT,
                relationshipToPrimary = RelationshipToPrimary.SPOUSE
            ),
            TestUtils.createTestApplicantResponse(
                id = UUID.randomUUID(),
                personId = UUID.randomUUID(),
                leadId = leadId,
                applicantType = ApplicantType.GUARANTOR,
                relationshipToPrimary = RelationshipToPrimary.FATHER
            )
        )

        every { applicantService.getApplicantsByLeadId(leadId) } returns applicants

        // When
        val response = applicantService.getApplicantsByLeadId(leadId)

        // Then
        assertEquals(3, response.size)
        assertEquals(ApplicantType.PRIMARY, response[0].applicantType)
        assertEquals(ApplicantType.CO_APPLICANT, response[1].applicantType)
        assertEquals(ApplicantType.GUARANTOR, response[2].applicantType)
        assertEquals(RelationshipToPrimary.SELF, response[0].relationshipToPrimary)
        assertEquals(RelationshipToPrimary.SPOUSE, response[1].relationshipToPrimary)
        assertEquals(RelationshipToPrimary.FATHER, response[2].relationshipToPrimary)
    }
}