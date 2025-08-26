package com.nivasafinance.features.wrapper.controller

import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperRequest
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.CreateLeadForAdvisorRequest
import com.nivasafinance.features.wrapper.service.AdvisorWrapperService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorWrapperController Tests")
class AdvisorWrapperControllerTest {

    private val advisorWrapperService = mockk<AdvisorWrapperService>()
    private lateinit var advisorWrapperController: AdvisorWrapperController

    private val advisorId = UUID.randomUUID()
    private val expectedResponse = createTestAdvisorWrapperResponse()

    @BeforeEach
    fun setup() {
        advisorWrapperController = AdvisorWrapperController(advisorWrapperService)
    }

    @Test
    @DisplayName("Should create advisor successfully")
    fun `createAdvisor should create advisor successfully`() {
        val request = createTestAdvisorWrapperRequest()

        every { advisorWrapperService.createAdvisor(request) } returns expectedResponse

        val result = advisorWrapperController.createAdvisor(request)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify { advisorWrapperService.createAdvisor(request) }
    }

    @Test
    @DisplayName("Should get advisor successfully")
    fun `getAdvisor should get advisor successfully`() {
        every { advisorWrapperService.getAdvisor(advisorId) } returns expectedResponse

        val result = advisorWrapperController.getAdvisor(advisorId)

        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.name, result.body?.name)
        assertEquals(expectedResponse.status, result.body?.status)

        verify { advisorWrapperService.getAdvisor(advisorId) }
    }

    @Test
    @DisplayName("Should create lead for advisor successfully")
    fun `createLeadForAdvisor should create lead for advisor successfully`() {
        val request = createTestCreateLeadForAdvisorRequest()

        every { advisorWrapperService.createLeadForAdvisor(advisorId, request) } returns expectedResponse

        val result = advisorWrapperController.createLeadForAdvisor(advisorId, request)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify { advisorWrapperService.createLeadForAdvisor(advisorId, request) }
    }

    @Test
    @DisplayName("Should handle advisor not found exception")
    fun `getAdvisor should handle advisor not found exception`() {
        every { advisorWrapperService.getAdvisor(advisorId) } throws RuntimeException("Advisor not found")

        try {
            advisorWrapperController.getAdvisor(advisorId)
        } catch (e: RuntimeException) {
            assertEquals("Advisor not found", e.message)
        }

        verify { advisorWrapperService.getAdvisor(advisorId) }
    }

    @Test
    @DisplayName("Should handle advisor creation exception")
    fun `createAdvisor should handle advisor creation exception`() {
        val request = createTestAdvisorWrapperRequest()

        every { advisorWrapperService.createAdvisor(request) } throws RuntimeException("Creation failed")

        try {
            advisorWrapperController.createAdvisor(request)
        } catch (e: RuntimeException) {
            assertEquals("Creation failed", e.message)
        }

        verify { advisorWrapperService.createAdvisor(request) }
    }

    @Test
    @DisplayName("Should handle lead creation exception")
    fun `createLeadForAdvisor should handle lead creation exception`() {
        val request = createTestCreateLeadForAdvisorRequest()

        every {
            advisorWrapperService.createLeadForAdvisor(advisorId, request)
        } throws RuntimeException("Lead creation failed")

        try {
            advisorWrapperController.createLeadForAdvisor(advisorId, request)
        } catch (e: RuntimeException) {
            assertEquals("Lead creation failed", e.message)
        }

        verify { advisorWrapperService.createLeadForAdvisor(advisorId, request) }
    }

    @Test
    @DisplayName("Should create advisor with minimal data")
    fun `createAdvisor should create advisor with minimal data`() {
        val request = createMinimalAdvisorWrapperRequest()

        every { advisorWrapperService.createAdvisor(request) } returns expectedResponse

        val result = advisorWrapperController.createAdvisor(request)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify { advisorWrapperService.createAdvisor(request) }
    }

    @Test
    @DisplayName("Should create advisor with complete data")
    fun `createAdvisor should create advisor with complete data`() {
        val request = createCompleteAdvisorWrapperRequest()

        every { advisorWrapperService.createAdvisor(request) } returns expectedResponse

        val result = advisorWrapperController.createAdvisor(request)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify { advisorWrapperService.createAdvisor(request) }
    }

    @Test
    @DisplayName("Should create lead for advisor with complete data")
    fun `createLeadForAdvisor should create lead for advisor with complete data`() {
        val request = createCompleteCreateLeadForAdvisorRequest()

        every { advisorWrapperService.createLeadForAdvisor(advisorId, request) } returns expectedResponse

        val result = advisorWrapperController.createLeadForAdvisor(advisorId, request)

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify { advisorWrapperService.createLeadForAdvisor(advisorId, request) }
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
                sourcingChannel = "Direct",
                preliminaryInformation = null,
                leadContacts = null,
                stage = com.nivasafinance.features.lead.enum.LeadStage.INQUIRY,
                status = com.nivasafinance.features.lead.enum.LeadStatus.ACTIVE
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
                sourcingChannel = "Direct",
                preliminaryInformation = null,
                leadContacts = null,
                stage = com.nivasafinance.features.lead.enum.LeadStage.INQUIRY,
                status = com.nivasafinance.features.lead.enum.LeadStatus.ACTIVE
            ),
            applicantData = null
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
}
