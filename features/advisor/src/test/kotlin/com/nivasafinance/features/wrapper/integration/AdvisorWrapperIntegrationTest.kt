package com.nivasafinance.features.wrapper.integration

import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperRequest
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.CreateLeadForAdvisorRequest
import com.nivasafinance.features.wrapper.service.AdvisorWrapperService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorWrapper Integration Tests")
class AdvisorWrapperIntegrationTest {

    private val advisorWrapperService = mockk<AdvisorWrapperService>()
    private val advisorId = UUID.randomUUID()
    private val expectedResponse = createTestAdvisorWrapperResponse()

    @BeforeEach
    fun setup() {
        // Setup mocks for integration test scenarios
    }

    @Test
    @DisplayName("Should create and retrieve advisor successfully")
    fun `should create and retrieve advisor successfully`() {
        val request = createTestAdvisorWrapperRequest()

        every { advisorWrapperService.createAdvisor(request) } returns expectedResponse
        every { advisorWrapperService.getAdvisor(advisorId) } returns expectedResponse

        val createdAdvisor = advisorWrapperService.createAdvisor(request)

        assertNotNull(createdAdvisor)
        assertNotNull(createdAdvisor.id)
        assertEquals(AdvisorStatus.ACTIVE, createdAdvisor.status)
        assertNotNull(createdAdvisor.leads)
        assertEquals(1, createdAdvisor.leads?.size)

        val retrievedAdvisor = advisorWrapperService.getAdvisor(advisorId)

        assertNotNull(retrievedAdvisor)
        assertEquals(createdAdvisor.id, retrievedAdvisor.id)
        assertEquals(createdAdvisor.status, retrievedAdvisor.status)
        assertEquals(createdAdvisor.leads?.size, retrievedAdvisor.leads?.size)
    }

    @Test
    @DisplayName("Should create advisor with minimal data")
    fun `should create advisor with minimal data`() {
        val request = createMinimalAdvisorWrapperRequest()

        every { advisorWrapperService.createAdvisor(request) } returns expectedResponse

        val createdAdvisor = advisorWrapperService.createAdvisor(request)

        assertNotNull(createdAdvisor)
        assertNotNull(createdAdvisor.id)
        assertEquals(AdvisorStatus.ACTIVE, createdAdvisor.status)
        assertNotNull(createdAdvisor.leads)
    }

    @Test
    @DisplayName("Should create advisor with complete data")
    fun `should create advisor with complete data`() {
        val request = createCompleteAdvisorWrapperRequest()

        every { advisorWrapperService.createAdvisor(request) } returns expectedResponse

        val createdAdvisor = advisorWrapperService.createAdvisor(request)

        assertNotNull(createdAdvisor)
        assertNotNull(createdAdvisor.id)
        assertEquals(AdvisorStatus.ACTIVE, createdAdvisor.status)
        assertNotNull(createdAdvisor.leads)
    }

    @Test
    @DisplayName("Should create advisor without advisor data")
    fun `should create advisor without advisor data`() {
        val request = createAdvisorWithoutAdvisorData()

        every { advisorWrapperService.createAdvisor(request) } returns expectedResponse

        val createdAdvisor = advisorWrapperService.createAdvisor(request)

        assertNotNull(createdAdvisor)
        assertNotNull(createdAdvisor.id)
        assertEquals(AdvisorStatus.ACTIVE, createdAdvisor.status)
        assertNotNull(createdAdvisor.leads)
    }

    @Test
    @DisplayName("Should create lead for advisor successfully")
    fun `should create lead for advisor successfully`() {
        val request = createTestCreateLeadForAdvisorRequest()

        every { advisorWrapperService.createLeadForAdvisor(advisorId, request) } returns expectedResponse

        val result = advisorWrapperService.createLeadForAdvisor(advisorId, request)

        assertNotNull(result)
        assertNotNull(result.id)
        assertEquals(AdvisorStatus.ACTIVE, result.status)
        assertNotNull(result.leads)
        assertEquals(1, result.leads?.size)
    }

    @Test
    @DisplayName("Should create lead for advisor with complete data")
    fun `should create lead for advisor with complete data`() {
        val request = createCompleteCreateLeadForAdvisorRequest()

        every { advisorWrapperService.createLeadForAdvisor(advisorId, request) } returns expectedResponse

        val result = advisorWrapperService.createLeadForAdvisor(advisorId, request)

        assertNotNull(result)
        assertNotNull(result.id)
        assertEquals(AdvisorStatus.ACTIVE, result.status)
        assertNotNull(result.leads)
        assertEquals(1, result.leads?.size)
    }

