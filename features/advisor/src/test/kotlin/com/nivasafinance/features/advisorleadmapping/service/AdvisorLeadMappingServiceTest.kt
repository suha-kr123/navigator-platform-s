package com.nivasafinance.features.advisorleadmapping.service

import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingUpdateRequest
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingByAdvisorAndLeadNotFoundException
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingNoPaymentException
import com.nivasafinance.features.advisorleadmapping.service.impl.AdvisorLeadMappingReadServiceImpl
import com.nivasafinance.features.advisorleadmapping.service.impl.AdvisorLeadMappingServiceImpl
import com.nivasafinance.features.advisorleadmapping.service.impl.AdvisorLeadMappingWriteServiceImpl
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import com.nivasafinance.features.payment.service.PaymentService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorLeadMappingService Tests")
class AdvisorLeadMappingServiceTest {

    private val advisorLeadMappingReadService = mockk<AdvisorLeadMappingReadServiceImpl>()
    private val advisorLeadMappingWriteService = mockk<AdvisorLeadMappingWriteServiceImpl>()
    private val paymentService = mockk<PaymentService>()
    private val messageSource = mockk<org.springframework.context.MessageSource>()
    private lateinit var advisorLeadMappingService: AdvisorLeadMappingServiceImpl

    private val advisorId = UUID.randomUUID()
    private val leadId = UUID.randomUUID()
    private val mappingId = UUID.randomUUID()
    private val paymentId = UUID.randomUUID()

    private val expectedPaymentResponse = PaymentResponse(
        id = paymentId,
        paymentStatus = "PAID",
        amountPaid = BigDecimal("1000.00"),
        paidAt = LocalDateTime.now(),
        paymentMethod = "BANK_TRANSFER",
        transactionId = "TXN123456",
        remarks = "Test payment",
        extData = null
    )

    @BeforeEach
    fun setup() {
        advisorLeadMappingService = AdvisorLeadMappingServiceImpl(
            advisorLeadMappingReadService,
            advisorLeadMappingWriteService,
            paymentService
        )

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = advisorLeadMappingService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(advisorLeadMappingService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    @Test
    @DisplayName("should get advisor lead mapping by id successfully")
    fun `getAdvisorLeadMapping should return mapping when found`() {
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.id } returns mappingId
        every { mappingData.advisorId } returns advisorId
        every { mappingData.leadId } returns leadId
        every { mappingData.remarks } returns "Test mapping"
        every { mappingData.extData } returns null
        every { mappingData.paymentId } returns paymentId
        every { advisorLeadMappingReadService.getAdvisorLeadMappingData(mappingId) } returns mappingData
        every { paymentService.getPayment(paymentId) } returns expectedPaymentResponse

        val result = advisorLeadMappingService.getAdvisorLeadMapping(mappingId)

        assertNotNull(result)
        verify(exactly = 1) { advisorLeadMappingReadService.getAdvisorLeadMappingData(mappingId) }
    }

    @Test
    @DisplayName("should get advisor lead mapping by advisor and lead successfully")
    fun `getAdvisorLeadMappingByAdvisorAndLead should return mapping when found`() {
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.id } returns mappingId
        every { mappingData.advisorId } returns advisorId
        every { mappingData.leadId } returns leadId
        every { mappingData.remarks } returns "Test mapping"
        every { mappingData.extData } returns null
        every { mappingData.paymentId } returns paymentId
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns mappingData
        every { paymentService.getPayment(paymentId) } returns expectedPaymentResponse

        val result = advisorLeadMappingService.getAdvisorLeadMappingByAdvisorAndLead(advisorId, leadId)

        assertNotNull(result)
        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
    }

    @Test
    @DisplayName("should return null when mapping not found by advisor and lead")
    fun `getAdvisorLeadMappingByAdvisorAndLead should return null when not found`() {
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns null

        val result = advisorLeadMappingService.getAdvisorLeadMappingByAdvisorAndLead(advisorId, leadId)

        assertEquals(null, result)
        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
    }

