package com.nivasafinance.features.applicant.service.impl

import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import com.nivasafinance.features.applicant.entity.Applicant
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.applicant.repository.ApplicantRepository
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.service.PersonService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicantWriteServiceImplTest {

    private val applicantRepository = mockk<ApplicantRepository>()
    private val personService = mockk<PersonService>()
    private val leadService = mockk<LeadService>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var applicantWriteService: ApplicantWriteServiceImpl

    @BeforeEach
    fun setup() {
        clearAllMocks()
        applicantWriteService = ApplicantWriteServiceImpl(
            applicantRepository,
            personService,
            leadService
        )

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = applicantWriteService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(applicantWriteService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Applicant not found"

        // Mock personService methods
        every { personService.getPerson(any()) } returns mockk<PersonResponse>()

        // Mock leadService methods
        every { leadService.getLeadById(any()) } returns mockk()
    }

    @Nested
    @DisplayName("createApplicant Tests")
    inner class CreateApplicantTests {

        @Test
        @DisplayName("Should create applicant successfully with all required fields")
        fun `createApplicant should create and save applicant successfully`() {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val applicantType = ApplicantType.PRIMARY
            val relationshipToPrimary = RelationshipToPrimary.SPOUSE
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest(
                leadId = leadId,
                personId = personId,
                applicantType = applicantType,
                relationshipToPrimary = relationshipToPrimary,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(any()) } returns savedApplicant
            every { applicantRepository.findByLeadIdAndApplicantType(any(), any()) } returns emptyList()

            // When
            val result = applicantWriteService.createApplicant(request)

            // Then
            assertEquals(savedApplicantId, result)
            verify { applicantRepository.save(any()) }
        }

        @Test
        @DisplayName("Should create co-applicant successfully")
        fun `createApplicant should create co-applicant successfully`() {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val applicantType = ApplicantType.CO_APPLICANT
            val relationshipToPrimary = RelationshipToPrimary.SPOUSE
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest(
                leadId = leadId,
                personId = personId,
                applicantType = applicantType,
                relationshipToPrimary = relationshipToPrimary,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(any()) } returns savedApplicant
            every { applicantRepository.findByLeadIdAndApplicantType(any(), any()) } returns emptyList()

            // When
            val result = applicantWriteService.createApplicant(request)

            // Then
            assertEquals(savedApplicantId, result)
            verify { applicantRepository.save(any()) }
        }

        @Test
        @DisplayName("Should create guarantor successfully")
        fun `createApplicant should create guarantor successfully`() {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val applicantType = ApplicantType.GUARANTOR
            val relationshipToPrimary = RelationshipToPrimary.FATHER
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest(
                leadId = leadId,
                personId = personId,
                applicantType = applicantType,
                relationshipToPrimary = relationshipToPrimary,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(any()) } returns savedApplicant
            every { applicantRepository.findByLeadIdAndApplicantType(any(), any()) } returns emptyList()

            // When
            val result = applicantWriteService.createApplicant(request)

            // Then
            assertEquals(savedApplicantId, result)
            verify { applicantRepository.save(any()) }
        }

        @Test
        @DisplayName("Should throw exception when primary applicant already exists")
        fun `createApplicant should throw exception when primary applicant already exists`() {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val applicantType = ApplicantType.PRIMARY
            val relationshipToPrimary = RelationshipToPrimary.SELF

            val request = ApplicantCreateRequest(
                leadId = leadId,
                personId = personId,
                applicantType = applicantType,
                relationshipToPrimary = relationshipToPrimary,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            val existingApplicant = mockk<Applicant>()
            every { applicantRepository.findByLeadIdAndApplicantType(any(), any()) } returns listOf(existingApplicant)

            // When & Then
            assertThrows<Exception> {
                applicantWriteService.createApplicant(request)
            }
        }
    }

    @Nested
    @DisplayName("updateApplicant Tests")
    inner class UpdateApplicantTests {

        @Test
        @DisplayName("Should update applicant successfully")
        fun `updateApplicant should update applicant successfully`() {
            val applicantId = UUID.randomUUID()
            val request = ApplicantUpdateRequest(
                applicantType = ApplicantType.CO_APPLICANT,
                relationshipToPrimary = RelationshipToPrimary.SPOUSE,
                status = ApplicantStatus.UNDER_REVIEW
            )

            val existingApplicant = mockk<Applicant>(relaxed = true)
            every { existingApplicant.id } returns applicantId
            every { existingApplicant.personId } returns UUID.randomUUID()
            every { existingApplicant.leadId } returns UUID.randomUUID()
            every { existingApplicant.applicantType } returns ApplicantType.PRIMARY
            every { existingApplicant.relationshipToPrimary } returns RelationshipToPrimary.SELF
            every { existingApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            val updatedApplicant = mockk<Applicant>(relaxed = true)
            every { updatedApplicant.id } returns applicantId
            every { updatedApplicant.personId } returns existingApplicant.personId
            every { updatedApplicant.leadId } returns existingApplicant.leadId
            every { updatedApplicant.applicantType } returns request.applicantType!!
            every { updatedApplicant.relationshipToPrimary } returns request.relationshipToPrimary!!
            every { updatedApplicant.status } returns request.status!!

            every { applicantRepository.findById(applicantId) } returns java.util.Optional.of(existingApplicant)
            every { applicantRepository.save(any()) } returns updatedApplicant

            applicantWriteService.updateApplicant(applicantId, request)

            verify { applicantRepository.findById(applicantId) }
            verify { applicantRepository.save(any()) }
        }

        @Test
        @DisplayName("Should throw exception when applicant not found")
        fun `updateApplicant should throw exception when applicant not found`() {
            // Given
            val applicantId = UUID.randomUUID()
            val request = ApplicantUpdateRequest()

            every { applicantRepository.findById(applicantId) } returns java.util.Optional.empty()

            // When & Then
            assertThrows<ApplicantNotFoundException> {
                applicantWriteService.updateApplicant(applicantId, request)
            }
        }
    }

    @Nested
    @DisplayName("deleteApplicant Tests")
    inner class DeleteApplicantTests {

        @Test
        @DisplayName("Should delete applicant successfully")
        fun `deleteApplicant should delete applicant successfully`() {
            // Given
            val applicantId = UUID.randomUUID()
            val applicant = mockk<Applicant>()

            every { applicantRepository.findById(applicantId) } returns java.util.Optional.of(applicant)
            every { applicantRepository.delete(applicant) } just runs

            // When
            applicantWriteService.deleteApplicant(applicantId)

            // Then
            verify { applicantRepository.delete(applicant) }
        }

        @Test
        @DisplayName("Should throw exception when applicant not found")
        fun `deleteApplicant should throw exception when applicant not found`() {
            // Given
            val applicantId = UUID.randomUUID()

            every { applicantRepository.findById(applicantId) } returns java.util.Optional.empty()

            // When & Then
            assertThrows<ApplicantNotFoundException> {
                applicantWriteService.deleteApplicant(applicantId)
            }
        }
    }
}