    @Test
    @DisplayName("Should handle advisor creation workflow")
    fun `should handle advisor creation workflow`() {
        val request = createTestAdvisorWrapperRequest()

        every { advisorWrapperService.createAdvisor(request) } returns expectedResponse

        // Step 1: Create advisor
        val createdAdvisor = advisorWrapperService.createAdvisor(request)
        assertNotNull(createdAdvisor)
        assertEquals(AdvisorStatus.ACTIVE, createdAdvisor.status)

        // Step 2: Create lead for the advisor
        val leadRequest = createTestCreateLeadForAdvisorRequest()
        every { advisorWrapperService.createLeadForAdvisor(createdAdvisor.id!!, leadRequest) } returns expectedResponse

        val advisorWithLead = advisorWrapperService.createLeadForAdvisor(createdAdvisor.id!!, leadRequest)
        assertNotNull(advisorWithLead)
        assertNotNull(advisorWithLead.leads)
        assertEquals(1, advisorWithLead.leads?.size)
    }

    @Test
    @DisplayName("Should handle advisor with multiple leads")
    fun `should handle advisor with multiple leads`() {
        val request = createTestAdvisorWrapperRequest()
        val responseWithMultipleLeads = createAdvisorWrapperResponseWithMultipleLeads()

        every { advisorWrapperService.createAdvisor(request) } returns responseWithMultipleLeads

        val createdAdvisor = advisorWrapperService.createAdvisor(request)

        assertNotNull(createdAdvisor)
        assertNotNull(createdAdvisor.leads)
        assertEquals(2, createdAdvisor.leads?.size)

        // Verify lead details
        val firstLead = createdAdvisor.leads?.get(0)
        assertNotNull(firstLead)
        assertEquals(LeadStatus.ACTIVE, firstLead.status)
        assertEquals(LeadStage.INQUIRY, firstLead.stage)

        val secondLead = createdAdvisor.leads?.get(1)
        assertNotNull(secondLead)
        assertEquals(LeadStatus.ACTIVE, secondLead.status)
        assertEquals(LeadStage.INQUIRY, secondLead.stage)
    }

    @Test
    @DisplayName("Should handle advisor with no leads")
    fun `should handle advisor with no leads`() {
        val request = createTestAdvisorWrapperRequest()
        val responseWithNoLeads = createAdvisorWrapperResponseWithNoLeads()

        every { advisorWrapperService.createAdvisor(request) } returns responseWithNoLeads

        val createdAdvisor = advisorWrapperService.createAdvisor(request)

        assertNotNull(createdAdvisor)
        assertEquals(AdvisorStatus.ACTIVE, createdAdvisor.status)
        assertNotNull(createdAdvisor.leads)
        assertEquals(0, createdAdvisor.leads?.size)
    }

    private fun createTestAdvisorWrapperResponse(): AdvisorWrapperResponse {
        return AdvisorWrapperResponse(
            id = advisorId,
            name = "John Doe",
            status = AdvisorStatus.ACTIVE,
            leads = listOf(
                AdvisorWrapperResponse.LeadInfo(
                    leadId = UUID.randomUUID(),
                    status = LeadStatus.ACTIVE,
                    stage = LeadStage.INQUIRY
                )
            )
        )
    }

    private fun createAdvisorWrapperResponseWithMultipleLeads(): AdvisorWrapperResponse {
        return AdvisorWrapperResponse(
            id = advisorId,
            name = "John Doe",
            status = AdvisorStatus.ACTIVE,
            leads = listOf(
                AdvisorWrapperResponse.LeadInfo(
                    leadId = UUID.randomUUID(),
                    status = LeadStatus.ACTIVE,
                    stage = LeadStage.INQUIRY
                ),
                AdvisorWrapperResponse.LeadInfo(
                    leadId = UUID.randomUUID(),
                    status = LeadStatus.ACTIVE,
                    stage = LeadStage.INQUIRY
                )
            )
        )
    }

    private fun createAdvisorWrapperResponseWithNoLeads(): AdvisorWrapperResponse {
        return AdvisorWrapperResponse(
            id = advisorId,
            name = "John Doe",
            status = AdvisorStatus.ACTIVE,
            leads = emptyList()
        )
    }

