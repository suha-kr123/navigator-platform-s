package com.nivasafinance.features.advisorleadmapping.controller

import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingUpdateRequest
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorLeadMappingController Tests")
class AdvisorLeadMappingControllerTest {

    private val advisorLeadMappingService = mockk<AdvisorLeadMappingService>()
    private lateinit var advisorLeadMappingController: AdvisorLeadMappingController

    private val advisorId = UUID.randomUUID()
    private val leadId = UUID.randomUUID()
    private val mappingId = UUID.randomUUID()
    private val paymentId = UUID.randomUUID()

    private val expectedMappingResponse = AdvisorLeadMappingResponse(
        id = mappingId,
        advisorId = advisorId,
        leadId = leadId,
        remarks = "Test mapping",
        extData = null
    )

    private val expectedPaymentResponse = PaymentResponse(
        id = paymentId,
        paymentStatus = "COMPLETED",
        amountPaid = BigDecimal("1000.00"),
        paidAt = LocalDateTime.now(),
        paymentMethod = "BANK_TRANSFER",
        transactionId = "TXN123456",
        remarks = "Test payment",
        extData = null
    )

    @BeforeEach
    fun setup() {
        advisorLeadMappingController = AdvisorLeadMappingController(advisorLeadMappingService)
    }

    @Test
    @DisplayName("should create advisor lead mapping successfully")
    fun `createAdvisorLeadMapping should create mapping successfully`() {
        val createRequest = AdvisorLeadMappingCreateRequest(
            remarks = "Test mapping",
            extData = null
        )
        every {
            advisorLeadMappingService.createAdvisorLeadMapping(advisorId, leadId, createRequest)
        } returns expectedMappingResponse

        val result = advisorLeadMappingController.createAdvisorLeadMapping(advisorId, leadId, createRequest)

        assertNotNull(result)
        assertEquals(201, result.statusCode.value())
        assertEquals(expectedMappingResponse.id, result.body?.id)
        assertEquals(expectedMappingResponse.advisorId, result.body?.advisorId)
        assertEquals(expectedMappingResponse.leadId, result.body?.leadId)

        verify(exactly = 1) { advisorLeadMappingService.createAdvisorLeadMapping(advisorId, leadId, createRequest) }
    }

    @Test
    @DisplayName("should get all leads for advisor successfully")
    fun `getAllLeadsForAdvisor should return leads list`() {
        val expectedMappings = listOf(expectedMappingResponse)
        every { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) } returns expectedMappings