    @Test
    @DisplayName("should get all leads for advisor successfully")
    fun `getAllLeadsForAdvisor should return leads list`() {
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.id } returns mappingId
        every { mappingData.advisorId } returns advisorId
        every { mappingData.leadId } returns leadId
        every { mappingData.remarks } returns "Test mapping"
        every { mappingData.extData } returns null
        every { mappingData.paymentId } returns paymentId
        val mappings = listOf(mappingData)
        every { advisorLeadMappingReadService.getAllLeadsForAdvisorData(advisorId) } returns mappings
        every { paymentService.getPayment(paymentId) } returns expectedPaymentResponse

        val result = advisorLeadMappingService.getAllLeadsForAdvisor(advisorId)

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(exactly = 1) { advisorLeadMappingReadService.getAllLeadsForAdvisorData(advisorId) }
    }

    @Test
    @DisplayName("should get all leads for advisor by mobile successfully")
    fun `getAllLeadsForAdvisorByMobile should return leads list`() {
        val mobileNumber = "1234567890"
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.id } returns mappingId
        every { mappingData.advisorId } returns advisorId
        every { mappingData.leadId } returns leadId
        every { mappingData.remarks } returns "Test mapping"
        every { mappingData.extData } returns null
        every { mappingData.paymentId } returns paymentId
        val mappings = listOf(mappingData)
        every {
            advisorLeadMappingReadService.getAllLeadsForAdvisorByMobileData(mobileNumber)
        } returns mappings
        every { paymentService.getPayment(paymentId) } returns expectedPaymentResponse

        val result = advisorLeadMappingService.getAllLeadsForAdvisorByMobile(mobileNumber)

        assertNotNull(result)
        assertEquals(1, result.size)
        verify(exactly = 1) { advisorLeadMappingReadService.getAllLeadsForAdvisorByMobileData(mobileNumber) }
    }

    @Test
    @DisplayName("should create advisor lead mapping successfully")
    fun `createAdvisorLeadMapping should create mapping successfully`() {
        val createRequest = AdvisorLeadMappingCreateRequest(
            remarks = "Test mapping",
            extData = null
        )
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.id } returns mappingId
        every { mappingData.advisorId } returns advisorId
        every { mappingData.leadId } returns leadId
        every { mappingData.remarks } returns "Test mapping"
        every { mappingData.extData } returns null
        every { mappingData.paymentId } returns null
        every {
            advisorLeadMappingWriteService.createAdvisorLeadMappingData(advisorId, leadId, createRequest)
        } returns mappingData

        val result = advisorLeadMappingService.createAdvisorLeadMapping(advisorId, leadId, createRequest)

        assertNotNull(result)
        verify(exactly = 1) {
            advisorLeadMappingWriteService.createAdvisorLeadMappingData(advisorId, leadId, createRequest)
        }
    }

    @Test
    @DisplayName("should update advisor lead mapping successfully")
    fun `updateAdvisorLeadMapping should update mapping successfully`() {
        val updateRequest = AdvisorLeadMappingUpdateRequest(
            remarks = "Updated mapping",
            extData = null
        )
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.id } returns mappingId
        every { mappingData.advisorId } returns advisorId
        every { mappingData.leadId } returns leadId
        every { mappingData.remarks } returns "Updated mapping"
        every { mappingData.extData } returns null
        every { mappingData.paymentId } returns null
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns mappingData
        every {
            advisorLeadMappingWriteService.updateAdvisorLeadMappingData(mappingId, updateRequest)
        } returns mappingData

        val result = advisorLeadMappingService.updateAdvisorLeadMapping(advisorId, leadId, updateRequest)

        assertNotNull(result)
        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 1) {
            advisorLeadMappingWriteService.updateAdvisorLeadMappingData(mappingId, updateRequest)
        }
    }

    @Test
    @DisplayName("should throw exception when updating non-existent mapping")
    fun `updateAdvisorLeadMapping should throw exception when mapping not found`() {
        val updateRequest = AdvisorLeadMappingUpdateRequest(
            remarks = "Updated mapping",
            extData = null
        )
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns null

        assertThrows<AdvisorLeadMappingByAdvisorAndLeadNotFoundException> {
            advisorLeadMappingService.updateAdvisorLeadMapping(advisorId, leadId, updateRequest)
        }

        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 0) {
            advisorLeadMappingWriteService.updateAdvisorLeadMappingData(any(), any())
        }
    }

    @Test
    @DisplayName("should delete advisor lead mapping successfully")
    fun `deleteAdvisorLeadMapping should delete mapping successfully`() {
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.id } returns mappingId
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns mappingData
        every { advisorLeadMappingWriteService.deleteAdvisorLeadMapping(mappingId) } returns Unit

        advisorLeadMappingService.deleteAdvisorLeadMapping(advisorId, leadId)

        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 1) { advisorLeadMappingWriteService.deleteAdvisorLeadMapping(mappingId) }
    }

    @Test
    @DisplayName("should handle delete when mapping not found")
    fun `deleteAdvisorLeadMapping should handle when mapping not found`() {
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns null

        advisorLeadMappingService.deleteAdvisorLeadMapping(advisorId, leadId)

        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 0) { advisorLeadMappingWriteService.deleteAdvisorLeadMapping(any()) }
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
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns mappingData
        every { paymentService.createPayment(paymentRequest) } returns expectedPaymentResponse

        val result = advisorLeadMappingService.createPaymentForLead(advisorId, leadId, paymentRequest)

        assertNotNull(result)
        assertEquals(expectedPaymentResponse.id, result.id)
        assertEquals(expectedPaymentResponse.paymentStatus, result.paymentStatus)
        assertEquals(expectedPaymentResponse.amountPaid, result.amountPaid)

        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 1) { paymentService.createPayment(paymentRequest) }
    }

    @Test
    @DisplayName("should get payment for lead successfully")
    fun `getPaymentForLead should return payment when found`() {
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.paymentId } returns paymentId
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns mappingData
        every { paymentService.getPayment(paymentId) } returns expectedPaymentResponse

        val result = advisorLeadMappingService.getPaymentForLead(advisorId, leadId)

        assertNotNull(result)
        assertEquals(expectedPaymentResponse.id, result.id)
        assertEquals(expectedPaymentResponse.paymentStatus, result.paymentStatus)

        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 1) { paymentService.getPayment(paymentId) }
    }

    @Test
    @DisplayName("should throw exception when payment not found")
    fun `getPaymentForLead should throw exception when payment not found`() {
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.paymentId } returns null
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns mappingData

        assertThrows<AdvisorLeadMappingNoPaymentException> {
            advisorLeadMappingService.getPaymentForLead(advisorId, leadId)
        }

        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 0) { paymentService.getPayment(any()) }
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
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.paymentId } returns paymentId
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns mappingData
        every { paymentService.updatePayment(paymentId, paymentUpdateRequest) } returns expectedPaymentResponse

        val result = advisorLeadMappingService.updatePaymentForLead(advisorId, leadId, paymentUpdateRequest)

        assertNotNull(result)
        assertEquals(expectedPaymentResponse.id, result.id)

        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 1) { paymentService.updatePayment(paymentId, paymentUpdateRequest) }
    }

    @Test
    @DisplayName("should delete payment for lead successfully")
    fun `deletePaymentForLead should delete payment successfully`() {
        val mappingData = mockk<com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData>()
        every { mappingData.paymentId } returns paymentId
        every {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        } returns mappingData
        every { paymentService.deletePayment(paymentId) } returns Unit

        advisorLeadMappingService.deletePaymentForLead(advisorId, leadId)

        verify(exactly = 1) {
            advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        }
        verify(exactly = 1) { paymentService.deletePayment(paymentId) }
    }
}
