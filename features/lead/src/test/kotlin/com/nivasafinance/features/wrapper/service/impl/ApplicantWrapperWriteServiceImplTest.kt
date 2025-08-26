package com.nivasafinance.features.wrapper.service.impl

import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("ApplicantWrapperWriteServiceImpl Tests")
class ApplicantWrapperWriteServiceImplTest {

    private val personService = mockk<com.nivasafinance.features.person.service.PersonService>()
    private val leadService = mockk<com.nivasafinance.features.lead.service.LeadService>()
    private val applicantService = mockk<com.nivasafinance.features.applicant.service.ApplicantService>()

    private lateinit var applicantWrapperWriteService: ApplicantWrapperWriteServiceImpl

    private val applicantId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val leadId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        applicantWrapperWriteService = ApplicantWrapperWriteServiceImpl(
            personService,
            leadService,
            applicantService
        )
    }

    @Test
    @DisplayName("Should create applicant successfully")
    fun `createApplicant should create applicant successfully`() {
        // Given
        val request = createTestApplicantWrapperRequest()
        val personResponse = createTestPersonResponse()
        val leadResponse = createTestLeadResponse()
        val applicantResponse = createTestApplicantResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every { applicantService.createApplicant(any<ApplicantCreateRequest>()) } returns applicantResponse

        // When
        val result = applicantWrapperWriteService.createApplicant(request)

        // Then
        assertNotNull(result)
        assertEquals(applicantId, result.id)
        assertEquals(LeadStatus.ACTIVE, result.status)
        assertEquals(LeadStage.INQUIRY, result.stage)

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { applicantService.createApplicant(any<ApplicantCreateRequest>()) }
    }

    @Test
    @DisplayName("Should create applicant with minimal data")
    fun `createApplicant should create applicant with minimal data`() {
        // Given
        val request = createMinimalApplicantWrapperRequest()
        val personResponse = createTestPersonResponse()
        val leadResponse = createTestLeadResponse()
        val applicantResponse = createTestApplicantResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every { applicantService.createApplicant(any<ApplicantCreateRequest>()) } returns applicantResponse

        // When
        val result = applicantWrapperWriteService.createApplicant(request)

        // Then
        assertNotNull(result)
        assertEquals(applicantId, result.id)
        assertEquals(LeadStatus.ACTIVE, result.status)
        assertEquals(LeadStage.INQUIRY, result.stage)

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { applicantService.createApplicant(any<ApplicantCreateRequest>()) }
    }

    @Test
    @DisplayName("Should create applicant with complete data")
    fun `createApplicant should create applicant with complete data`() {
        // Given
        val request = createCompleteApplicantWrapperRequest()
        val personResponse = createTestPersonResponse()
        val leadResponse = createTestLeadResponse()
        val applicantResponse = createTestApplicantResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every { applicantService.createApplicant(any<ApplicantCreateRequest>()) } returns applicantResponse

        // When
        val result = applicantWrapperWriteService.createApplicant(request)

        // Then
        assertNotNull(result)
        assertEquals(applicantId, result.id)
        assertEquals(LeadStatus.ACTIVE, result.status)
        assertEquals(LeadStage.INQUIRY, result.stage)

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { applicantService.createApplicant(any<ApplicantCreateRequest>()) }
    }

    @Test
    @DisplayName("Should create applicant without lead data")
    fun `createApplicant should create applicant without lead data`() {
        // Given
        val request = createApplicantWithoutLeadData()
        val personResponse = createTestPersonResponse()
        val leadResponse = createTestLeadResponse()
        val applicantResponse = createTestApplicantResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every { applicantService.createApplicant(any<ApplicantCreateRequest>()) } returns applicantResponse

        // When
        val result = applicantWrapperWriteService.createApplicant(request)

        // Then
        assertNotNull(result)
        assertEquals(applicantId, result.id)
        assertEquals(LeadStatus.ACTIVE, result.status)
        assertEquals(LeadStage.INQUIRY, result.stage)

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { applicantService.createApplicant(any<ApplicantCreateRequest>()) }
    }

    @Test
    @DisplayName("Should create applicant without applicant data")
    fun `createApplicant should create applicant without applicant data`() {
        // Given
        val request = createApplicantWithoutApplicantData()
        val personResponse = createTestPersonResponse()
        val leadResponse = createTestLeadResponse()
        val applicantResponse = createTestApplicantResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every { applicantService.createApplicant(any<ApplicantCreateRequest>()) } returns applicantResponse

        // When
        val result = applicantWrapperWriteService.createApplicant(request)

        // Then
        assertNotNull(result)
        assertEquals(applicantId, result.id)
        assertEquals(LeadStatus.ACTIVE, result.status)
        assertEquals(LeadStage.INQUIRY, result.stage)

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { applicantService.createApplicant(any<ApplicantCreateRequest>()) }
    }

    @Test
    @DisplayName("Should handle person service exception")
    fun `createApplicant should handle person service exception`() {
        // Given
        val request = createTestApplicantWrapperRequest()

        every {
            personService.createPerson(any<PersonCreateRequest>())
        } throws RuntimeException("Person creation failed")

        // When & Then
        try {
            applicantWrapperWriteService.createApplicant(request)
        } catch (e: RuntimeException) {
            assertEquals("Person creation failed", e.message)
        }

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 0) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 0) { applicantService.createApplicant(any<ApplicantCreateRequest>()) }
    }

    @Test
    @DisplayName("Should handle lead service exception")
    fun `createApplicant should handle lead service exception`() {
        // Given
        val request = createTestApplicantWrapperRequest()
        val personResponse = createTestPersonResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } throws RuntimeException("Lead creation failed")

        // When & Then
        try {
            applicantWrapperWriteService.createApplicant(request)
        } catch (e: RuntimeException) {
            assertEquals("Lead creation failed", e.message)
        }

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 0) { applicantService.createApplicant(any<ApplicantCreateRequest>()) }
    }

    @Test
    @DisplayName("Should handle applicant service exception")
    fun `createApplicant should handle applicant service exception`() {
        // Given
        val request = createTestApplicantWrapperRequest()
        val personResponse = createTestPersonResponse()
        val leadResponse = createTestLeadResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every {
            applicantService.createApplicant(any<ApplicantCreateRequest>())
        } throws RuntimeException("Applicant creation failed")

        // When & Then
        try {
            applicantWrapperWriteService.createApplicant(request)
        } catch (e: RuntimeException) {
            assertEquals("Applicant creation failed", e.message)
        }

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { applicantService.createApplicant(any<ApplicantCreateRequest>()) }
    }

    private fun createTestApplicantWrapperRequest(): ApplicantWrapperRequest {
        return ApplicantWrapperRequest(
            personData = ApplicantWrapperRequest.PersonData(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    ApplicantWrapperRequest.PersonData.MobileNumberDetails("1234567890", true)
                ),
                email = "john.doe@example.com",
                dateOfBirth = "1990-01-01",
                gender = "MALE",
                addresses = listOf(
                    ApplicantWrapperRequest.PersonData.AddressData(
                        addressOne = "123 Main St",
                        addressTwo = "Apt 4B",
                        landmark = "Near Park",
                        district = "Central",
                        state = "Karnataka",
                        pincode = "560001",
                        addressSource = "AADHAR",
                        addressType = "PERMANENT"
                    )
                ),
                identifiers = listOf(
                    ApplicantWrapperRequest.PersonData.IdentifierData("123456789012", "AADHAR")
                )
            ),
            leadData = ApplicantWrapperRequest.LeadData(
                requestedAmount = BigDecimal("500000"),
                purpose = "Home Construction",
                productCode = "HL001",
                sourcingChannel = "Direct",
                preliminaryInformation = ApplicantWrapperRequest.LeadData.LeadPreliminaryInformation(
                    whenYouWantLoan = "Within 3 months",
                    isHouseConstructionStarted = false,
                    isEKhathaAvailable = true,
                    selfDeclaredAnnualFamilyIncome = 800000
                ),
                leadContacts = ApplicantWrapperRequest.LeadData.LeadContacts(
                    name = "Jane Doe",
                    number = "9876543210"
                ),
                stage = LeadStage.INQUIRY,
                status = LeadStatus.ACTIVE
            ),
            applicantData = ApplicantWrapperRequest.ApplicantData(
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )
        )
    }

    private fun createMinimalApplicantWrapperRequest(): ApplicantWrapperRequest {
        return ApplicantWrapperRequest(
            personData = ApplicantWrapperRequest.PersonData(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    ApplicantWrapperRequest.PersonData.MobileNumberDetails("1234567890", true)
                ),
                addresses = listOf(
                    ApplicantWrapperRequest.PersonData.AddressData(
                        pincode = "560001",
                        addressType = "PERMANENT"
                    )
                )
            )
        )
    }

    private fun createCompleteApplicantWrapperRequest(): ApplicantWrapperRequest {
        return ApplicantWrapperRequest(
            personData = ApplicantWrapperRequest.PersonData(
                firstName = "John",
                middleName = "Michael",
                lastName = "Doe",
                mobileNumbers = listOf(
                    ApplicantWrapperRequest.PersonData.MobileNumberDetails("1234567890", true),
                    ApplicantWrapperRequest.PersonData.MobileNumberDetails("0987654321", false)
                ),
                email = "john.michael.doe@example.com",
                dateOfBirth = "1990-01-01",
                gender = "MALE",
                addresses = listOf(
                    ApplicantWrapperRequest.PersonData.AddressData(
                        addressOne = "123 Main St",
                        addressTwo = "Apt 4B",
                        landmark = "Near Park",
                        district = "Central",
                        state = "Karnataka",
                        pincode = "560001",
                        addressSource = "AADHAR",
                        addressType = "PERMANENT"
                    ),
                    ApplicantWrapperRequest.PersonData.AddressData(
                        addressOne = "456 Work St",
                        addressTwo = "Office 10",
                        landmark = "Near Mall",
                        district = "Business",
                        state = "Karnataka",
                        pincode = "560002",
                        addressSource = "OFFICE",
                        addressType = "OFFICE"
                    )
                ),
                identifiers = listOf(
                    ApplicantWrapperRequest.PersonData.IdentifierData("123456789012", "AADHAR"),
                    ApplicantWrapperRequest.PersonData.IdentifierData("ABCD123456", "PAN")
                )
            ),
            leadData = ApplicantWrapperRequest.LeadData(
                requestedAmount = BigDecimal("1000000"),
                purpose = "Home Construction",
                productCode = "HL001",
                sourcingChannel = "Direct",
                preliminaryInformation = ApplicantWrapperRequest.LeadData.LeadPreliminaryInformation(
                    whenYouWantLoan = "Within 6 months",
                    isHouseConstructionStarted = true,
                    isEKhathaAvailable = true,
                    selfDeclaredAnnualFamilyIncome = 1200000
                ),
                leadContacts = ApplicantWrapperRequest.LeadData.LeadContacts(
                    name = "Jane Doe",
                    number = "9876543210"
                ),
                stage = LeadStage.INQUIRY,
                status = LeadStatus.ACTIVE
            ),
            applicantData = ApplicantWrapperRequest.ApplicantData(
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )
        )
    }

    private fun createApplicantWithoutLeadData(): ApplicantWrapperRequest {
        return ApplicantWrapperRequest(
            personData = ApplicantWrapperRequest.PersonData(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    ApplicantWrapperRequest.PersonData.MobileNumberDetails("1234567890", true)
                ),
                addresses = listOf(
                    ApplicantWrapperRequest.PersonData.AddressData(
                        pincode = "560001",
                        addressType = "PERMANENT"
                    )
                )
            ),
            applicantData = ApplicantWrapperRequest.ApplicantData(
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )
        )
    }

    private fun createApplicantWithoutApplicantData(): ApplicantWrapperRequest {
        return ApplicantWrapperRequest(
            personData = ApplicantWrapperRequest.PersonData(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    ApplicantWrapperRequest.PersonData.MobileNumberDetails("1234567890", true)
                ),
                addresses = listOf(
                    ApplicantWrapperRequest.PersonData.AddressData(
                        pincode = "560001",
                        addressType = "PERMANENT"
                    )
                )
            ),
            leadData = ApplicantWrapperRequest.LeadData(
                requestedAmount = BigDecimal("500000"),
                purpose = "Home Construction",
                productCode = "HL001",
                sourcingChannel = "Direct",
                preliminaryInformation = null,
                leadContacts = null
            )
        )
    }

    private fun createTestPersonResponse(): PersonResponse {
        return PersonResponse(
            id = personId,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            dateOfBirth = java.time.LocalDate.of(1990, 1, 1),
            gender = data.enums.Gender.MALE
        )
    }

    private fun createTestLeadResponse(): LeadResponse {
        return LeadResponse(
            id = leadId,
            requestedAmount = BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            sourcingChannel = "Direct",
            status = LeadStatus.ACTIVE,
            stage = LeadStage.INQUIRY,
            preliminaryInformation = null,
            leadContacts = null
        )
    }

    private fun createTestApplicantResponse(): ApplicantResponse {
        return ApplicantResponse(
            id = applicantId,
            leadId = leadId,
            personId = personId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
        )
    }
}
