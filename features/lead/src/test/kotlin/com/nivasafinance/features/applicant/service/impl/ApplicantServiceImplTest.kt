package com.nivasafinance.features.applicant.service.impl

import com.nivasafinance.features.TestUtils.createTestApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantData
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.applicant.service.ApplicantReadService
import com.nivasafinance.features.applicant.service.ApplicantWriteService
import com.nivasafinance.features.person.dto.PersonData
import com.nivasafinance.features.person.service.PersonReadService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.context.MessageSource
import java.util.UUID

@DisplayName("ApplicantServiceImpl Tests")
class ApplicantServiceImplTest {

    private lateinit var applicantReadService: ApplicantReadService
    private lateinit var applicantWriteService: ApplicantWriteService
    private lateinit var personReadService: PersonReadService
    private lateinit var cacheManager: CacheManager
    private lateinit var cache: Cache
    private lateinit var messageSource: MessageSource
    private lateinit var applicantService: ApplicantServiceImpl

    @BeforeEach
    fun setUp() {
        applicantReadService = mockk()
        applicantWriteService = mockk()
        personReadService = mockk()
        cacheManager = mockk()
        cache = mockk()
        messageSource = mockk()
        applicantService = ApplicantServiceImpl(
            applicantReadService,
            applicantWriteService,
            personReadService,
            cacheManager
        )

        every { cacheManager.getCache("applicant") } returns cache
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    @Nested
    @DisplayName("getApplicant")
    inner class GetApplicant {

        @Test
        @DisplayName("should return applicant response when applicant exists")
        fun `should return applicant response when applicant exists`() {
            val applicantId = UUID.randomUUID()
            val applicantData = ApplicantData(
                id = applicantId,
                personId = UUID.randomUUID(),
                leadId = UUID.randomUUID(),
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            every { applicantReadService.getApplicant(applicantId) } returns applicantData

            val result = applicantService.getApplicant(applicantId)

            assertEquals(applicantId, result.id)
            assertEquals(applicantData.personId, result.personId)
            assertEquals(applicantData.leadId, result.leadId)
            assertEquals(applicantData.applicantType, result.applicantType)
            assertEquals(applicantData.relationshipToPrimary, result.relationshipToPrimary)
            assertEquals(applicantData.status, result.status)

            verify { applicantReadService.getApplicant(applicantId) }
        }

        @Test
        @DisplayName("should throw exception when applicant not found")
        fun `should throw exception when applicant not found`() {
            val applicantId = UUID.randomUUID()

            every {
                applicantReadService.getApplicant(applicantId)
            } throws ApplicantNotFoundException(applicantId, messageSource)

            assertThrows(ApplicantNotFoundException::class.java) {
                applicantService.getApplicant(applicantId)
            }

            verify { applicantReadService.getApplicant(applicantId) }
        }
    }

    @Nested
    @DisplayName("getApplicantsByLeadId")
    inner class GetApplicantsByLeadId {

        @Test
        @DisplayName("should return list of applicant responses when lead has applicants")
        fun `should return list of applicant responses when lead has applicants`() {
            val leadId = UUID.randomUUID()
            val applicantsData = listOf(
                ApplicantData(
                    id = UUID.randomUUID(),
                    personId = UUID.randomUUID(),
                    leadId = leadId,
                    applicantType = ApplicantType.PRIMARY,
                    relationshipToPrimary = RelationshipToPrimary.SELF,
                    status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
                ),
                ApplicantData(
                    id = UUID.randomUUID(),
                    personId = UUID.randomUUID(),
                    leadId = leadId,
                    applicantType = ApplicantType.CO_APPLICANT,
                    relationshipToPrimary = RelationshipToPrimary.SPOUSE,
                    status = ApplicantStatus.UNDER_REVIEW
                )
            )

            every { applicantReadService.getApplicantsByLeadId(leadId) } returns applicantsData

            val result = applicantService.getApplicantsByLeadId(leadId)

            assertEquals(2, result.size)
            assertEquals(applicantsData[0].id, result[0].id)
            assertEquals(applicantsData[1].id, result[1].id)

            verify { applicantReadService.getApplicantsByLeadId(leadId) }
        }

        @Test
        @DisplayName("should return empty list when lead has no applicants")
        fun `should return empty list when lead has no applicants`() {
            val leadId = UUID.randomUUID()

            every { applicantReadService.getApplicantsByLeadId(leadId) } returns emptyList()

            val result = applicantService.getApplicantsByLeadId(leadId)

            assertEquals(0, result.size)

            verify { applicantReadService.getApplicantsByLeadId(leadId) }
        }
    }



    @Nested
    @DisplayName("getApplicantsByMobileNumber")
    inner class GetApplicantsByMobileNumber {

        @Test
        @DisplayName("should return list of applicant responses when mobile number has applicants")
        fun `should return list of applicant responses when mobile number has applicants`() {
            val mobileNumber = "9876543210"
            val personId = UUID.randomUUID()
            val personData = PersonData(
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
            val applicantsData = listOf(
                ApplicantData(
                    id = UUID.randomUUID(),
                    personId = personId,
                    leadId = UUID.randomUUID(),
                    applicantType = ApplicantType.PRIMARY,
                    relationshipToPrimary = RelationshipToPrimary.SELF,
                    status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
                )
            )

            every { personReadService.getPersonByMobileNo(mobileNumber) } returns personData
            every { applicantReadService.getApplicantsByPersonId(personId) } returns applicantsData

            val result = applicantService.getApplicantsByMobileNumber(mobileNumber)

            assertEquals(1, result.size)
            assertEquals(applicantsData[0].id, result[0].id)
            assertEquals(personId, result[0].personId)

            verify { personReadService.getPersonByMobileNo(mobileNumber) }
            verify { applicantReadService.getApplicantsByPersonId(personId) }
        }

        @Test
        @DisplayName("should return empty list when mobile number has no applicants")
        fun `should return empty list when mobile number has no applicants`() {
            val mobileNumber = "9876543210"
            val personId = UUID.randomUUID()
            val personData = PersonData(
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

            every { personReadService.getPersonByMobileNo(mobileNumber) } returns personData
            every { applicantReadService.getApplicantsByPersonId(personId) } returns emptyList()

            val result = applicantService.getApplicantsByMobileNumber(mobileNumber)

            assertEquals(0, result.size)

            verify { personReadService.getPersonByMobileNo(mobileNumber) }
            verify { applicantReadService.getApplicantsByPersonId(personId) }
        }
    }

    @Nested
    @DisplayName("createApplicant")
    inner class CreateApplicant {

        @Test
        @DisplayName("should create applicant and return response")
        fun `should create applicant and return response`() {
            val request = createTestApplicantCreateRequest()
            val applicantId = UUID.randomUUID()
            val applicantData = ApplicantData(
                id = applicantId,
                personId = request.personId,
                leadId = request.leadId,
                applicantType = request.applicantType,
                relationshipToPrimary = request.relationshipToPrimary,
                status = request.status
            )

            every { applicantWriteService.createApplicant(request) } returns applicantId
            every { applicantReadService.getApplicant(applicantId) } returns applicantData

            val result = applicantService.createApplicant(request)

            assertEquals(applicantId, result.id)
            assertEquals(request.personId, result.personId)
            assertEquals(request.leadId, result.leadId)
            assertEquals(request.applicantType, result.applicantType)
            assertEquals(request.relationshipToPrimary, result.relationshipToPrimary)
            assertEquals(request.status, result.status)

            verify { applicantWriteService.createApplicant(request) }
            verify { applicantReadService.getApplicant(applicantId) }
        }
    }

    @Nested
    @DisplayName("updateApplicant")
    inner class UpdateApplicant {

        @Test
        @DisplayName("should update applicant and return response")
        fun `should update applicant and return response`() {
            val applicantId = UUID.randomUUID()
            val request = ApplicantUpdateRequest(
                applicantType = ApplicantType.CO_APPLICANT,
                relationshipToPrimary = RelationshipToPrimary.SPOUSE,
                status = ApplicantStatus.UNDER_REVIEW
            )
            val applicantData = ApplicantData(
                id = applicantId,
                personId = UUID.randomUUID(),
                leadId = UUID.randomUUID(),
                applicantType = request.applicantType!!,
                relationshipToPrimary = request.relationshipToPrimary!!,
                status = request.status!!
            )

            every { applicantWriteService.updateApplicant(applicantId, request) } returns Unit
            every { applicantReadService.getApplicant(applicantId) } returns applicantData

            val result = applicantService.updateApplicant(applicantId, request)

            assertEquals(applicantId, result.id)
            assertEquals(request.applicantType, result.applicantType)
            assertEquals(request.relationshipToPrimary, result.relationshipToPrimary)
            assertEquals(request.status, result.status)

            verify { applicantWriteService.updateApplicant(applicantId, request) }
            verify { applicantReadService.getApplicant(applicantId) }
        }
    }

    @Nested
    @DisplayName("deleteApplicant")
    inner class DeleteApplicant {

        @Test
        @DisplayName("should delete applicant and clear cache")
        fun `should delete applicant and clear cache`() {
            val applicantId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val applicantData = ApplicantData(
                id = applicantId,
                personId = UUID.randomUUID(),
                leadId = leadId,
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            every { applicantReadService.getApplicant(applicantId) } returns applicantData
            every { applicantWriteService.deleteApplicant(applicantId) } returns Unit
            every { cache.evict(any()) } returns Unit

            applicantService.deleteApplicant(applicantId)

            verify { applicantReadService.getApplicant(applicantId) }
            verify { applicantWriteService.deleteApplicant(applicantId) }
            verify { cache.evict(any()) }
        }
    }
}