        val result = advisorLeadMappingController.getAllLeadsForAdvisor(advisorId)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedMappings.size, result.body?.size)
        assertEquals(expectedMappingResponse.id, result.body?.first()?.id)

        verify(exactly = 1) { advisorLeadMappingService.getAllLeadsForAdvisor(advisorId) }
    }

    @Test
    @DisplayName("should get all leads for advisor by mobile successfully")
    fun `getAllLeadsForAdvisorByMobile should return leads list`() {
        val mobileNumber = "1234567890"
        val expectedMappings = listOf(expectedMappingResponse)
        every { advisorLeadMappingService.getAllLeadsForAdvisorByMobile(mobileNumber) } returns expectedMappings

        val result = advisorLeadMappingController.getAllLeadsForAdvisorByMobile(mobileNumber)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedMappings.size, result.body?.size)
        assertEquals(expectedMappingResponse.id, result.body?.first()?.id)

        verify(exactly = 1) { advisorLeadMappingService.getAllLeadsForAdvisorByMobile(mobileNumber) }
    }

    @Test
    @DisplayName("should update advisor lead mapping successfully")
    fun `updateAdvisorLeadMapping should update mapping successfully`() {
        val updateRequest = AdvisorLeadMappingUpdateRequest(
            remarks = "Updated mapping",
            extData = null
        )
        every {
            advisorLeadMappingService.updateAdvisorLeadMapping(advisorId, leadId, updateRequest)
        } returns expectedMappingResponse

        val result = advisorLeadMappingController.updateAdvisorLeadMapping(advisorId, leadId, updateRequest)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedMappingResponse.id, result.body?.id)

        verify(exactly = 1) { advisorLeadMappingService.updateAdvisorLeadMapping(advisorId, leadId, updateRequest) }
    }

    @Test
    @DisplayName("should delete advisor lead mapping successfully")
    fun `deleteAdvisorLeadMapping should delete mapping successfully`() {
        every { advisorLeadMappingService.deleteAdvisorLeadMapping(advisorId, leadId) } returns Unit

        val result = advisorLeadMappingController.deleteAdvisorLeadMapping(advisorId, leadId)

        assertNotNull(result)
        assertEquals(204, result.statusCode.value())

        verify(exactly = 1) { advisorLeadMappingService.deleteAdvisorLeadMapping(advisorId, leadId) }
    }

    @Test
    @DisplayName("should create payment for lead successfully")
    fun `createPaymentForLead should create payment successfully`() {
        val paymentRequest = PaymentCreateRequest(
            paymentStatus = "COMPLETED",
            amountPaid = BigDecimal("1000.00"),
            paidAt = LocalDateTime.now(),
            paymentMethod = "BANK_TRANSFER",
            transactionId = "TXN123456",
            remarks = "Test payment",
            extData = null
        )
        every {
            advisorLeadMappingService.createPaymentForLead(advisorId, leadId, paymentRequest)
        } returns expectedPaymentResponse

        val result = advisorLeadMappingController.createPaymentForLead(advisorId, leadId, paymentRequest)

        assertNotNull(result)
        assertEquals(201, result.statusCode.value())
        assertEquals(expectedPaymentResponse.id, result.body?.id)
        assertEquals(expectedPaymentResponse.paymentStatus, result.body?.paymentStatus)
        assertEquals(expectedPaymentResponse.amountPaid, result.body?.amountPaid)

        verify(exactly = 1) { advisorLeadMappingService.createPaymentForLead(advisorId, leadId, paymentRequest) }
    }

    @Test
    @DisplayName("should get payment for lead successfully")
    fun `getPaymentForLead should return payment`() {
        every { advisorLeadMappingService.getPaymentForLead(advisorId, leadId) } returns expectedPaymentResponse

        val result = advisorLeadMappingController.getPaymentForLead(advisorId, leadId)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedPaymentResponse.id, result.body?.id)
        assertEquals(expectedPaymentResponse.paymentStatus, result.body?.paymentStatus)

        verify(exactly = 1) { advisorLeadMappingService.getPaymentForLead(advisorId, leadId) }
    }

    @Test
    @DisplayName("should update payment for lead successfully")
    fun `updatePaymentForLead should update payment successfully`() {
        val paymentUpdateRequest = PaymentUpdateRequest(
            paymentStatus = "COMPLETED",
            amountPaid = BigDecimal("1500.00"),
            paidAt = LocalDateTime.now(),
            paymentMethod = "CARD",
            transactionId = "TXN789012",
            remarks = "Updated payment",
            extData = null
        )
        every {
            advisorLeadMappingService.updatePaymentForLead(advisorId, leadId, paymentUpdateRequest)
        } returns expectedPaymentResponse

        val result = advisorLeadMappingController.updatePaymentForLead(advisorId, leadId, paymentUpdateRequest)

        assertNotNull(result)
        assertEquals(200, result.statusCode.value())
        assertEquals(expectedPaymentResponse.id, result.body?.id)

        verify(exactly = 1) { advisorLeadMappingService.updatePaymentForLead(advisorId, leadId, paymentUpdateRequest) }
    }

    @Test
    @DisplayName("should delete payment for lead successfully")
    fun `deletePaymentForLead should delete payment successfully`() {
        every { advisorLeadMappingService.deletePaymentForLead(advisorId, leadId) } returns Unit

        val result = advisorLeadMappingController.deletePaymentForLead(advisorId, leadId)

        assertNotNull(result)
        assertEquals(204, result.statusCode.value())

        verify(exactly = 1) { advisorLeadMappingService.deletePaymentForLead(advisorId, leadId) }
    }
}
