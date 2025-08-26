package com.nivasafinance.features.lead.lead.service.impl

import com.nivasafinance.features.lead.TestUtils.createTestLeadCreateRequest
import com.nivasafinance.features.lead.TestUtils.createTestLeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.service.LeadReadService
import com.nivasafinance.features.lead.service.LeadWriteService
import com.nivasafinance.features.lead.service.impl.LeadServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

@DisplayName("LeadServiceImpl Tests")
class LeadServiceImplTest {

    private lateinit var leadReadService: LeadReadService
    private lateinit var leadWriteService: LeadWriteService
    private lateinit var leadService: LeadServiceImpl

    @BeforeEach
    fun setUp() {
        leadReadService = mockk()
        leadWriteService = mockk()
        leadService = LeadServiceImpl(leadReadService, leadWriteService)
    }

    @Nested
    @DisplayName("createLead")
    inner class CreateLead {

        @Test
        @DisplayName("should create lead and return response")
        fun `should create lead and return response`() {
            val request = createTestLeadCreateRequest()
            val expectedResponse = createTestLeadResponse()

            every { leadWriteService.createLead(request) } returns expectedResponse

            val result = leadService.createLead(request)

            assertEquals(expectedResponse.id, result.id)
            assertEquals(expectedResponse.requestedAmount, result.requestedAmount)
            assertEquals(expectedResponse.purpose, result.purpose)
            assertEquals(expectedResponse.productCode, result.productCode)
            assertEquals(expectedResponse.sourcingChannel, result.sourcingChannel)
            assertEquals(expectedResponse.stage, result.stage)
            assertEquals(expectedResponse.status, result.status)

            verify { leadWriteService.createLead(request) }
        }
    }

    @Nested
    @DisplayName("getLeadById")
    inner class GetLeadById {

        @Test
        @DisplayName("should return lead response when lead exists")
        fun `should return lead response when lead exists`() {
            val leadId = UUID.randomUUID()
            val expectedResponse = createTestLeadResponse(id = leadId)

            every { leadReadService.getLeadById(leadId) } returns expectedResponse

            val result = leadService.getLeadById(leadId)

            assertEquals(expectedResponse.id, result.id)
            assertEquals(expectedResponse.requestedAmount, result.requestedAmount)
            assertEquals(expectedResponse.purpose, result.purpose)
            assertEquals(expectedResponse.productCode, result.productCode)
            assertEquals(expectedResponse.sourcingChannel, result.sourcingChannel)
            assertEquals(expectedResponse.stage, result.stage)
            assertEquals(expectedResponse.status, result.status)

            verify { leadReadService.getLeadById(leadId) }
        }
    }

    @Nested
    @DisplayName("updateLead")
    inner class UpdateLead {

        @Test
        @DisplayName("should update lead and return response")
        fun `should update lead and return response`() {
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

            every { leadWriteService.updateLead(leadId, request) } returns expectedResponse

            val result = leadService.updateLead(leadId, request)

            assertEquals(expectedResponse.id, result.id)
            assertEquals(expectedResponse.requestedAmount, result.requestedAmount)
            assertEquals(expectedResponse.purpose, result.purpose)
            assertEquals(expectedResponse.productCode, result.productCode)
            assertEquals(expectedResponse.sourcingChannel, result.sourcingChannel)
            assertEquals(expectedResponse.stage, result.stage)
            assertEquals(expectedResponse.status, result.status)

            verify { leadWriteService.updateLead(leadId, request) }
        }
    }
}
