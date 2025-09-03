package com.nivasafinance.features.wrapper.service.impl

import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.service.AdvisorService
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.person.dto.PersonResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorWrapperReadServiceImpl Tests")
class AdvisorWrapperReadServiceImplTest {

    private val advisorService = mockk<AdvisorService>()
    private val advisorLeadMappingService = mockk<AdvisorLeadMappingService>()
    private val leadService = mockk<LeadService>()

    private lateinit var advisorWrapperReadService: AdvisorWrapperReadServiceImpl

    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()
    private val leadId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        advisorWrapperReadService = AdvisorWrapperReadServiceImpl(
            advisorService,
            advisorLeadMappingService,
            leadService
        )
    }

    @Test
    @DisplayName("Should get advisor successfully")
    fun `getAdvisor should get advisor successfully`() {
        val advisorResponse = createTestAdvisorResponse()
        val leadMappings = listOf(createTestAdvisorLeadMappingResponse(leadId))
        val leadResponse = createTestLeadResponse()

        every { advisorService.getAdvisor(advisorId) } returns advisorResponse
        every { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) } returns leadMappings
        every { leadService.getLeadById(leadId) } returns leadResponse

        val result = advisorWrapperReadService.getAdvisor(advisorId)

        assertNotNull(result)
        assertEquals(advisorId, result.id)
        assertEquals("John Doe", result.name)
        assertEquals(AdvisorStatus.ACTIVE, result.status)
        assertNotNull(result.leads)
        assertEquals(1, result.leads.size)
        assertEquals(leadMappings[0].leadId, result.leads[0].leadId)
        assertEquals(LeadStatus.ACTIVE, result.leads[0].status)
        assertEquals(LeadStage.INQUIRY, result.leads[0].stage)

        verify(exactly = 1) { advisorService.getAdvisor(advisorId) }
        verify(exactly = 1) { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) }
        verify(exactly = 1) { leadService.getLeadById(leadId) }
    }

    @Test
    @DisplayName("Should get advisor with no leads")
    fun `getAdvisor should get advisor with no leads`() {
        val advisorResponse = createTestAdvisorResponse()

        every { advisorService.getAdvisor(advisorId) } returns advisorResponse
        every { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) } returns emptyList()

        val result = advisorWrapperReadService.getAdvisor(advisorId)

        assertNotNull(result)
        assertEquals(advisorId, result.id)
        assertEquals("John Doe", result.name)
        assertEquals(AdvisorStatus.ACTIVE, result.status)
        assertNotNull(result.leads)
        assertEquals(0, result.leads.size)

        verify(exactly = 1) { advisorService.getAdvisor(advisorId) }
        verify(exactly = 1) { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) }
    }

    @Test
    @DisplayName("Should get advisor with multiple leads")
    fun `getAdvisor should get advisor with multiple leads`() {
        val advisorResponse = createTestAdvisorResponse()
        val secondLeadId = UUID.randomUUID()
        val leadMappings = listOf(
            createTestAdvisorLeadMappingResponse(leadId),
            createTestAdvisorLeadMappingResponse(leadId = secondLeadId)
        )
        val leadResponse1 = createTestLeadResponse()
        val leadResponse2 = createTestLeadResponse().copy(id = secondLeadId)

        every { advisorService.getAdvisor(advisorId) } returns advisorResponse
        every { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) } returns leadMappings
        every { leadService.getLeadById(leadId) } returns leadResponse1
        every { leadService.getLeadById(secondLeadId) } returns leadResponse2

        val result = advisorWrapperReadService.getAdvisor(advisorId)

        assertNotNull(result)
        assertEquals(advisorId, result.id)
        assertEquals("John Doe", result.name)
        assertEquals(AdvisorStatus.ACTIVE, result.status)
        assertNotNull(result.leads)
        assertEquals(2, result.leads.size)

        verify(exactly = 1) { advisorService.getAdvisor(advisorId) }
        verify(exactly = 1) { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) }
        verify(exactly = 1) { leadService.getLeadById(leadId) }
        verify(exactly = 1) { leadService.getLeadById(secondLeadId) }
    }

    @Test
    @DisplayName("Should handle advisor service exception")
    fun `getAdvisor should handle advisor service exception`() {
        every {
            advisorService.getAdvisor(advisorId)
        } throws RuntimeException("Advisor not found")

        try {
            advisorWrapperReadService.getAdvisor(advisorId)
        } catch (e: RuntimeException) {
            assertEquals("Advisor not found", e.message)
        }

        verify(exactly = 1) { advisorService.getAdvisor(advisorId) }
        verify(exactly = 0) { advisorLeadMappingService.getAllLeadsForAdvisor(any()) }
    }

    @Test
    @DisplayName("Should handle advisor lead mapping service exception")
    fun `getAdvisor should handle advisor lead mapping service exception`() {
        val advisorResponse = createTestAdvisorResponse()

        every { advisorService.getAdvisor(advisorId) } returns advisorResponse
        every {
            advisorLeadMappingService.getAllLeadsForAdvisor(advisorId)
        } throws RuntimeException("Lead mapping failed")

        try {
            advisorWrapperReadService.getAdvisor(advisorId)
        } catch (e: RuntimeException) {
            assertEquals("Lead mapping failed", e.message)
        }

        verify(exactly = 1) { advisorService.getAdvisor(advisorId) }
        verify(exactly = 1) { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) }
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

    private fun createTestAdvisorLeadMappingResponse(leadId: UUID = UUID.randomUUID()): AdvisorLeadMappingResponse {
        return AdvisorLeadMappingResponse(
            id = UUID.randomUUID(),
            advisorId = advisorId,
            leadId = leadId,
            remarks = "Test mapping",
            extData = null,
            payment = null
        )
    }

    private fun createTestLeadResponse(): com.nivasafinance.features.lead.dto.LeadResponse {
        return com.nivasafinance.features.lead.dto.LeadResponse(
            id = leadId,
            requestedAmount = java.math.BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            sourcingChannel = SourcingChannel.DIRECT,
            status = LeadStatus.ACTIVE,
            stage = LeadStage.INQUIRY,
            preliminaryInformation = null,
            leadContacts = null
        )
    }
}
