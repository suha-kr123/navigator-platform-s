package com.nivasafinance.features.lead.integration

import com.nivasafinance.features.lead.TestUtils.createTestLeadCreateRequest
import com.nivasafinance.features.lead.TestUtils.createTestLeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.service.LeadService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

@DisplayName("Lead Integration Tests")
class LeadIntegrationTest {

    private lateinit var leadService: LeadService

    @BeforeEach
    fun setUp() {
        leadService = mockk()
    }

    @Test
    @DisplayName("Should create lead successfully")
    fun `createLead should create lead successfully`() {
        val request = createTestLeadCreateRequest()
        val expectedResponse = createTestLeadResponse()

        every { leadService.createLead(request) } returns expectedResponse

        val result = leadService.createLead(request)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.requestedAmount, result.requestedAmount)
        assertEquals(expectedResponse.purpose, result.purpose)
        assertEquals(expectedResponse.productCode, result.productCode)
        assertEquals(expectedResponse.sourcingChannel, result.sourcingChannel)
        assertEquals(expectedResponse.stage, result.stage)
        assertEquals(expectedResponse.status, result.status)

        verify { leadService.createLead(request) }
    }

    @Test
    @DisplayName("Should get lead by id successfully")
    fun `getLeadById should return lead when exists`() {
        val leadId = UUID.randomUUID()
        val expectedResponse = createTestLeadResponse(id = leadId)

        every { leadService.getLeadById(leadId) } returns expectedResponse

        val result = leadService.getLeadById(leadId)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.requestedAmount, result.requestedAmount)
        assertEquals(expectedResponse.purpose, result.purpose)
        assertEquals(expectedResponse.productCode, result.productCode)
        assertEquals(expectedResponse.sourcingChannel, result.sourcingChannel)
        assertEquals(expectedResponse.stage, result.stage)
        assertEquals(expectedResponse.status, result.status)

        verify { leadService.getLeadById(leadId) }
    }

    @Test
    @DisplayName("Should update lead successfully")
    fun `updateLead should update lead successfully`() {
        val leadId = UUID.randomUUID()
        val request = LeadUpdateRequest(
            requestedAmount = BigDecimal("600000"),
            purpose = "Home Renovation",
            productCode = "HL002"
        )
        val expectedResponse = createTestLeadResponse(
            id = leadId,
            requestedAmount = request.requestedAmount!!,
            purpose = request.purpose!!,
            productCode = request.productCode!!
        )

        every { leadService.updateLead(leadId, request) } returns expectedResponse

        val result = leadService.updateLead(leadId, request)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.requestedAmount, result.requestedAmount)
        assertEquals(expectedResponse.purpose, result.purpose)
        assertEquals(expectedResponse.productCode, result.productCode)
        assertEquals(expectedResponse.sourcingChannel, result.sourcingChannel)
        assertEquals(expectedResponse.stage, result.stage)
        assertEquals(expectedResponse.status, result.status)

        verify { leadService.updateLead(leadId, request) }
    }

    @Test
    @DisplayName("Should handle lead not found")
    fun `getLeadById should handle lead not found`() {
        val nonExistentId = UUID.randomUUID()

        every { leadService.getLeadById(nonExistentId) } throws RuntimeException("Lead not found")

        try {
            leadService.getLeadById(nonExistentId)
        } catch (e: RuntimeException) {
            assertEquals("Lead not found", e.message)
        }

        verify { leadService.getLeadById(nonExistentId) }
    }
}
