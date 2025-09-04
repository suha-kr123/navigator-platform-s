package com.nivasafinance.features.lead.controller

import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import com.nivasafinance.features.lead.service.LeadService
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

@DisplayName("LeadController Tests")
class LeadControllerTest {

    private val leadService = mockk<LeadService>()
    private lateinit var leadController: LeadController

    private val leadId = UUID.randomUUID()
    private val expectedResponse = createTestLeadResponse()

    @BeforeEach
    fun setup() {
        leadController = LeadController(leadService)
    }

    @Test
    @DisplayName("Should create lead successfully")
    fun `createLead should create lead successfully`() {
        val request = createTestLeadCreateRequest()
        val expectedResponse = createTestLeadResponse()

        every { leadService.createLead(request) } returns expectedResponse

        val result = leadController.createLead(request)

        assertEquals(expectedResponse, result.body)
        verify { leadService.createLead(request) }
    }

    @Test
    @DisplayName("should update lead successfully")
    fun `updateLead should update lead successfully`() {
        val updateRequest = LeadUpdateRequest(
            requestedAmount = BigDecimal("600000"),
            purpose = "Home Renovation",
            productCode = "HL002"
        )
        val updatedResponse = expectedResponse.copy(
            requestedAmount = BigDecimal("600000"),
            purpose = "Home Renovation",
            productCode = "HL002"
        )
        every { leadService.updateLead(leadId, updateRequest) } returns updatedResponse

        val result = leadController.updateLead(leadId, updateRequest)

        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(updatedResponse.id, result.body?.id)
        assertEquals(updatedResponse.requestedAmount, result.body?.requestedAmount)
        assertEquals(updatedResponse.purpose, result.body?.purpose)

        verify { leadService.updateLead(leadId, updateRequest) }
    }

    @Test
    @DisplayName("should get lead successfully")
    fun `getLeadById should get lead successfully`() {
        every { leadService.getLeadById(leadId) } returns expectedResponse

        val result = leadController.getLeadById(leadId)

        assertNotNull(result)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(expectedResponse.id, result.body?.id)
        assertEquals(expectedResponse.stage, result.body?.stage)
        assertEquals(expectedResponse.status, result.body?.status)

        verify { leadService.getLeadById(leadId) }
    }

    @Test
    @DisplayName("Should handle lead not found exception")
    fun `getLeadById should handle lead not found exception`() {
        val leadId = UUID.randomUUID()

        every { leadService.getLeadById(leadId) } throws RuntimeException("Lead not found")

        try {
            leadController.getLeadById(leadId)
        } catch (e: RuntimeException) {
            assertEquals("Lead not found", e.message)
        }

        verify { leadService.getLeadById(leadId) }
    }

    private fun createTestLeadResponse(): LeadResponse {
        return LeadResponse(
            id = leadId,
            requestedAmount = BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            status = LeadStatus.ACTIVE,
            stage = LeadStage.INQUIRY,
            preliminaryInformation = null,
            leadContacts = null,
            sourcingChannel = SourcingChannel.DIRECT,
            extData = null
        )
    }

    private fun createTestLeadCreateRequest(): LeadCreateRequest {
        return LeadCreateRequest(
            requestedAmount = BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            preliminaryInformation = null,
            leadContacts = null,
            sourcingChannel = SourcingChannel.DIRECT,
            extData = null
        )
    }
}
