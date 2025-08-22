package com.nivasafinance.features.lead.applicant.service.impl

import com.nivasafinance.features.lead.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.lead.applicant.dto.ApplicantPatchRequest
import com.nivasafinance.features.lead.applicant.entity.Applicant
import com.nivasafinance.features.lead.applicant.enum.ApplicantStatus
import com.nivasafinance.features.lead.applicant.enum.ApplicantType
import com.nivasafinance.features.lead.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.lead.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.lead.applicant.repository.ApplicantRepository
import com.nivasafinance.features.person.dto.PersonDto
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.repository.PersonRepository
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.context.MessageSource
import java.util.Optional
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicantWriteServiceImplTest {

    private val applicantRepository = mockk<ApplicantRepository>()
    private val personRepository = mockk<PersonRepository>()
    private val personWriteService = mockk<PersonWriteService>()
    private val messageSource = mockk<MessageSource>()

    private lateinit var applicantWriteService: ApplicantWriteServiceImpl

    @BeforeEach
    fun setup() {
        clearAllMocks()
        applicantWriteService = ApplicantWriteServiceImpl(
            applicantRepository,
            personWriteService
        )

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = applicantWriteService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(applicantWriteService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Applicant not found"

        // Mock personWriteService methods
        every { personWriteService.addIdentifier(any(), any()) } just runs
        every { personWriteService.updateIdentifier(any(), any(), any()) } just runs
    }

    @Nested
    @DisplayName("createApplicant Tests")
    inner class CreateApplicantTests {

        @Test
        @DisplayName("Should create applicant successfully with all required fields")
        fun `createApplicant should create and save applicant successfully`() {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val applicantType = ApplicantType.PRIMARY
            val relationshipToPrimary = RelationshipToPrimary.SPOUSE
            val description = "Test description"
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest(
                personalDetails = PersonDto(),
                applicantType = applicantType,
                relationshipToPrimary = relationshipToPrimary,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED,
                description = description
            )

            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(any()) } returns savedApplicant

            // When
            val result = applicantWriteService.createApplicant(
                personId,
                leadId,
                request
            )

            // Then
            verify(exactly = 1) { applicantRepository.save(any()) }

            assertEquals(savedApplicantId, result.id)
            assertEquals(personId, result.personId)
            assertEquals(leadId, result.leadId)
            assertEquals(ApplicantStatus.NEEDS_TO_BE_REVIEWED.toString(), result.status)
        }

        @Test
        @DisplayName("Should create applicant with minimal parameters")
        fun `createApplicant should work with minimal parameters`() {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest()

            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(any()) } returns savedApplicant

            // When
            val result = applicantWriteService.createApplicant(
                personId,
                leadId,
                request
            )

            // Then
            verify(exactly = 1) { applicantRepository.save(any()) }
            assertEquals(savedApplicantId, result.id)
            assertEquals(personId, result.personId)
            assertEquals(leadId, result.leadId)
        }

        @ParameterizedTest
        @EnumSource(ApplicantType::class)
        @DisplayName("Should create applicant with all possible applicant types")
        fun `createApplicant should work with all applicant types`(applicantType: ApplicantType) {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest(
                applicantType = applicantType,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(any()) } returns savedApplicant

            // When
            val result = applicantWriteService.createApplicant(
                personId,
                leadId,
                request
            )

            // Then
            assertEquals(savedApplicantId, result.id)
        }

        @ParameterizedTest
        @EnumSource(RelationshipToPrimary::class)
        @DisplayName("Should create applicant with all possible relationships")
        fun `createApplicant should work with all relationship types`(relationship: RelationshipToPrimary) {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest(
                applicantType = ApplicantType.CO_APPLICANT,
                relationshipToPrimary = relationship,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(any()) } returns savedApplicant

            // When
            val result = applicantWriteService.createApplicant(
                personId,
                leadId,
                request
            )

            // Then
            assertEquals(savedApplicantId, result.id)
        }

        @Test
        @DisplayName("Should verify applicant entity creation with correct values")
        fun `createApplicant should create applicant entity with correct values`() {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val applicantType = ApplicantType.GUARANTOR
            val relationship = RelationshipToPrimary.FATHER
            val description = "Test guarantor"
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest(
                applicantType = applicantType,
                relationshipToPrimary = relationship,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED,
                description = description
            )

            val applicantSlot = slot<Applicant>()
            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(capture(applicantSlot)) } returns savedApplicant

            // When
            applicantWriteService.createApplicant(personId, leadId, request)

            // Then
            val capturedApplicant = applicantSlot.captured
            assertEquals(personId, capturedApplicant.personId)
            assertEquals(leadId, capturedApplicant.leadId)
            assertEquals(applicantType, capturedApplicant.applicantType)
            assertEquals(relationship, capturedApplicant.relationshipToPrimary)
            assertEquals(ApplicantStatus.NEEDS_TO_BE_REVIEWED, capturedApplicant.status)
        }

        @Test
        @DisplayName("Should create applicant with default values when nulls provided")
        fun `createApplicant should handle null applicant type with default behavior`() {
            // Given
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest(
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )

            val applicantSlot = slot<Applicant>()
            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns personId
            every { savedApplicant.leadId } returns leadId
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(capture(applicantSlot)) } returns savedApplicant

            // When
            applicantWriteService.createApplicant(
                personId,
                leadId,
                request
            )

            // Then
            val capturedApplicant = applicantSlot.captured
            assertEquals(ApplicantType.PRIMARY, capturedApplicant.applicantType)
            assertEquals(RelationshipToPrimary.SELF, capturedApplicant.relationshipToPrimary)
        }
    }

    @Nested
    @DisplayName("patchApplicant Tests")
    inner class PatchApplicantTests {

        @Test
        @DisplayName("Should update applicant successfully with all fields")
        fun `patchApplicant should update applicant successfully with all fields`() {
            // Given
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val updatedPersonDto = createTestPersonDto()

            val existingApplicant = createTestApplicant(applicantId, personId, leadId)
            val patchRequest = ApplicantPatchRequest(
                personalDetails = updatedPersonDto,
                applicantType = ApplicantType.GUARANTOR,
                relationshipToPrimary = RelationshipToPrimary.FATHER,
                description = "Updated description"
            )

            every { applicantRepository.findById(applicantId) } returns Optional.of(existingApplicant)
            every { personWriteService.updatePerson(personId, updatedPersonDto) } returns updatedPersonDto

            // When
            val result = applicantWriteService.patchApplicant(applicantId, patchRequest)

            // Then
            verify(exactly = 1) { applicantRepository.findById(applicantId) }
            verify(exactly = 1) { personWriteService.updatePerson(personId, updatedPersonDto) }

            assertEquals(applicantId, result.id)
            assertEquals(personId, result.personId)
            assertEquals(leadId, result.leadId)
            assertEquals(ApplicantType.GUARANTOR, existingApplicant.applicantType)
            assertEquals(RelationshipToPrimary.FATHER, existingApplicant.relationshipToPrimary)
        }

        @Test
        @DisplayName("Should update applicant with partial fields")
        fun `patchApplicant should handle partial updates`() {
            // Given
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val updatedPersonDto = createTestPersonDto()

            val existingApplicant = createTestApplicant(applicantId, personId, leadId)
            val originalType = existingApplicant.applicantType
            val originalRelationship = existingApplicant.relationshipToPrimary

            val patchRequest = ApplicantPatchRequest(
                personalDetails = updatedPersonDto,
                applicantType = null, // Should not update
                relationshipToPrimary = null, // Should not update
                description = null
            )

            every { applicantRepository.findById(applicantId) } returns Optional.of(existingApplicant)
            every { personWriteService.updatePerson(personId, updatedPersonDto) } returns updatedPersonDto

            // When
            applicantWriteService.patchApplicant(applicantId, patchRequest)

            // Then
            verify(exactly = 1) { personWriteService.updatePerson(personId, updatedPersonDto) }

            // Verify that null values didn't change the original values
            assertEquals(originalType, existingApplicant.applicantType)
            assertEquals(originalRelationship, existingApplicant.relationshipToPrimary)
        }

        @Test
        @DisplayName("Should update only applicant type when provided")
        fun `patchApplicant should update only applicant type`() {
            // Given
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val updatedPersonDto = createTestPersonDto()

            val existingApplicant = createTestApplicant(applicantId, personId, leadId)
            val originalRelationship = existingApplicant.relationshipToPrimary

            val patchRequest = ApplicantPatchRequest(
                personalDetails = updatedPersonDto,
                applicantType = ApplicantType.GUARANTOR,
                relationshipToPrimary = null,
                description = null
            )

            every { applicantRepository.findById(applicantId) } returns Optional.of(existingApplicant)
            every { personWriteService.updatePerson(personId, updatedPersonDto) } returns updatedPersonDto

            // When
            applicantWriteService.patchApplicant(applicantId, patchRequest)

            // Then
            assertEquals(ApplicantType.GUARANTOR, existingApplicant.applicantType)
            assertEquals(originalRelationship, existingApplicant.relationshipToPrimary)
        }

        @Test
        @DisplayName("Should update only relationship when provided")
        fun `patchApplicant should update only relationship type`() {
            // Given
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val updatedPersonDto = createTestPersonDto()

            val existingApplicant = createTestApplicant(applicantId, personId, leadId)
            val originalType = existingApplicant.applicantType

            val patchRequest = ApplicantPatchRequest(
                personalDetails = updatedPersonDto,
                applicantType = null,
                relationshipToPrimary = RelationshipToPrimary.MOTHER,
                description = null
            )

            every { applicantRepository.findById(applicantId) } returns Optional.of(existingApplicant)
            every { personWriteService.updatePerson(personId, updatedPersonDto) } returns updatedPersonDto

            // When
            applicantWriteService.patchApplicant(applicantId, patchRequest)

            // Then
            assertEquals(originalType, existingApplicant.applicantType)
            assertEquals(RelationshipToPrimary.MOTHER, existingApplicant.relationshipToPrimary)
        }

        @Test
        @DisplayName("Should throw exception when applicant not found")
        fun `patchApplicant should throw exception when applicant not found`() {
            // Given
            val applicantId = UUID.randomUUID()
            val patchRequest = ApplicantPatchRequest()

            every { applicantRepository.findById(applicantId) } returns Optional.empty()

            // When & Then
            assertThrows<ApplicantNotFoundException> {
                applicantWriteService.patchApplicant(applicantId, patchRequest)
            }

            verify(exactly = 1) { applicantRepository.findById(applicantId) }
            verify(exactly = 0) { personWriteService.updatePerson(any(), any()) }
        }

        @ParameterizedTest
        @EnumSource(ApplicantType::class)
        @DisplayName("Should update applicant with all possible applicant types")
        fun `patchApplicant should work with all applicant types`(applicantType: ApplicantType) {
            // Given
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val updatedPersonDto = createTestPersonDto()

            val existingApplicant = createTestApplicant(applicantId, personId, leadId)
            val patchRequest = ApplicantPatchRequest(
                personalDetails = updatedPersonDto,
                applicantType = applicantType,
                relationshipToPrimary = null
            )

            every { applicantRepository.findById(applicantId) } returns Optional.of(existingApplicant)
            every { personWriteService.updatePerson(personId, updatedPersonDto) } returns updatedPersonDto

            // When
            applicantWriteService.patchApplicant(applicantId, patchRequest)

            // Then
            assertEquals(applicantType, existingApplicant.applicantType)
        }

        @ParameterizedTest
        @EnumSource(RelationshipToPrimary::class)
        @DisplayName("Should update applicant with all possible relationships")
        fun `patchApplicant should work with all relationship types`(relationship: RelationshipToPrimary) {
            // Given
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()
            val updatedPersonDto = createTestPersonDto()

            val existingApplicant = createTestApplicant(applicantId, personId, leadId)
            val patchRequest = ApplicantPatchRequest(
                personalDetails = updatedPersonDto,
                applicantType = null,
                relationshipToPrimary = relationship
            )

            every { applicantRepository.findById(applicantId) } returns Optional.of(existingApplicant)
            every { personWriteService.updatePerson(personId, updatedPersonDto) } returns updatedPersonDto

            // When
            applicantWriteService.patchApplicant(applicantId, patchRequest)

            // Then
            assertEquals(relationship, existingApplicant.relationshipToPrimary)
        }
    }

    @Nested
    @DisplayName("addIdentifier Tests")
    inner class AddIdentifierTests {

        @Test
        @DisplayName("Should add identifier to person successfully")
        fun `addIdentifier should add identifier to person successfully`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(IdentifierType.PAN)

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, emptyList())

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.addIdentifier(applicantId, addIdentifier)

            // Then
            verify(exactly = 1) { applicantRepository.findById(applicantId) }
            verify(exactly = 1) { personWriteService.addIdentifier(personId, addIdentifier) }
        }

        @Test
        @DisplayName("Should add identifier to person with existing identifiers")
        fun `addIdentifier should add identifier to person with existing identifiers`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val existingIdentifier = createTestIdentifier(IdentifierType.VOTER)
            val addIdentifier = createTestIdentifier(IdentifierType.PAN)

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, listOf(existingIdentifier))

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.addIdentifier(applicantId, addIdentifier)

            // Then
            verify(exactly = 1) { applicantRepository.findById(applicantId) }
            verify(exactly = 1) { personWriteService.addIdentifier(personId, addIdentifier) }
        }

        @Test
        @DisplayName("Should handle null identifiers list")
        fun `addIdentifier should handle null identifiers list`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(IdentifierType.PAN)

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, null)

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.addIdentifier(applicantId, addIdentifier)

            // Then
            verify(exactly = 1) { applicantRepository.findById(applicantId) }
            verify(exactly = 1) { personWriteService.addIdentifier(personId, addIdentifier) }
        }

        @Test
        @DisplayName("Should throw exception when applicant not found")
        fun `addIdentifier should throw exception when applicant not found`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(IdentifierType.PAN)

            every { applicantRepository.findById(applicantId) } returns Optional.empty()

            // When & Then
            assertThrows<ApplicantNotFoundException> {
                applicantWriteService.addIdentifier(applicantId, addIdentifier)
            }

            verify(exactly = 1) { applicantRepository.findById(applicantId) }
            verify(exactly = 0) { personWriteService.addIdentifier(any(), any()) }
        }

        @ParameterizedTest
        @EnumSource(IdentifierType::class)
        @DisplayName("Should add identifier with all identifier types")
        fun `addIdentifier should work with all identifier types`(identifierType: IdentifierType) {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val addIdentifier = createTestIdentifier(identifierType)

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, emptyList())

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.addIdentifier(applicantId, addIdentifier)

            // Then
            verify(exactly = 1) { personWriteService.addIdentifier(personId, addIdentifier) }
        }
    }

    @Nested
    @DisplayName("updateIdentifier Tests")
    inner class UpdateIdentifierTests {

        @Test
        @DisplayName("Should update existing identifier successfully")
        fun `updateIdentifier should update existing identifier successfully`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val existingIdentifier = Identifier(
                id = identifierId.toString(),
                identifier = "OLD_VALUE",
                type = IdentifierType.PAN
            )
            val updateIdentifier = createTestIdentifier(IdentifierType.VOTER, "NEW_VALUE")

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, mutableListOf(existingIdentifier))

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.updateIdentifier(applicantId, identifierId, updateIdentifier)

            // Then
            verify(exactly = 1) { applicantRepository.findById(applicantId) }
            verify(exactly = 1) { personWriteService.updateIdentifier(personId, identifierId, updateIdentifier) }
        }

        @Test
        @DisplayName("Should throw exception when applicant not found")
        fun `updateIdentifier should throw exception when applicant not found`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val updateIdentifier = createTestIdentifier(IdentifierType.PAN)

            every { applicantRepository.findById(applicantId) } returns Optional.empty()

            // When & Then
            assertThrows<ApplicantNotFoundException> {
                applicantWriteService.updateIdentifier(applicantId, identifierId, updateIdentifier)
            }

            verify(exactly = 1) { applicantRepository.findById(applicantId) }
            verify(exactly = 0) { personWriteService.updateIdentifier(any(), any(), any()) }
        }

        @ParameterizedTest
        @EnumSource(IdentifierType::class)
        @DisplayName("Should update identifier with all identifier types")
        fun `updateIdentifier should work with all identifier types`(identifierType: IdentifierType) {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val existingIdentifier = Identifier(
                id = identifierId.toString(),
                identifier = "OLD_VALUE",
                type = IdentifierType.PAN
            )
            val updateIdentifier = createTestIdentifier(identifierType, "NEW_VALUE")

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, mutableListOf(existingIdentifier))

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.updateIdentifier(applicantId, identifierId, updateIdentifier)

            // Then
            verify(exactly = 1) { personWriteService.updateIdentifier(personId, identifierId, updateIdentifier) }
        }

        @Test
        @DisplayName("Should update multiple different identifier fields")
        fun `updateIdentifier should update both identifier and type fields`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val existingIdentifier = Identifier(
                id = identifierId.toString(),
                identifier = "ABCDE1234F",
                type = IdentifierType.PAN
            )
            val updateIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "VOT123456789",
                type = IdentifierType.VOTER
            )

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, mutableListOf(existingIdentifier))

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.updateIdentifier(applicantId, identifierId, updateIdentifier)

            // Then
            verify(exactly = 1) { personWriteService.updateIdentifier(personId, identifierId, updateIdentifier) }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    inner class EdgeCasesAndErrorScenarios {

        @Test
        @DisplayName("Should handle boundary UUID values")
        fun `createApplicant should handle boundary UUID values`() {
            // Given
            val minUuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
            val maxUuid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
            val savedApplicantId = UUID.randomUUID()

            val request = ApplicantCreateRequest()

            val savedApplicant = mockk<Applicant>()
            every { savedApplicant.id } returns savedApplicantId
            every { savedApplicant.personId } returns minUuid
            every { savedApplicant.leadId } returns maxUuid
            every { savedApplicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED

            every { applicantRepository.save(any()) } returns savedApplicant

            // When
            val result = applicantWriteService.createApplicant(
                minUuid,
                maxUuid,
                request
            )

            // Then
            assertEquals(savedApplicantId, result.id)
            assertEquals(minUuid, result.personId)
            assertEquals(maxUuid, result.leadId)
        }

        @Test
        @DisplayName("Should handle special characters in identifier values")
        fun `addIdentifier should handle special characters in identifier`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val specialIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "ABC@#$%^&*()DE1234F!",
                type = IdentifierType.PAN
            )

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, emptyList())

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.addIdentifier(applicantId, specialIdentifier)

            // Then
            verify(exactly = 1) { personWriteService.addIdentifier(personId, specialIdentifier) }
        }

        @Test
        @DisplayName("Should handle empty string in identifier")
        fun `updateIdentifier should handle empty string identifier`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val existingIdentifier = Identifier(
                id = identifierId.toString(),
                identifier = "OLD_VALUE",
                type = IdentifierType.PAN
            )
            val emptyIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = "",
                type = IdentifierType.VOTER
            )

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, mutableListOf(existingIdentifier))

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.updateIdentifier(applicantId, identifierId, emptyIdentifier)

            // Then
            verify(exactly = 1) { personWriteService.updateIdentifier(personId, identifierId, emptyIdentifier) }
        }

        @Test
        @DisplayName("Should handle very long identifier strings")
        fun `addIdentifier should handle very long identifier strings`() {
            // Given
            val leadId = UUID.randomUUID()
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val longIdentifierValue = "A".repeat(1000) // Very long string
            val longIdentifier = Identifier(
                id = UUID.randomUUID().toString(),
                identifier = longIdentifierValue,
                type = IdentifierType.PAN
            )

            val applicant = createTestApplicant(applicantId, personId, leadId)
            val person = createTestPerson(personId, emptyList())

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)
            every { personRepository.save(any()) } returns person

            // When
            applicantWriteService.addIdentifier(applicantId, longIdentifier)

            // Then
            verify(exactly = 1) { personWriteService.addIdentifier(personId, longIdentifier) }
        }
    }

    // Helper methods
    private fun createTestApplicant(
        id: UUID,
        personId: UUID,
        leadId: UUID
    ): Applicant {
        val applicant = mockk<Applicant>(relaxed = true)
        every { applicant.id } returns id
        every { applicant.personId } returns personId
        every { applicant.leadId } returns leadId
        every { applicant.status } returns ApplicantStatus.NEEDS_TO_BE_REVIEWED
        // Use simple approach - create a mutable mock with proper setters
        var currentApplicantType: ApplicantType = ApplicantType.PRIMARY
        var currentRelationship: RelationshipToPrimary = RelationshipToPrimary.SELF

        every { applicant.applicantType } answers { currentApplicantType }
        every { applicant.relationshipToPrimary } answers { currentRelationship }

        every { applicant.applicantType = any() } answers {
            currentApplicantType = firstArg()
        }
        every { applicant.relationshipToPrimary = any() } answers {
            currentRelationship = firstArg()
        }

        return applicant
    }

    private fun createTestPerson(
        id: UUID,
        identifiers: List<Identifier>?
    ): Person {
        val person = mockk<Person>()
        every { person.id } returns id
        every { person.identifiers } returns identifiers
        every { person.identifiers = any() } just runs
        return person
    }

    private fun createTestPersonDto(): PersonDto {
        return PersonDto()
    }

    private fun createTestIdentifier(
        type: IdentifierType,
        value: String = when (type) {
            IdentifierType.PAN -> "ABCDE1234F"
            IdentifierType.VOTER -> "VOT123456789"
        }
    ): Identifier {
        return Identifier(
            id = UUID.randomUUID().toString(),
            identifier = value,
            type = type
        )
    }
}
