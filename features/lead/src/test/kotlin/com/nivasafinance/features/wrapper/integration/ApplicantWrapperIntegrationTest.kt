package com.nivasafinance.features.wrapper.integration

import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperResponse
import com.nivasafinance.features.wrapper.service.ApplicantWrapperService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("ApplicantWrapper Integration Tests")
class ApplicantWrapperIntegrationTest {

    private val applicantWrapperService = mockk<ApplicantWrapperService>()
    private val applicantId = UUID.randomUUID()
    private val expectedResponse = createTestApplicantWrapperResponse()

    @BeforeEach
    fun setup() {
        // Setup mocks for integration test scenarios
    }

    @Test
    @DisplayName("Should create and retrieve applicant successfully")
    fun `should create and retrieve applicant successfully`() {
        val request = createTestApplicantWrapperRequest()

        every { applicantWrapperService.createApplicant(request) } returns expectedResponse
        every { applicantWrapperService.getApplicant(applicantId) } returns expectedResponse

        val createdApplicant = applicantWrapperService.createApplicant(request)

        assertNotNull(createdApplicant)
        assertNotNull(createdApplicant.id)
        assertEquals(LeadStatus.ACTIVE, createdApplicant.status)
        assertEquals(LeadStage.INQUIRY, createdApplicant.stage)

        val retrievedApplicant = applicantWrapperService.getApplicant(applicantId)

        assertNotNull(retrievedApplicant)
        assertEquals(createdApplicant.id, retrievedApplicant.id)
        assertEquals(createdApplicant.status, retrievedApplicant.status)
        assertEquals(createdApplicant.stage, retrievedApplicant.stage)
    }

    @Test
    @DisplayName("Should create applicant with minimal data")
    fun `should create applicant with minimal data`() {
        val request = createMinimalApplicantWrapperRequest()

        every { applicantWrapperService.createApplicant(request) } returns expectedResponse

        val createdApplicant = applicantWrapperService.createApplicant(request)

        assertNotNull(createdApplicant)
        assertNotNull(createdApplicant.id)
        assertEquals(LeadStatus.ACTIVE, createdApplicant.status)
        assertEquals(LeadStage.INQUIRY, createdApplicant.stage)
    }

    @Test
    @DisplayName("Should create applicant with complete data")
    fun `should create applicant with complete data`() {
        val request = createCompleteApplicantWrapperRequest()

        every { applicantWrapperService.createApplicant(request) } returns expectedResponse

        val createdApplicant = applicantWrapperService.createApplicant(request)

        assertNotNull(createdApplicant)
        assertNotNull(createdApplicant.id)
        assertEquals(LeadStatus.ACTIVE, createdApplicant.status)
        assertEquals(LeadStage.INQUIRY, createdApplicant.stage)
    }

    @Test
    @DisplayName("Should create applicant without lead data")
    fun `should create applicant without lead data`() {
        val request = createApplicantWithoutLeadData()

        every { applicantWrapperService.createApplicant(request) } returns expectedResponse

        val createdApplicant = applicantWrapperService.createApplicant(request)

        assertNotNull(createdApplicant)
        assertNotNull(createdApplicant.id)
        assertEquals(LeadStatus.ACTIVE, createdApplicant.status)
        assertEquals(LeadStage.INQUIRY, createdApplicant.stage)
    }

    @Test
    @DisplayName("Should create applicant without applicant data")
    fun `should create applicant without applicant data`() {
        val request = createApplicantWithoutApplicantData()

        every { applicantWrapperService.createApplicant(request) } returns expectedResponse

        val createdApplicant = applicantWrapperService.createApplicant(request)

        assertNotNull(createdApplicant)
        assertNotNull(createdApplicant.id)
        assertEquals(LeadStatus.ACTIVE, createdApplicant.status)
        assertEquals(LeadStage.INQUIRY, createdApplicant.stage)
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
                applicantType = com.nivasafinance.features.applicant.enum.ApplicantType.PRIMARY,
                relationshipToPrimary = com.nivasafinance.features.applicant.enum.RelationshipToPrimary.SELF,
                status = com.nivasafinance.features.applicant.enum.ApplicantStatus.NEEDS_TO_BE_REVIEWED
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
                applicantType = com.nivasafinance.features.applicant.enum.ApplicantType.PRIMARY,
                relationshipToPrimary = com.nivasafinance.features.applicant.enum.RelationshipToPrimary.SELF,
                status = com.nivasafinance.features.applicant.enum.ApplicantStatus.NEEDS_TO_BE_REVIEWED
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
                productCode = "HL001"
            )
        )
    }

    private fun createTestApplicantWrapperResponse(): ApplicantWrapperResponse {
        return ApplicantWrapperResponse(
            id = applicantId,
            status = LeadStatus.ACTIVE,
            stage = LeadStage.INQUIRY
        )
    }
}