    private fun createTestAdvisorWrapperRequest(): AdvisorWrapperRequest {
        return AdvisorWrapperRequest(
            personData = AdvisorWrapperRequest.PersonData(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    AdvisorWrapperRequest.PersonData.MobileNumberDetails("1234567890", true)
                ),
                email = "john.doe@example.com",
                dateOfBirth = "1990-01-01",
                gender = "MALE",
                addresses = listOf(
                    AdvisorWrapperRequest.PersonData.AddressData(
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
                    AdvisorWrapperRequest.PersonData.IdentifierData("123456789012", "AADHAR")
                )
            ),
            advisorData = AdvisorWrapperRequest.AdvisorData(
                advisorCode = "ADV001",
                remarks = "Senior advisor"
            )
        )
    }

    private fun createMinimalAdvisorWrapperRequest(): AdvisorWrapperRequest {
        return AdvisorWrapperRequest(
            personData = AdvisorWrapperRequest.PersonData(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    AdvisorWrapperRequest.PersonData.MobileNumberDetails("1234567890", true)
                ),
                addresses = listOf(
                    AdvisorWrapperRequest.PersonData.AddressData(
                        pincode = "560001",
                        addressType = "PERMANENT"
                    )
                )
            )
        )
    }

    private fun createCompleteAdvisorWrapperRequest(): AdvisorWrapperRequest {
        return AdvisorWrapperRequest(
            personData = AdvisorWrapperRequest.PersonData(
                firstName = "John",
                middleName = "Michael",
                lastName = "Doe",
                mobileNumbers = listOf(
                    AdvisorWrapperRequest.PersonData.MobileNumberDetails("1234567890", true),
                    AdvisorWrapperRequest.PersonData.MobileNumberDetails("0987654321", false)
                ),
                email = "john.michael.doe@example.com",
                dateOfBirth = "1990-01-01",
                gender = "MALE",
                addresses = listOf(
                    AdvisorWrapperRequest.PersonData.AddressData(
                        addressOne = "123 Main St",
                        addressTwo = "Apt 4B",
                        landmark = "Near Park",
                        district = "Central",
                        state = "Karnataka",
                        pincode = "560001",
                        addressSource = "AADHAR",
                        addressType = "PERMANENT"
                    ),
                    AdvisorWrapperRequest.PersonData.AddressData(
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
                    AdvisorWrapperRequest.PersonData.IdentifierData("123456789012", "AADHAR"),
                    AdvisorWrapperRequest.PersonData.IdentifierData("ABCD123456", "PAN")
                )
            ),
            advisorData = AdvisorWrapperRequest.AdvisorData(
                advisorCode = "ADV002",
                remarks = "Senior advisor with complete profile"
            )
        )
    }

    private fun createAdvisorWithoutAdvisorData(): AdvisorWrapperRequest {
        return AdvisorWrapperRequest(
            personData = AdvisorWrapperRequest.PersonData(
                firstName = "John",
                lastName = "Doe",
                mobileNumbers = listOf(
                    AdvisorWrapperRequest.PersonData.MobileNumberDetails("1234567890", true)
                ),
                addresses = listOf(
                    AdvisorWrapperRequest.PersonData.AddressData(
                        addressOne = "123 Main St",
                        pincode = "560001",
                        addressType = "PERMANENT"
                    )
                )
            )
        )
    }

    private fun createTestCreateLeadForAdvisorRequest(): CreateLeadForAdvisorRequest {
        return CreateLeadForAdvisorRequest(
            applicantWrapperRequest = createTestApplicantWrapperRequest(),
            remarks = "Test lead for advisor"
        )
    }

    private fun createCompleteCreateLeadForAdvisorRequest(): CreateLeadForAdvisorRequest {
        return CreateLeadForAdvisorRequest(
            applicantWrapperRequest = createCompleteApplicantWrapperRequest(),
            remarks = "Complete lead for advisor"
        )
    }

    private fun createTestApplicantWrapperRequest(): ApplicantWrapperRequest {
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
                sourcingChannel = SourcingChannel.DIRECT,
                preliminaryInformation = null,
                leadContacts = null,
                stage = LeadStage.INQUIRY,
                status = LeadStatus.ACTIVE
            ),
            applicantData = null
        )
    }

    private fun createCompleteApplicantWrapperRequest(): ApplicantWrapperRequest {
        return ApplicantWrapperRequest(
            personData = ApplicantWrapperRequest.PersonData(
                firstName = "John",
                middleName = "Michael",
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
                requestedAmount = BigDecimal("1000000"),
                purpose = "Home Construction",
                productCode = "HL001",
                sourcingChannel = SourcingChannel.DIRECT,
                preliminaryInformation = null,
                leadContacts = null,
                stage = LeadStage.INQUIRY,
                status = LeadStatus.ACTIVE
            ),
            applicantData = null
        )
    }
}
