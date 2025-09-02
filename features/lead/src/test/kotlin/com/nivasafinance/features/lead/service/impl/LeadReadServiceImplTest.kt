package com.nivasafinance.features.lead.service.impl

import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import com.nivasafinance.features.lead.exception.LeadNotFoundException
import com.nivasafinance.features.lead.repository.LeadRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.UUID

class LeadReadServiceImplTest {

    private val leadRepository = mockk<LeadRepository>()
    private val messageSource = mockk<MessageSource>()
    private lateinit var leadReadService: LeadReadServiceImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        leadReadService = LeadReadServiceImpl(leadRepository)

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = leadReadService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(leadReadService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Lead not found"
    }

    @Test
    @DisplayName("Should return lead response when lead exists")
    fun `getLeadById should return lead response when lead exists`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.INQUIRY
        val status = LeadStatus.ACTIVE

        val lead = mockk<Lead>()
        every { lead.id } returns leadId
        every { lead.requestedAmount } returns BigDecimal("500000")
        every { lead.purpose } returns "Home Construction"
        every { lead.productCode } returns "HL001"
        every { lead.sourcingChannel } returns SourcingChannel.DIRECT
        every { lead.stage } returns stage
        every { lead.status } returns status
        every { lead.preliminaryInformation } returns null
        every { lead.leadContacts } returns null

        every { leadRepository.findById(leadId) } returns java.util.Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assertNotNull(result)
        assertEquals(leadId, result.id)
        assertEquals(stage, result.stage)
        assertEquals(status, result.status)
        assertEquals(BigDecimal("500000"), result.requestedAmount)
        assertEquals("Home Construction", result.purpose)
        assertEquals("HL001", result.productCode)
        assertEquals(SourcingChannel.DIRECT, result.sourcingChannel)
    }

    @Test
    @DisplayName("Should throw exception when lead not found")
    fun `getLeadById should throw exception when lead not found`() {
        // Given
        val leadId = UUID.randomUUID()

        every { leadRepository.findById(leadId) } returns java.util.Optional.empty()

        // When & Then
        assertThrows<LeadNotFoundException> {
            leadReadService.getLeadById(leadId)
        }
        verify { leadRepository.findById(leadId) }
    }

    @Test
    @DisplayName("Should handle different lead stages correctly")
    fun `getLeadById should handle different lead stages correctly`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.INQUIRY
        val status = LeadStatus.ACTIVE

        val lead = mockk<Lead>()
        every { lead.id } returns leadId
        every { lead.requestedAmount } returns BigDecimal("750000")
        every { lead.purpose } returns "Business Loan"
        every { lead.productCode } returns "BL002"
        every { lead.sourcingChannel } returns SourcingChannel.ADVISOR
        every { lead.stage } returns stage
        every { lead.status } returns status
        every { lead.preliminaryInformation } returns null
        every { lead.leadContacts } returns null

        every { leadRepository.findById(leadId) } returns java.util.Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assertNotNull(result)
        assertEquals(leadId, result.id)
        assertEquals(stage, result.stage)
        assertEquals(status, result.status)
        assertEquals(BigDecimal("750000"), result.requestedAmount)
        assertEquals("Business Loan", result.purpose)
        assertEquals("BL002", result.productCode)
        assertEquals(SourcingChannel.ADVISOR, result.sourcingChannel)
    }

    @Test
    @DisplayName("Should handle different lead statuses correctly")
    fun `getLeadById should handle different lead statuses correctly`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.INQUIRY
        val status = LeadStatus.ON_HOLD

        val lead = mockk<Lead>()
        every { lead.id } returns leadId
        every { lead.requestedAmount } returns BigDecimal("300000")
        every { lead.purpose } returns "Personal Loan"
        every { lead.productCode } returns "PL003"
        every { lead.sourcingChannel } returns SourcingChannel.DIRECT
        every { lead.stage } returns stage
        every { lead.status } returns status
        every { lead.preliminaryInformation } returns null
        every { lead.leadContacts } returns null

        every { leadRepository.findById(leadId) } returns java.util.Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assertNotNull(result)
        assertEquals(leadId, result.id)
        assertEquals(stage, result.stage)
        assertEquals(status, result.status)
        assertEquals(BigDecimal("300000"), result.requestedAmount)
        assertEquals("Personal Loan", result.purpose)
        assertEquals("PL003", result.productCode)
        assertEquals(SourcingChannel.DIRECT, result.sourcingChannel)
    }
}
