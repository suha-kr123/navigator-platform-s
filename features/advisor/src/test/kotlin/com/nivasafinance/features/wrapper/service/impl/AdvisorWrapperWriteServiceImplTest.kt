package com.nivasafinance.features.wrapper.service.impl

import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.service.AdvisorService
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.service.PersonService
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperRequest
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.CreateLeadForAdvisorRequest
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

@DisplayName("AdvisorWrapperWriteServiceImpl Tests")
class AdvisorWrapperWriteServiceImplTest {

    private val personService = mockk<PersonService>()
    private val advisorService = mockk<AdvisorService>()
    private val advisorLeadMappingService = mockk<AdvisorLeadMappingService>()
    private val leadService = mockk<LeadService>()

    private lateinit var advisorWrapperWriteService: AdvisorWrapperWriteServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val leadId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        advisorWrapperWriteService = AdvisorWrapperWriteServiceImpl(
            personService,
            advisorService,
            advisorLeadMappingService,
            leadService
        )
    }

    @Test
    @DisplayName("Should create advisor successfully")
    fun `createAdvisor should create advisor successfully`() {
        val request = createTestAdvisorWrapperRequest()
        val personResponse = createTestPersonResponse()
        val advisorResponse = createTestAdvisorResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { advisorService.createAdvisor(any<AdvisorCreateRequest>()) } returns advisorResponse

        val result = advisorWrapperWriteService.createAdvisor(request)

        assertNotNull(result)
        assertEquals(advisorId, result.id)
        assertEquals("John Doe", result.name)
        assertEquals(AdvisorStatus.ACTIVE, result.status)
        assertNotNull(result.leads)
        assertEquals(0, result.leads.size)

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { advisorService.createAdvisor(any<AdvisorCreateRequest>()) }
    }

    @Test
    @DisplayName("Should create advisor with minimal data")
    fun `createAdvisor should create advisor with minimal data`() {
        val request = createMinimalAdvisorWrapperRequest()
        val personResponse = createTestPersonResponse()
        val advisorResponse = createTestAdvisorResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { advisorService.createAdvisor(any<AdvisorCreateRequest>()) } returns advisorResponse

        val result = advisorWrapperWriteService.createAdvisor(request)

        assertNotNull(result)
        assertEquals(advisorId, result.id)
        assertEquals("John Doe", result.name)
        assertEquals(AdvisorStatus.ACTIVE, result.status)

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { advisorService.createAdvisor(any<AdvisorCreateRequest>()) }
    }

    @Test
    @DisplayName("Should create advisor with complete data")
    fun `createAdvisor should create advisor with complete data`() {
        val request = createCompleteAdvisorWrapperRequest()
        val personResponse = createTestPersonResponse()
        val advisorResponse = createTestAdvisorResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every { advisorService.createAdvisor(any<AdvisorCreateRequest>()) } returns advisorResponse

        val result = advisorWrapperWriteService.createAdvisor(request)

        assertNotNull(result)
        assertEquals(advisorId, result.id)
        assertEquals("John Doe", result.name)
        assertEquals(AdvisorStatus.ACTIVE, result.status)

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { advisorService.createAdvisor(any<AdvisorCreateRequest>()) }
    }

    @Test
    @DisplayName("Should handle person service exception")
    fun `createAdvisor should handle person service exception`() {
        val request = createTestAdvisorWrapperRequest()

        every {
            personService.createPerson(any<PersonCreateRequest>())
        } throws RuntimeException("Person creation failed")

        try {
            advisorWrapperWriteService.createAdvisor(request)
        } catch (e: RuntimeException) {
            assertEquals("Person creation failed", e.message)
        }

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 0) { advisorService.createAdvisor(any<AdvisorCreateRequest>()) }
    }

    @Test
    @DisplayName("Should handle advisor service exception")
    fun `createAdvisor should handle advisor service exception`() {
        val request = createTestAdvisorWrapperRequest()
        val personResponse = createTestPersonResponse()

        every { personService.createPerson(any<PersonCreateRequest>()) } returns personResponse
        every {
            advisorService.createAdvisor(any<AdvisorCreateRequest>())
        } throws RuntimeException("Advisor creation failed")

        try {
            advisorWrapperWriteService.createAdvisor(request)
        } catch (e: RuntimeException) {
            assertEquals("Advisor creation failed", e.message)
        }

        verify(exactly = 1) { personService.createPerson(any<PersonCreateRequest>()) }
        verify(exactly = 1) { advisorService.createAdvisor(any<AdvisorCreateRequest>()) }
    }

    @Test
    @DisplayName("Should create lead for advisor successfully")
    fun `createLeadForAdvisor should create lead for advisor successfully`() {
        val request = createTestCreateLeadForAdvisorRequest()
        val advisorResponse = createTestAdvisorResponse()
        val leadResponse = createTestLeadResponse()
        val mappingResponse = createTestAdvisorLeadMappingResponse()
        val leadMappings = listOf(mappingResponse)

        every { advisorService.getAdvisor(advisorId) } returns advisorResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every { leadService.getLeadById(leadId) } returns leadResponse
        every { advisorLeadMappingService.createAdvisorLeadMapping(any(), any(), any()) } returns mappingResponse
        every { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) } returns leadMappings

        val result = advisorWrapperWriteService.createLeadForAdvisor(advisorId, request)

        assertNotNull(result)
        assertEquals(advisorId, result.id)
        assertEquals("John Doe", result.name)
        assertEquals(AdvisorStatus.ACTIVE, result.status)
        assertNotNull(result.leads)
        assertEquals(1, result.leads.size)
        assertEquals(leadId, result.leads[0].leadId)
        assertEquals(LeadStatus.ACTIVE, result.leads[0].status)
        assertEquals(LeadStage.INQUIRY, result.leads[0].stage)

        verify(exactly = 1) { advisorService.getAdvisor(advisorId) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { leadService.getLeadById(leadId) }
        verify(exactly = 1) { advisorLeadMappingService.createAdvisorLeadMapping(any(), any(), any()) }
        verify(exactly = 1) { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) }
    }

    @Test
    @DisplayName("Should handle advisor not found exception")
    fun `createLeadForAdvisor should handle advisor not found exception`() {
        val request = createTestCreateLeadForAdvisorRequest()
        val advisorResponse = createTestAdvisorResponse()
        val leadResponse = createTestLeadResponse()

        every { advisorService.getAdvisor(advisorId) } returns advisorResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every {
            advisorLeadMappingService.createAdvisorLeadMapping(any(), any(), any())
        } throws RuntimeException("Advisor not found")

        try {
            advisorWrapperWriteService.createLeadForAdvisor(advisorId, request)
        } catch (e: RuntimeException) {
            assertEquals("Advisor not found", e.message)
        }

        verify(exactly = 0) { advisorService.getAdvisor(any()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { advisorLeadMappingService.createAdvisorLeadMapping(any(), any(), any()) }
        verify(exactly = 0) { advisorLeadMappingService.getAllLeadsForAdvisor(any()) }
    }

    @Test
    @DisplayName("Should handle lead service exception")
    fun `createLeadForAdvisor should handle lead service exception`() {
        val request = createTestCreateLeadForAdvisorRequest()
        val advisorResponse = createTestAdvisorResponse()

        every { advisorService.getAdvisor(advisorId) } returns advisorResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } throws RuntimeException("Lead creation failed")

        try {
            advisorWrapperWriteService.createLeadForAdvisor(advisorId, request)
        } catch (e: RuntimeException) {
            assertEquals("Lead creation failed", e.message)
        }

        verify(exactly = 0) { advisorService.getAdvisor(any()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 0) { advisorLeadMappingService.createAdvisorLeadMapping(any(), any(), any()) }
        verify(exactly = 0) { advisorLeadMappingService.getAllLeadsForAdvisor(any()) }
    }

    @Test
    @DisplayName("Should handle advisor lead mapping service exception")
    fun `createLeadForAdvisor should handle advisor lead mapping service exception`() {
        val request = createTestCreateLeadForAdvisorRequest()
        val advisorResponse = createTestAdvisorResponse()
        val leadResponse = createTestLeadResponse()

        every { advisorService.getAdvisor(advisorId) } returns advisorResponse
        every { leadService.createLead(any<LeadCreateRequest>()) } returns leadResponse
        every {
            advisorLeadMappingService.createAdvisorLeadMapping(any(), any(), any())
        } throws RuntimeException("Mapping creation failed")

        try {
            advisorWrapperWriteService.createLeadForAdvisor(advisorId, request)
        } catch (e: RuntimeException) {
            assertEquals("Mapping creation failed", e.message)
        }

        verify(exactly = 0) { advisorService.getAdvisor(any()) }
        verify(exactly = 1) { leadService.createLead(any<LeadCreateRequest>()) }
        verify(exactly = 1) { advisorLeadMappingService.createAdvisorLeadMapping(any(), any(), any()) }
        verify(exactly = 0) { advisorLeadMappingService.getAllLeadsForAdvisor(any()) }
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
                remarks = "Test advisor"
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
                advisorCode = "ADV001",
                remarks = "Complete test advisor"
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
                sourcingChannel = SourcingChannel.DIRECT,
                preliminaryInformation = null,
                leadContacts = null,
                stage = LeadStage.INQUIRY,
                status = LeadStatus.ACTIVE
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

    private fun createTestAdvisorResponse(): AdvisorResponse {
        return AdvisorResponse(
            id = advisorId,
            personId = personId,
            advisorCode = "ADV001",
            personalDetails = PersonResponse(
                id = personId,
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                dateOfBirth = java.time.LocalDate.of(1990, 1, 1),
                gender = data.enums.Gender.MALE
            ),
            status = AdvisorStatus.ACTIVE,
            isEmployee = false,
            remarks = "Test advisor"
        )
    }

    private fun createTestLeadResponse(): LeadResponse {
        return LeadResponse(
            id = leadId,
            requestedAmount = BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            sourcingChannel = SourcingChannel.DIRECT,
            status = LeadStatus.ACTIVE,
            stage = LeadStage.INQUIRY,
            preliminaryInformation = null,
            leadContacts = null,
            extData = null
        )
    }

    private fun createTestAdvisorLeadMappingResponse(): AdvisorLeadMappingResponse {
        return AdvisorLeadMappingResponse(
            id = UUID.randomUUID(),
            advisorId = advisorId,
            leadId = leadId,
            remarks = "Test mapping",
            extData = null,
            payment = null
        )
    }
}
