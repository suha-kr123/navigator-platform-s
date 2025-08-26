package com.nivasafinance.features.wrapper.service

import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperRequest
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.CreateLeadForAdvisorRequest
import com.nivasafinance.features.wrapper.service.impl.AdvisorWrapperServiceImpl
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

@DisplayName("AdvisorWrapperService Tests")
class AdvisorWrapperServiceTest {

    private val advisorWrapperReadService = mockk<AdvisorWrapperReadService>()
    private val advisorWrapperWriteService = mockk<AdvisorWrapperWriteService>()
    private lateinit var advisorWrapperService: AdvisorWrapperServiceImpl

    private val advisorId = UUID.randomUUID()
    private val expectedResponse = createTestAdvisorWrapperResponse()

    @BeforeEach
    fun setup() {
        advisorWrapperService = AdvisorWrapperServiceImpl(
            advisorWrapperReadService,
            advisorWrapperWriteService
        )
    }

    @Test
    @DisplayName("Should create advisor successfully")
    fun `createAdvisor should create advisor successfully`() {
        val request = createTestAdvisorWrapperRequest()

        every { advisorWrapperWriteService.createAdvisor(request) } returns expectedResponse

        val result = advisorWrapperService.createAdvisor(request)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.name, result.name)
        assertEquals(expectedResponse.status, result.status)

        verify { advisorWrapperWriteService.createAdvisor(request) }
    }

    @Test
    @DisplayName("Should get advisor successfully")
    fun `getAdvisor should get advisor successfully`() {
        every { advisorWrapperReadService.getAdvisor(advisorId) } returns expectedResponse

        val result = advisorWrapperService.getAdvisor(advisorId)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.name, result.name)
        assertEquals(expectedResponse.status, result.status)

        verify { advisorWrapperReadService.getAdvisor(advisorId) }
    }

    @Test
    @DisplayName("Should create lead for advisor successfully")
    fun `createLeadForAdvisor should create lead for advisor successfully`() {
        val request = createTestCreateLeadForAdvisorRequest()

        every { advisorWrapperWriteService.createLeadForAdvisor(advisorId, request) } returns expectedResponse

        val result = advisorWrapperService.createLeadForAdvisor(advisorId, request)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.name, result.name)
        assertEquals(expectedResponse.status, result.status)

        verify { advisorWrapperWriteService.createLeadForAdvisor(advisorId, request) }
    }

    @Test
    @DisplayName("Should create advisor with minimal data")
    fun `createAdvisor should create advisor with minimal data`() {
        val request = createMinimalAdvisorWrapperRequest()

        every { advisorWrapperWriteService.createAdvisor(request) } returns expectedResponse

        val result = advisorWrapperService.createAdvisor(request)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.name, result.name)
        assertEquals(expectedResponse.status, result.status)

        verify { advisorWrapperWriteService.createAdvisor(request) }
    }

    @Test
    @DisplayName("Should create advisor with complete data")
    fun `createAdvisor should create advisor with complete data`() {
        val request = createCompleteAdvisorWrapperRequest()

        every { advisorWrapperWriteService.createAdvisor(request) } returns expectedResponse

        val result = advisorWrapperService.createAdvisor(request)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.name, result.name)
        assertEquals(expectedResponse.status, result.status)

        verify { advisorWrapperWriteService.createAdvisor(request) }
    }

    @Test
    @DisplayName("Should handle service exception during creation")
    fun `createAdvisor should handle service exception during creation`() {
        val request = createTestAdvisorWrapperRequest()

        every { advisorWrapperWriteService.createAdvisor(request) } throws RuntimeException("Service error")

        try {
            advisorWrapperService.createAdvisor(request)
        } catch (e: RuntimeException) {
            assertEquals("Service error", e.message)
        }

        verify { advisorWrapperWriteService.createAdvisor(request) }
    }

    @Test
    @DisplayName("Should handle service exception during retrieval")
    fun `getAdvisor should handle service exception during retrieval`() {
        every { advisorWrapperReadService.getAdvisor(advisorId) } throws RuntimeException("Advisor not found")

        try {
            advisorWrapperService.getAdvisor(advisorId)
        } catch (e: RuntimeException) {
            assertEquals("Advisor not found", e.message)
        }

        verify { advisorWrapperReadService.getAdvisor(advisorId) }
    }

    @Test
    @DisplayName("Should handle service exception during lead creation")
    fun `createLeadForAdvisor should handle service exception during lead creation`() {
        val request = createTestCreateLeadForAdvisorRequest()

        every {
            advisorWrapperWriteService.createLeadForAdvisor(advisorId, request)
        } throws RuntimeException("Lead creation failed")

        try {
            advisorWrapperService.createLeadForAdvisor(advisorId, request)
        } catch (e: RuntimeException) {
            assertEquals("Lead creation failed", e.message)
        }

        verify { advisorWrapperWriteService.createLeadForAdvisor(advisorId, request) }
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

    private fun createTestCreateLeadForAdvisorRequest(): CreateLeadForAdvisorRequest {
        return CreateLeadForAdvisorRequest(
            applicantWrapperRequest = createTestApplicantWrapperRequest(),
            remarks = "Test lead for advisor"
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
}
