package com.nivasafinance.features.lead.lead.service.impl

import com.nivasafinance.features.lead.lead.entity.Lead
import com.nivasafinance.features.lead.lead.enum.LeadStage
import com.nivasafinance.features.lead.lead.enum.LeadStatus
import com.nivasafinance.features.lead.lead.exception.LeadNotFoundException
import com.nivasafinance.features.lead.lead.repository.LeadRepository
import com.nivasafinance.features.lead.lead.service.LeadReadService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class LeadReadServiceImplTest {

    private val leadRepository = mockk<LeadRepository>()
    private val messageSource = mockk<MessageSource>()
    private lateinit var leadReadService: LeadReadService

    @BeforeEach
    fun setUp() {
        leadReadService = LeadReadServiceImpl(leadRepository)

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = leadReadService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(leadReadService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    @Test
    fun `getLeadById should return lead response when lead exists`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.INQUIRY
        val status = LeadStatus.ACTIVE

        val lead = Lead(
            id = leadId,
            requestedAmount = BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            sourcingChannel = "Direct",
            stage = stage,
            status = status,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assert(result.id == leadId)
        assert(result.stage == stage.toString())
        assert(result.status == status.toString())
    }

    @Test
    fun `getLeadById should throw exception when lead not found`() {
        // Given
        val leadId = UUID.randomUUID()

        every { leadRepository.findById(leadId) } returns Optional.empty()

        // When & Then
        assertThrows<LeadNotFoundException> {
            leadReadService.getLeadById(leadId)
        }
        verify { leadRepository.findById(leadId) }
    }

    @Test
    fun `getLeadById should handle different lead stages correctly`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.INQUIRY
        val status = LeadStatus.ACTIVE

        val lead = Lead(
            id = leadId,
            requestedAmount = BigDecimal("300000"),
            purpose = "Business Loan",
            productCode = "BL001",
            sourcingChannel = "Agent",
            stage = stage,
            status = status,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assert(result.stage == LeadStage.INQUIRY.toString())
        assert(result.status == LeadStatus.ACTIVE.toString())
    }

    @Test
    fun `getLeadById should handle different lead statuses correctly`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.CLOSED
        val status = LeadStatus.COMPLETED

        val lead = Lead(
            id = leadId,
            requestedAmount = BigDecimal("800000"),
            purpose = "Home Loan",
            productCode = "HL002",
            sourcingChannel = "Website",
            stage = stage,
            status = status,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assert(result.stage == LeadStage.CLOSED.toString())
        assert(result.status == LeadStatus.COMPLETED.toString())
    }

    @Test
    fun `getLeadById should work with all lead stages`() {
        // Test all possible LeadStage values
        val leadId = UUID.randomUUID()
        val testCases = listOf(
            LeadStage.INQUIRY to "INQUIRY",
            LeadStage.DOCUMENTATION to "DOCUMENTATION",
            LeadStage.PROCESSING to "PROCESSING",
            LeadStage.SANCTION to "SANCTION",
            LeadStage.DISBURSEMENT to "DISBURSEMENT",
            LeadStage.CLOSED to "CLOSED"
        )

        testCases.forEach { (stage, expectedString) ->
            val lead = Lead(
                id = leadId,
                requestedAmount = BigDecimal("500000"),
                purpose = "Test Purpose",
                productCode = "TEST001",
                sourcingChannel = "Test",
                stage = stage,
                status = LeadStatus.ACTIVE,
                preimerlyInformation = mockk(),
                leadContacts = mockk()
            )

            every { leadRepository.findById(leadId) } returns Optional.of(lead)

            val result = leadReadService.getLeadById(leadId)

            assert(result.stage == expectedString)
        }
    }

    @Test
    fun `getLeadById should work with all lead statuses`() {
        // Test all possible LeadStatus values
        val leadId = UUID.randomUUID()
        val testCases = listOf(
            LeadStatus.ACTIVE to "ACTIVE",
            LeadStatus.ON_HOLD to "ON_HOLD",
            LeadStatus.REJECTED to "REJECTED",
            LeadStatus.CANCELLED to "CANCELLED",
            LeadStatus.COMPLETED to "COMPLETED"
        )

        testCases.forEach { (status, expectedString) ->
            val lead = Lead(
                id = leadId,
                requestedAmount = BigDecimal("500000"),
                purpose = "Test Purpose",
                productCode = "TEST001",
                sourcingChannel = "Test",
                stage = LeadStage.INQUIRY,
                status = status,
                preimerlyInformation = mockk(),
                leadContacts = mockk()
            )

            every { leadRepository.findById(leadId) } returns Optional.of(lead)

            val result = leadReadService.getLeadById(leadId)

            assert(result.status == expectedString)
        }
    }

    @Test
    fun `getLeadById should handle lead with complex data`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.DOCUMENTATION
        val status = LeadStatus.COMPLETED

        val lead = Lead(
            id = leadId,
            requestedAmount = BigDecimal("750000"),
            purpose = "Home Construction",
            productCode = "HL003",
            sourcingChannel = "Referral",
            stage = stage,
            status = status,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assert(result.id == leadId)
        assert(result.stage == stage.toString())
        assert(result.status == status.toString())
    }

    @Test
    fun `getLeadById should handle lead with minimal data`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.INQUIRY
        val status = LeadStatus.ACTIVE

        val lead = Lead(
            id = leadId,
            requestedAmount = BigDecimal("100000"),
            purpose = "Personal Loan",
            productCode = "PL001",
            sourcingChannel = "Direct",
            stage = stage,
            status = status,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assert(result.id == leadId)
        assert(result.stage == stage.toString())
        assert(result.status == status.toString())
    }

    @Test
    fun `getLeadById should handle edge case with maximum values`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.CLOSED
        val status = LeadStatus.COMPLETED

        val lead = Lead(
            id = leadId,
            requestedAmount = BigDecimal("999999999.99"),
            purpose = "Maximum Test",
            productCode = "MAX001",
            sourcingChannel = "Maximum Channel",
            stage = stage,
            status = status,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assert(result.id == leadId)
        assert(result.stage == stage.toString())
        assert(result.status == status.toString())
    }

    @Test
    fun `getLeadById should handle edge case with minimum values`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.INQUIRY
        val status = LeadStatus.ACTIVE

        val lead = Lead(
            id = leadId,
            requestedAmount = BigDecimal("0.01"),
            purpose = "A",
            productCode = "MIN",
            sourcingChannel = "A",
            stage = stage,
            status = status,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        // When
        val result = leadReadService.getLeadById(leadId)

        // Then
        verify { leadRepository.findById(leadId) }
        assert(result.id == leadId)
        assert(result.stage == stage.toString())
        assert(result.status == status.toString())
    }

    @Test
    fun `getLeadById should handle multiple calls with same lead ID`() {
        // Given
        val leadId = UUID.randomUUID()
        val stage = LeadStage.PROCESSING
        val status = LeadStatus.ON_HOLD

        val lead = Lead(
            id = leadId,
            requestedAmount = BigDecimal("500000"),
            purpose = "Test Purpose",
            productCode = "TEST001",
            sourcingChannel = "Test",
            stage = stage,
            status = status,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        // When - Multiple calls
        val result1 = leadReadService.getLeadById(leadId)
        val result2 = leadReadService.getLeadById(leadId)
        val result3 = leadReadService.getLeadById(leadId)

        // Then
        verify(exactly = 3) { leadRepository.findById(leadId) }
        assert(result1.id == leadId)
        assert(result2.id == leadId)
        assert(result3.id == leadId)
        assert(result1.stage == stage.toString())
        assert(result2.stage == stage.toString())
        assert(result3.stage == stage.toString())
    }

    @Test
    fun `getLeadById should handle different lead IDs correctly`() {
        // Given
        val leadId1 = UUID.randomUUID()
        val leadId2 = UUID.randomUUID()
        val leadId3 = UUID.randomUUID()

        val lead1 = Lead(
            id = leadId1,
            requestedAmount = BigDecimal("100000"),
            purpose = "Lead 1",
            productCode = "L1",
            sourcingChannel = "Channel 1",
            stage = LeadStage.INQUIRY,
            status = LeadStatus.ACTIVE,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        val lead2 = Lead(
            id = leadId2,
            requestedAmount = BigDecimal("200000"),
            purpose = "Lead 2",
            productCode = "L2",
            sourcingChannel = "Channel 2",
            stage = LeadStage.DOCUMENTATION,
            status = LeadStatus.ON_HOLD,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        val lead3 = Lead(
            id = leadId3,
            requestedAmount = BigDecimal("300000"),
            purpose = "Lead 3",
            productCode = "L3",
            sourcingChannel = "Channel 3",
            stage = LeadStage.PROCESSING,
            status = LeadStatus.REJECTED,
            preimerlyInformation = mockk(),
            leadContacts = mockk()
        )

        every { leadRepository.findById(leadId1) } returns Optional.of(lead1)
        every { leadRepository.findById(leadId2) } returns Optional.of(lead2)
        every { leadRepository.findById(leadId3) } returns Optional.of(lead3)

        // When
        val result1 = leadReadService.getLeadById(leadId1)
        val result2 = leadReadService.getLeadById(leadId2)
        val result3 = leadReadService.getLeadById(leadId3)

        // Then
        verify { leadRepository.findById(leadId1) }
        verify { leadRepository.findById(leadId2) }
        verify { leadRepository.findById(leadId3) }
        assert(result1.id == leadId1)
        assert(result2.id == leadId2)
        assert(result3.id == leadId3)
        assert(result1.stage == LeadStage.INQUIRY.toString())
        assert(result2.stage == LeadStage.DOCUMENTATION.toString())
        assert(result3.stage == LeadStage.PROCESSING.toString())
    }
}
