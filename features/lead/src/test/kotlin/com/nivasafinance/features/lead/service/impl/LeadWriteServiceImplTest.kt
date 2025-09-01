package com.nivasafinance.features.lead.service.impl

import com.nivasafinance.features.lead.dto.LeadContacts
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadPreliminaryInformation
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeadWriteServiceImplTest {

    private val leadRepository = mockk<LeadRepository>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var leadWriteService: LeadWriteServiceImpl

    @BeforeEach
    fun setup() {
        clearAllMocks()
        leadWriteService = LeadWriteServiceImpl(leadRepository)

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = leadWriteService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(leadWriteService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Lead not found"
    }

    @Nested
    @DisplayName("createLead Tests")
    inner class CreateLeadTests {

        @Test
        @DisplayName("Should create lead successfully with all required fields")
        fun `createLead should create and save lead successfully`() {
            // Given
            val request = createTestLeadCreateRequest()
            val savedLeadId = UUID.randomUUID()

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns savedLeadId
            every { savedLead.requestedAmount } returns request.requestedAmount
            every { savedLead.purpose } returns request.purpose
            every { savedLead.productCode } returns request.productCode
            every { savedLead.stage } returns LeadStage.INQUIRY
            every { savedLead.status } returns LeadStatus.ACTIVE
            every { savedLead.preliminaryInformation } returns request.preliminaryInformation
            every { savedLead.leadContacts } returns request.leadContacts
            every { savedLead.sourcingChannel } returns request.sourcingChannel

            every { leadRepository.save(any()) } returns savedLead

            // When
            val result = leadWriteService.createLead(request)

            // Then
            verify(exactly = 1) { leadRepository.save(any()) }
            assertNotNull(result)
            assertEquals(savedLeadId, result.id)
            assertEquals(request.requestedAmount, result.requestedAmount)
            assertEquals(request.purpose, result.purpose)
            assertEquals(request.productCode, result.productCode)
            assertEquals(LeadStage.INQUIRY, result.stage)
            assertEquals(LeadStatus.ACTIVE, result.status)
        }

        @Test
        @DisplayName("Should create lead with custom stage and status")
        fun `createLead should use custom stage and status when provided`() {
            // Given
            val request = createTestLeadCreateRequest().copy(
                stage = LeadStage.DOCUMENTATION,
                status = LeadStatus.ON_HOLD
            )
            val savedLeadId = UUID.randomUUID()

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns savedLeadId
            every { savedLead.requestedAmount } returns request.requestedAmount
            every { savedLead.purpose } returns request.purpose
            every { savedLead.productCode } returns request.productCode
            every { savedLead.stage } returns LeadStage.DOCUMENTATION
            every { savedLead.status } returns LeadStatus.ON_HOLD
            every { savedLead.preliminaryInformation } returns request.preliminaryInformation
            every { savedLead.leadContacts } returns request.leadContacts
            every { savedLead.sourcingChannel } returns request.sourcingChannel

            every { leadRepository.save(any()) } returns savedLead

            // When
            val result = leadWriteService.createLead(request)

            // Then
            verify(exactly = 1) { leadRepository.save(any()) }
            assertEquals(LeadStage.DOCUMENTATION, result.stage)
            assertEquals(LeadStatus.ON_HOLD, result.status)
        }
    }

    @Nested
    @DisplayName("updateLead Tests")
    inner class UpdateLeadTests {

        @Test
        @DisplayName("Should update and save lead successfully")
        fun `updateLead should update and save lead successfully`() {
            val leadId = UUID.randomUUID()
            val request = LeadUpdateRequest(
                requestedAmount = BigDecimal("750000"),
                purpose = "Home Renovation",
                productCode = "HL002"
            )

            val existingLead = mockk<Lead>(relaxed = true)
            every { existingLead.id } returns leadId
            every { existingLead.requestedAmount } returns BigDecimal("500000")
            every { existingLead.purpose } returns "Home Construction"
            every { existingLead.productCode } returns "HL001"
            every { existingLead.stage } returns LeadStage.INQUIRY
            every { existingLead.status } returns LeadStatus.ACTIVE
            every { existingLead.preliminaryInformation } returns null
            every { existingLead.leadContacts } returns null
            every { existingLead.sourcingChannel } returns SourcingChannel.DIRECT

            val updatedLead = mockk<Lead>(relaxed = true)
            every { updatedLead.id } returns leadId
            every { updatedLead.requestedAmount } returns request.requestedAmount!!
            every { updatedLead.purpose } returns request.purpose!!
            every { updatedLead.productCode } returns request.productCode!!
            every { updatedLead.stage } returns LeadStage.INQUIRY
            every { updatedLead.status } returns LeadStatus.ACTIVE
            every { updatedLead.preliminaryInformation } returns null
            every { updatedLead.leadContacts } returns null
            every { updatedLead.sourcingChannel } returns SourcingChannel.DIRECT

            every { leadRepository.findById(leadId) } returns java.util.Optional.of(existingLead)
            every { leadRepository.save(any()) } returns updatedLead

            val result = leadWriteService.updateLead(leadId, request)

            assertEquals(leadId, result.id)
            assertEquals(request.requestedAmount, result.requestedAmount)
            assertEquals(request.purpose, result.purpose)
            assertEquals(request.productCode, result.productCode)

            verify { leadRepository.findById(leadId) }
            verify { leadRepository.save(any()) }
        }

        @Test
        @DisplayName("Should throw LeadNotFoundException when lead not found")
        fun `updateLead should throw LeadNotFoundException when lead not found`() {
            // Given
            val leadId = UUID.randomUUID()
            val request = createTestLeadUpdateRequest()

            every { leadRepository.findById(leadId) } returns Optional.empty()

            // When & Then
            assertThrows<LeadNotFoundException> {
                leadWriteService.updateLead(leadId, request)
            }
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 0) { leadRepository.save(any()) }
        }
    }

    private fun createTestLeadCreateRequest() = LeadCreateRequest(
        requestedAmount = BigDecimal("500000"),
        purpose = "Home Construction",
        productCode = "HOME_LOAN",
        sourcingChannel = SourcingChannel.DIRECT,
        preliminaryInformation = LeadPreliminaryInformation(
            whenYouWantLoan = "Within 6 months",
            isHouseConstructionStarted = false,
            isEKhathaAvailable = true,
            selfDeclaredAnnualFamilyIncome = 800000
        ),
        leadContacts = LeadContacts(
            name = "John Doe",
            number = "9876543210"
        ),
        stage = null,
        status = null
    )

    private fun createTestLeadUpdateRequest() = LeadUpdateRequest(
        requestedAmount = BigDecimal("750000"),
        purpose = "Home Renovation",
        productCode = "RENOVATION_LOAN",
        sourcingChannel = SourcingChannel.DIRECT,
        preliminaryInformation = LeadPreliminaryInformation(
            whenYouWantLoan = "Within 3 months",
            isHouseConstructionStarted = true,
            isEKhathaAvailable = false,
            selfDeclaredAnnualFamilyIncome = 1000000
        ),
        leadContacts = LeadContacts(
            name = "Jane Smith",
            number = "9876543211"
        ),
        stage = LeadStage.DOCUMENTATION,
        status = LeadStatus.ON_HOLD
    )
}
