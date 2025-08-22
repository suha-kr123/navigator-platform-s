package com.nivasafinance.features.lead.lead.service.impl

import com.nivasafinance.features.lead.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.lead.applicant.dto.ApplicantResponse
import com.nivasafinance.features.lead.applicant.enum.ApplicantType
import com.nivasafinance.features.lead.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.lead.applicant.service.ApplicantWriteService
import com.nivasafinance.features.lead.lead.dto.LeadContacts
import com.nivasafinance.features.lead.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.lead.dto.LeadPatchRequest
import com.nivasafinance.features.lead.lead.dto.LeadPreimerlyInformation
import com.nivasafinance.features.lead.lead.entity.Lead
import com.nivasafinance.features.lead.lead.enum.LeadStage
import com.nivasafinance.features.lead.lead.enum.LeadStatus
import com.nivasafinance.features.lead.lead.exception.LeadNotFoundException
import com.nivasafinance.features.lead.lead.repository.LeadRepository
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.service.PersonWriteService
import data.Identifier
import data.IdentifierType
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeadWriteServiceImplTest {

    private val leadRepository = mockk<LeadRepository>()
    private val personWriteService = mockk<PersonWriteService>()
    private val applicantWriteService = mockk<ApplicantWriteService>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var leadWriteService: LeadWriteServiceImpl

    @BeforeEach
    fun setup() {
        clearAllMocks()
        leadWriteService = LeadWriteServiceImpl(leadRepository, personWriteService, applicantWriteService)

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
            val personId = UUID.randomUUID()

            val person = mockk<PersonDto>()
            every { person.id } returns personId

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns savedLeadId
            every { savedLead.stage } returns LeadStage.INQUIRY
            every { savedLead.status } returns LeadStatus.ACTIVE

            every { personWriteService.savePerson(any()) } returns person
            every { leadRepository.save(any()) } returns savedLead
            every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                mockk<ApplicantResponse>()

            // When
            val result = leadWriteService.createLead(request)

            // Then
            verify(exactly = 1) { personWriteService.savePerson(request.applicantDetails.personalDetails) }
            verify(exactly = 1) { leadRepository.save(any()) }
            verify(exactly = 1) {
                applicantWriteService.createApplicant(
                    personId,
                    savedLeadId,
                    request.applicantDetails
                )
            }

            assertEquals(savedLeadId, result.id)
            assertEquals(LeadStage.INQUIRY.toString(), result.stage)
            assertEquals(LeadStatus.ACTIVE.toString(), result.status)
        }

        @Test
        @DisplayName("Should create lead with custom stage and status")
        fun `createLead should work with custom stage and status`() {
            // Given
            val request = createTestLeadCreateRequest(
                TestLeadCreateRequestParams(
                    stage = LeadStage.DOCUMENTATION,
                    status = LeadStatus.ON_HOLD
                )
            )
            val savedLeadId = UUID.randomUUID()
            val personId = UUID.randomUUID()

            val person = mockk<PersonDto>()
            every { person.id } returns personId

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns savedLeadId
            every { savedLead.stage } returns LeadStage.DOCUMENTATION
            every { savedLead.status } returns LeadStatus.ON_HOLD

            every { personWriteService.savePerson(any()) } returns person
            every { leadRepository.save(any()) } returns savedLead
            every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                mockk<ApplicantResponse>()

            // When
            val result = leadWriteService.createLead(request)

            // Then
            assertEquals(savedLeadId, result.id)
            assertEquals(LeadStage.DOCUMENTATION.toString(), result.stage)
            assertEquals(LeadStatus.ON_HOLD.toString(), result.status)
        }

        @Test
        @DisplayName("Should create lead with custom ID")
        fun `createLead should work with custom ID`() {
            // Given
            val customId = UUID.randomUUID()
            val request = createTestLeadCreateRequest(
                TestLeadCreateRequestParams(id = customId)
            )
            val personId = UUID.randomUUID()

            val person = mockk<PersonDto>()
            every { person.id } returns personId

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns customId
            every { savedLead.stage } returns LeadStage.INQUIRY
            every { savedLead.status } returns LeadStatus.ACTIVE

            every { personWriteService.savePerson(any()) } returns person
            every { leadRepository.save(any()) } returns savedLead
            every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                mockk<ApplicantResponse>()

            // When
            val result = leadWriteService.createLead(request)

            // Then
            assertEquals(customId, result.id)
        }

        @Test
        @DisplayName("Should throw exception when person ID is null")
        fun `createLead should handle null person ID error`() {
            // Given
            val request = createTestLeadCreateRequest()
            val person = mockk<PersonDto>()
            every { person.id } returns null

            every { personWriteService.savePerson(any()) } returns person

            // When & Then
            val exception = assertThrows<IllegalStateException> {
                leadWriteService.createLead(request)
            }

            assertEquals("Person ID is null", exception.message)
            verify(exactly = 1) { personWriteService.savePerson(any()) }
            verify(exactly = 0) { leadRepository.save(any()) }
            verify(exactly = 0) { applicantWriteService.createApplicant(any(), any(), any()) }
        }

        @ParameterizedTest
        @EnumSource(LeadStage::class)
        @DisplayName("Should create lead with all possible stages")
        fun `createLead should work with all lead stages`(stage: LeadStage) {
            // Given
            val request = createTestLeadCreateRequest(
                TestLeadCreateRequestParams(stage = stage)
            )
            val savedLeadId = UUID.randomUUID()
            val personId = UUID.randomUUID()

            val person = mockk<PersonDto>()
            every { person.id } returns personId

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns savedLeadId
            every { savedLead.stage } returns stage
            every { savedLead.status } returns LeadStatus.ACTIVE

            every { personWriteService.savePerson(any()) } returns person
            every { leadRepository.save(any()) } returns savedLead
            every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                mockk<ApplicantResponse>()

            // When
            val result = leadWriteService.createLead(request)

            // Then
            assertEquals(savedLeadId, result.id)
            assertEquals(stage.toString(), result.stage)
        }

        @ParameterizedTest
        @EnumSource(LeadStatus::class)
        @DisplayName("Should create lead with all possible statuses")
        fun `createLead should work with all lead statuses`(status: LeadStatus) {
            // Given
            val request = createTestLeadCreateRequest(
                TestLeadCreateRequestParams(status = status)
            )
            val savedLeadId = UUID.randomUUID()
            val personId = UUID.randomUUID()

            val person = mockk<PersonDto>()
            every { person.id } returns personId

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns savedLeadId
            every { savedLead.stage } returns LeadStage.INQUIRY
            every { savedLead.status } returns status

            every { personWriteService.savePerson(any()) } returns person
            every { leadRepository.save(any()) } returns savedLead
            every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                mockk<ApplicantResponse>()

            // When
            val result = leadWriteService.createLead(request)

            // Then
            assertEquals(savedLeadId, result.id)
            assertEquals(status.toString(), result.status)
        }

        @Test
        @DisplayName("Should create lead with different applicant types")
        fun `createLead should work with different applicant types`() {
            // Given
            val applicantTypes = listOf(ApplicantType.PRIMARY, ApplicantType.CO_APPLICANT, ApplicantType.GUARANTOR)

            applicantTypes.forEach { applicantType ->
                val request = createTestLeadCreateRequest(
                    TestLeadCreateRequestParams(
                        applicantDetails = ApplicantCreateRequest(
                            personalDetails = PersonDto(),
                            applicantType = applicantType,
                            relationshipToPrimary = RelationshipToPrimary.SELF,
                            description = "Test description"
                        )
                    )
                )
                val savedLeadId = UUID.randomUUID()
                val personId = UUID.randomUUID()

                val person = mockk<PersonDto>()
                every { person.id } returns personId

                val savedLead = mockk<Lead>()
                every { savedLead.id } returns savedLeadId
                every { savedLead.stage } returns LeadStage.INQUIRY
                every { savedLead.status } returns LeadStatus.ACTIVE

                every { personWriteService.savePerson(any()) } returns person
                every { leadRepository.save(any()) } returns savedLead
                every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                    mockk<ApplicantResponse>()

                // When
                val result = leadWriteService.createLead(request)

                // Then
                assertEquals(savedLeadId, result.id)
                verify(exactly = 1) {
                    applicantWriteService.createApplicant(
                        personId,
                        savedLeadId,
                        request.applicantDetails
                    )
                }
            }
        }

        @Test
        @DisplayName("Should create lead with different relationship types")
        fun `createLead should work with different relationship types`() {
            // Given
            val relationships = listOf(
                RelationshipToPrimary.SELF,
                RelationshipToPrimary.SPOUSE,
                RelationshipToPrimary.FATHER,
                RelationshipToPrimary.MOTHER,
                RelationshipToPrimary.BROTHER,
                RelationshipToPrimary.SISTER,
                RelationshipToPrimary.FRIEND,
                RelationshipToPrimary.BUSINESS_PARTNER
            )

            relationships.forEach { relationship ->
                val request = createTestLeadCreateRequest(
                    TestLeadCreateRequestParams(
                        applicantDetails = ApplicantCreateRequest(
                            personalDetails = PersonDto(),
                            applicantType = ApplicantType.PRIMARY,
                            relationshipToPrimary = relationship,
                            description = null
                        )
                    )
                )
                val savedLeadId = UUID.randomUUID()
                val personId = UUID.randomUUID()

                val person = mockk<PersonDto>()
                every { person.id } returns personId

                val savedLead = mockk<Lead>()
                every { savedLead.id } returns savedLeadId
                every { savedLead.stage } returns LeadStage.INQUIRY
                every { savedLead.status } returns LeadStatus.ACTIVE

                every { personWriteService.savePerson(any()) } returns person
                every { leadRepository.save(any()) } returns savedLead
                every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                    mockk<ApplicantResponse>()

                // When
                val result = leadWriteService.createLead(request)

                // Then
                assertEquals(savedLeadId, result.id)
                verify(exactly = 1) {
                    applicantWriteService.createApplicant(
                        personId,
                        savedLeadId,
                        request.applicantDetails
                    )
                }
            }
        }

        @Test
        @DisplayName("Should verify lead entity creation with correct values")
        fun `createLead should create lead entity with correct values`() {
            // Given
            val request = createTestLeadCreateRequest()
            val savedLeadId = UUID.randomUUID()
            val personId = UUID.randomUUID()

            val person = mockk<PersonDto>()
            every { person.id } returns personId

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns savedLeadId
            every { savedLead.stage } returns LeadStage.INQUIRY
            every { savedLead.status } returns LeadStatus.ACTIVE

            val leadSlot = slot<Lead>()
            every { personWriteService.savePerson(any()) } returns person
            every { leadRepository.save(capture(leadSlot)) } returns savedLead
            every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                mockk<ApplicantResponse>()

            // When
            leadWriteService.createLead(request)

            // Then
            val capturedLead = leadSlot.captured
            assertEquals(request.id, capturedLead.id)
            assertEquals(request.requestedAmount, capturedLead.requestedAmount)
            assertEquals(request.purpose, capturedLead.purpose)
            assertEquals(request.productCode, capturedLead.productCode)
            assertEquals(request.status ?: LeadStatus.ACTIVE, capturedLead.status)
            assertEquals(request.stage ?: LeadStage.INQUIRY, capturedLead.stage)
            assertEquals(request.preimerlyInformation, capturedLead.preimerlyInformation)
            assertEquals(request.leadContacts, capturedLead.leadContacts)
            assertEquals(request.sourcingChannel, capturedLead.sourcingChannel)
        }
    }

    @Nested
    @DisplayName("patchLead Tests")
    inner class PatchLeadTests {

        @Test
        @DisplayName("Should update lead successfully with all fields")
        fun `patchLead should update lead successfully with all fields`() {
            // Given
            val leadId = UUID.randomUUID()
            val existingLead = createTestLead(leadId)
            val patchRequest = createTestLeadPatchRequest()

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { leadRepository.save(any()) } returns existingLead

            // When
            leadWriteService.patchLead(leadId, patchRequest)

            // Then
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 1) { leadRepository.save(existingLead) }

            assertEquals(BigDecimal("600000"), existingLead.requestedAmount)
            assertEquals("Home Renovation", existingLead.purpose)
            assertEquals("HL002", existingLead.productCode)
            assertEquals("Website", existingLead.sourcingChannel)
            assertEquals(LeadStage.DOCUMENTATION, existingLead.stage)
            assertEquals(LeadStatus.ON_HOLD, existingLead.status)
            assertNotNull(existingLead.preimerlyInformation)
            assertNotNull(existingLead.leadContacts)
        }

        @Test
        @DisplayName("Should update lead with partial fields")
        fun `patchLead should update lead with partial fields`() {
            // Given
            val leadId = UUID.randomUUID()
            val existingLead = createTestLead(leadId)
            val patchRequest = LeadPatchRequest(
                requestedAmount = BigDecimal("700000"),
                purpose = "Business Expansion"
            )

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { leadRepository.save(any()) } returns existingLead

            // When
            leadWriteService.patchLead(leadId, patchRequest)

            // Then
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 1) { leadRepository.save(existingLead) }

            assertEquals(BigDecimal("700000"), existingLead.requestedAmount)
            assertEquals("Business Expansion", existingLead.purpose)
            // Other fields should remain unchanged
            assertEquals("HL001", existingLead.productCode)
            assertEquals("Direct", existingLead.sourcingChannel)
            assertEquals(LeadStage.INQUIRY, existingLead.stage)
            assertEquals(LeadStatus.ACTIVE, existingLead.status)
        }

        @Test
        @DisplayName("Should update lead with null values (no change)")
        fun `patchLead should handle null values in patch request`() {
            // Given
            val leadId = UUID.randomUUID()
            val existingLead = createTestLead(leadId)
            val originalValues = createTestLead(leadId)

            val patchRequest = LeadPatchRequest(
                requestedAmount = null,
                purpose = null,
                productCode = null,
                sourcingChannel = null,
                stage = null,
                status = null,
                preimerlyInformation = null,
                leadContacts = null
            )

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { leadRepository.save(any()) } returns existingLead

            // When
            leadWriteService.patchLead(leadId, patchRequest)

            // Then
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 1) { leadRepository.save(existingLead) }

            // All fields should remain unchanged
            assertEquals(originalValues.requestedAmount, existingLead.requestedAmount)
            assertEquals(originalValues.purpose, existingLead.purpose)
            assertEquals(originalValues.productCode, existingLead.productCode)
            assertEquals(originalValues.sourcingChannel, existingLead.sourcingChannel)
            assertEquals(originalValues.stage, existingLead.stage)
            assertEquals(originalValues.status, existingLead.status)
            assertEquals(originalValues.preimerlyInformation, existingLead.preimerlyInformation)
            assertEquals(originalValues.leadContacts, existingLead.leadContacts)
        }

        @Test
        @DisplayName("Should update preimerly information successfully")
        fun `patchLead should update preimerly information successfully`() {
            // Given
            val leadId = UUID.randomUUID()
            val existingLead = createTestLead(leadId)
            val newPreimerlyInfo = LeadPreimerlyInformation(
                whenYouWantLoan = "Next month",
                isHouseConstructionStarted = false,
                isEKhathaAvailable = false,
                selfDeclaredAnnualFamilyIncome = 900000
            )

            val patchRequest = LeadPatchRequest(
                preimerlyInformation = newPreimerlyInfo
            )

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { leadRepository.save(any()) } returns existingLead

            // When
            leadWriteService.patchLead(leadId, patchRequest)

            // Then
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 1) { leadRepository.save(existingLead) }

            assertEquals(newPreimerlyInfo, existingLead.preimerlyInformation)
            assertEquals("Next month", existingLead.preimerlyInformation?.whenYouWantLoan)
            assertEquals(false, existingLead.preimerlyInformation?.isHouseConstructionStarted)
            assertEquals(false, existingLead.preimerlyInformation?.isEKhathaAvailable)
            assertEquals(900000, existingLead.preimerlyInformation?.selfDeclaredAnnualFamilyIncome)
        }

        @Test
        @DisplayName("Should update lead contacts successfully")
        fun `patchLead should update lead contacts successfully`() {
            // Given
            val leadId = UUID.randomUUID()
            val existingLead = createTestLead(leadId)
            val newContacts = LeadContacts(
                name = "Jane Doe",
                number = "9876543211"
            )

            val patchRequest = LeadPatchRequest(
                leadContacts = newContacts
            )

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { leadRepository.save(any()) } returns existingLead

            // When
            leadWriteService.patchLead(leadId, patchRequest)

            // Then
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 1) { leadRepository.save(existingLead) }

            assertEquals(newContacts, existingLead.leadContacts)
            assertEquals("Jane Doe", existingLead.leadContacts?.name)
            assertEquals("9876543211", existingLead.leadContacts?.number)
        }

        @Test
        @DisplayName("Should throw exception when lead not found")
        fun `patchLead should throw exception when lead not found`() {
            // Given
            val leadId = UUID.randomUUID()
            val patchRequest = createTestLeadPatchRequest()

            every { leadRepository.findById(leadId) } returns Optional.empty()

            // When & Then
            assertThrows<LeadNotFoundException> {
                leadWriteService.patchLead(leadId, patchRequest)
            }

            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 0) { leadRepository.save(any()) }
        }

        @ParameterizedTest
        @EnumSource(LeadStage::class)
        @DisplayName("Should update lead with all possible stages")
        fun `patchLead should work with all lead stages`(stage: LeadStage) {
            // Given
            val leadId = UUID.randomUUID()
            val existingLead = createTestLead(leadId)
            val patchRequest = LeadPatchRequest(stage = stage)

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { leadRepository.save(any()) } returns existingLead

            // When
            leadWriteService.patchLead(leadId, patchRequest)

            // Then
            assertEquals(stage, existingLead.stage)
        }

        @ParameterizedTest
        @EnumSource(LeadStatus::class)
        @DisplayName("Should update lead with all possible statuses")
        fun `patchLead should work with all lead statuses`(status: LeadStatus) {
            // Given
            val leadId = UUID.randomUUID()
            val existingLead = createTestLead(leadId)
            val patchRequest = LeadPatchRequest(status = status)

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { leadRepository.save(any()) } returns existingLead

            // When
            leadWriteService.patchLead(leadId, patchRequest)

            // Then
            assertEquals(status, existingLead.status)
        }
    }

    @Nested
    @DisplayName("saveIdentifier Tests")
    inner class SaveIdentifierTests {

        @Test
        @DisplayName("Should save identifier successfully")
        fun `saveIdentifier should delegate to applicant service`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(IdentifierType.PAN)

            val existingLead = createTestLead(leadId)

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { applicantWriteService.addIdentifier(any(), any()) } just runs

            // When
            leadWriteService.saveIdentifier(leadId, applicantId, addIdentifier)

            // Then
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 1) { applicantWriteService.addIdentifier(applicantId, addIdentifier) }
        }

        @Test
        @DisplayName("Should throw exception when lead not found for saveIdentifier")
        fun `saveIdentifier should throw exception when lead not found`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(IdentifierType.PAN)

            every { leadRepository.findById(leadId) } returns Optional.empty()

            // When & Then
            assertThrows<LeadNotFoundException> {
                leadWriteService.saveIdentifier(leadId, applicantId, addIdentifier)
            }

            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 0) { applicantWriteService.addIdentifier(any(), any()) }
        }

        @ParameterizedTest
        @EnumSource(IdentifierType::class)
        @DisplayName("Should save identifier with all identifier types")
        fun `saveIdentifier should work with all identifier types`(identifierType: IdentifierType) {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(identifierType)

            val existingLead = createTestLead(leadId)

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { applicantWriteService.addIdentifier(any(), any()) } just runs

            // When
            leadWriteService.saveIdentifier(leadId, applicantId, addIdentifier)

            // Then
            verify(exactly = 1) { applicantWriteService.addIdentifier(applicantId, addIdentifier) }
        }
    }

    @Nested
    @DisplayName("updateIdentifier Tests")
    inner class UpdateIdentifierTests {

        @Test
        @DisplayName("Should update identifier successfully")
        fun `updateIdentifier should delegate to applicant service`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(IdentifierType.VOTER)

            val existingLead = createTestLead(leadId)

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { applicantWriteService.updateIdentifier(any(), any(), any()) } just runs

            // When
            leadWriteService.updateIdentifier(leadId, applicantId, identifierId, addIdentifier)

            // Then
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(
                exactly = 1
            ) { applicantWriteService.updateIdentifier(applicantId, identifierId, addIdentifier) }
        }

        @Test
        @DisplayName("Should throw exception when lead not found for updateIdentifier")
        fun `updateIdentifier should throw exception when lead not found`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(IdentifierType.VOTER)

            every { leadRepository.findById(leadId) } returns Optional.empty()

            // When & Then
            assertThrows<LeadNotFoundException> {
                leadWriteService.updateIdentifier(leadId, applicantId, identifierId, addIdentifier)
            }

            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 0) { applicantWriteService.updateIdentifier(any(), any(), any()) }
        }

        @ParameterizedTest
        @EnumSource(IdentifierType::class)
        @DisplayName("Should update identifier with all identifier types")
        fun `updateIdentifier should work with all identifier types`(identifierType: IdentifierType) {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(identifierType)

            val existingLead = createTestLead(leadId)

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { applicantWriteService.updateIdentifier(any(), any(), any()) } just runs

            // When
            leadWriteService.updateIdentifier(leadId, applicantId, identifierId, addIdentifier)

            // Then
            verify(
                exactly = 1
            ) { applicantWriteService.updateIdentifier(applicantId, identifierId, addIdentifier) }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    inner class EdgeCasesAndErrorScenarios {

        @Test
        @DisplayName("Should handle empty patch request")
        fun `patchLead should handle empty patch request`() {
            // Given
            val leadId = UUID.randomUUID()
            val existingLead = createTestLead(leadId)
            val patchRequest = LeadPatchRequest()

            every { leadRepository.findById(leadId) } returns Optional.of(existingLead)
            every { leadRepository.save(any()) } returns existingLead

            // When
            leadWriteService.patchLead(leadId, patchRequest)

            // Then
            verify(exactly = 1) { leadRepository.findById(leadId) }
            verify(exactly = 1) { leadRepository.save(existingLead) }
            // No changes should be made
        }

        @Test
        @DisplayName("Should handle boundary values for requested amount")
        fun `createLead should handle boundary values for requested amount`() {
            // Given
            val boundaryAmounts = listOf(
                BigDecimal.ZERO,
                BigDecimal.ONE,
                BigDecimal("999999999.99"),
                BigDecimal("-1000")
            )

            boundaryAmounts.forEach { amount ->
                val request = createTestLeadCreateRequest(
                    TestLeadCreateRequestParams(requestedAmount = amount)
                )
                val savedLeadId = UUID.randomUUID()
                val personId = UUID.randomUUID()

                val person = mockk<PersonDto>()
                every { person.id } returns personId

                val savedLead = mockk<Lead>()
                every { savedLead.id } returns savedLeadId
                every { savedLead.stage } returns LeadStage.INQUIRY
                every { savedLead.status } returns LeadStatus.ACTIVE

                every { personWriteService.savePerson(any()) } returns person
                every { leadRepository.save(any()) } returns savedLead
                every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                    mockk<ApplicantResponse>()

                // When
                val result = leadWriteService.createLead(request)

                // Then
                assertEquals(savedLeadId, result.id)
            }
        }

        @Test
        @DisplayName("Should handle special characters in purpose and product code")
        fun `createLead should handle special characters in purpose and product code`() {
            // Given
            val specialPurpose = "Home Construction & Renovation (Phase 1) - Special!"
            val specialProductCode = "HL-001_SPECIAL#2024"

            val request = createTestLeadCreateRequest(
                TestLeadCreateRequestParams(
                    purpose = specialPurpose,
                    productCode = specialProductCode
                )
            )
            val savedLeadId = UUID.randomUUID()
            val personId = UUID.randomUUID()

            val person = mockk<PersonDto>()
            every { person.id } returns personId

            val savedLead = mockk<Lead>()
            every { savedLead.id } returns savedLeadId
            every { savedLead.stage } returns LeadStage.INQUIRY
            every { savedLead.status } returns LeadStatus.ACTIVE

            every { personWriteService.savePerson(any()) } returns person
            every { leadRepository.save(any()) } returns savedLead
            every { applicantWriteService.createApplicant(any(), any(), any()) } returns
                mockk<ApplicantResponse>()

            // When
            val result = leadWriteService.createLead(request)

            // Then
            assertEquals(savedLeadId, result.id)
        }
    }

    // Helper methods
    private data class TestLeadCreateRequestParams(
        val id: UUID = UUID.randomUUID(),
        val requestedAmount: BigDecimal = BigDecimal("500000"),
        val purpose: String = "Home Construction",
        val productCode: String = "HL001",
        val sourcingChannel: String = "Direct",
        val stage: LeadStage? = null,
        val status: LeadStatus? = null,
        val applicantDetails: ApplicantCreateRequest = ApplicantCreateRequest(
            personalDetails = PersonDto(),
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            description = null
        )
    )

    private fun createTestLeadCreateRequest(
        params: TestLeadCreateRequestParams = TestLeadCreateRequestParams()
    ): LeadCreateRequest {
        return LeadCreateRequest(
            id = params.id,
            requestedAmount = params.requestedAmount,
            purpose = params.purpose,
            productCode = params.productCode,
            sourcingChannel = params.sourcingChannel,
            preimerlyInformation = LeadPreimerlyInformation(
                whenYouWantLoan = "In 3 months",
                isHouseConstructionStarted = true,
                isEKhathaAvailable = true,
                selfDeclaredAnnualFamilyIncome = 1000000
            ),
            leadContacts = LeadContacts(
                name = "John Doe",
                number = "9876543210"
            ),
            applicantDetails = params.applicantDetails,
            stage = params.stage,
            status = params.status
        )
    }

    private fun createTestLeadPatchRequest(): LeadPatchRequest {
        return LeadPatchRequest(
            requestedAmount = BigDecimal("600000"),
            purpose = "Home Renovation",
            productCode = "HL002",
            sourcingChannel = "Website",
            stage = LeadStage.DOCUMENTATION,
            status = LeadStatus.ON_HOLD,
            preimerlyInformation = LeadPreimerlyInformation(
                whenYouWantLoan = "Next month",
                isHouseConstructionStarted = false,
                isEKhathaAvailable = false,
                selfDeclaredAnnualFamilyIncome = 900000
            ),
            leadContacts = LeadContacts(
                name = "Jane Doe",
                number = "9876543211"
            )
        )
    }

    private fun createTestLead(id: UUID): Lead {
        return Lead(
            id = id,
            requestedAmount = BigDecimal("500000"),
            purpose = "Home Construction",
            productCode = "HL001",
            sourcingChannel = "Direct",
            stage = LeadStage.INQUIRY,
            status = LeadStatus.ACTIVE,
            preimerlyInformation = LeadPreimerlyInformation(
                whenYouWantLoan = "In 3 months",
                isHouseConstructionStarted = true,
                isEKhathaAvailable = true,
                selfDeclaredAnnualFamilyIncome = 1000000
            ),
            leadContacts = LeadContacts(
                name = "John Doe",
                number = "9876543210"
            )
        )
    }

    private fun createTestIdentifier(type: IdentifierType): Identifier {
        return Identifier(
            id = UUID.randomUUID().toString(),
            identifier = when (type) {
                IdentifierType.PAN -> "ABCDE1234F"
                IdentifierType.VOTER -> "123456789012"
            },
            type = type
        )
    }
}
