package com.nivasafinance.features.lead.applicant.service.impl

import com.nivasafinance.features.lead.applicant.entity.Applicant
import com.nivasafinance.features.lead.applicant.enum.ApplicantStatus
import com.nivasafinance.features.lead.applicant.enum.ApplicantType
import com.nivasafinance.features.lead.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.lead.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.lead.applicant.repository.ApplicantRepository
import com.nivasafinance.features.lead.applicant.service.ApplicantReadService
import com.nivasafinance.features.person.entity.Person
import com.nivasafinance.features.person.repository.PersonRepository
import data.Identifier
import data.IdentifierType
import exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.MessageSource
import java.util.Optional
import java.util.UUID

class ApplicantReadServiceImplTest {

    private val applicantRepository = mockk<ApplicantRepository>()
    private val personRepository = mockk<PersonRepository>()
    private val messageSource = mockk<MessageSource>()
    private lateinit var applicantReadService: ApplicantReadService

    @BeforeEach
    fun setUp() {
        applicantReadService = ApplicantReadServiceImpl(
            applicantRepository = applicantRepository,
            personRepository = personRepository
        )

        // Mock messageSource for BaseNavigatorService using reflection
        val messageSourceField = applicantReadService.javaClass.superclass.getDeclaredField("messageSource")
        messageSourceField.isAccessible = true
        messageSourceField.set(applicantReadService, messageSource)

        // Mock messageSource behavior
        every { messageSource.getMessage(any(), any(), any()) } returns "Test message"
    }

    // ==================== getApplicantById Tests ====================

    @Test
    fun `getApplicantById should return applicant response when found`() {
        // Given
        val applicantId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()
        val status = ApplicantStatus.NEEDS_TO_BE_REVIEWED

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = status
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)

        // When
        val result = applicantReadService.getApplicantById(applicantId)

        // Then
        verify { applicantRepository.findById(applicantId) }
        assert(result.id == applicantId)
        assert(result.personId == personId)
        assert(result.leadId == leadId)
        assert(result.status == status.toString())
    }

    @Test
    fun `getApplicantById should throw exception when not found`() {
        // Given
        val applicantId = UUID.randomUUID()
        every { applicantRepository.findById(applicantId) } returns Optional.empty()

        // When & Then
        assertThrows<ApplicantNotFoundException> {
            applicantReadService.getApplicantById(applicantId)
        }

        verify { applicantRepository.findById(applicantId) }
    }

    @Test
    fun `getApplicantById should handle all applicant statuses correctly`() {
        // Test all possible ApplicantStatus values
        val testCases = listOf(
            ApplicantStatus.NEEDS_TO_BE_REVIEWED,
            ApplicantStatus.UNDER_REVIEW,
            ApplicantStatus.REVIEWED,
            ApplicantStatus.INACTIVE
        )

        testCases.forEach { status ->
            val applicantId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()

            val applicant = Applicant(
                id = applicantId,
                personId = personId,
                leadId = leadId,
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = status
            )

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)

            val result = applicantReadService.getApplicantById(applicantId)

            assert(result.id == applicantId)
            assert(result.personId == personId)
            assert(result.leadId == leadId)
            assert(result.status == status.toString())
        }
    }

    @Test
    fun `getApplicantById should handle multiple calls with same applicant ID`() {
        // Given
        val applicantId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()
        val status = ApplicantStatus.UNDER_REVIEW

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = status
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)

        // When - Multiple calls
        val result1 = applicantReadService.getApplicantById(applicantId)
        val result2 = applicantReadService.getApplicantById(applicantId)
        val result3 = applicantReadService.getApplicantById(applicantId)

        // Then
        verify(exactly = 3) { applicantRepository.findById(applicantId) }
        assert(result1.id == applicantId)
        assert(result2.id == applicantId)
        assert(result3.id == applicantId)
        assert(result1.personId == personId)
        assert(result2.personId == personId)
        assert(result3.personId == personId)
        assert(result1.leadId == leadId)
        assert(result2.leadId == leadId)
        assert(result3.leadId == leadId)
        assert(result1.status == status.toString())
        assert(result2.status == status.toString())
        assert(result3.status == status.toString())
    }

    // ==================== getByIdentifierId Tests ====================

    @Test
    fun `getByIdentifierId should return identifier response when found`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.REVIEWED
        )

        val identifier = Identifier(
            id = identifierId.toString(),
            identifier = "ABCDE1234F",
            type = IdentifierType.PAN
        )

        val person = Person(
            id = personId,
            firstName = null,
            middleName = null,
            lastName = null,
            mobileNumber = null,
            email = null,
            dateOfBirth = null,
            gender = null,
            identifiers = listOf(identifier),
            addresses = null,
            dataExt = null
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When
        val result = applicantReadService.getByIdentifierId(applicantId, identifierId)

        // Then
        verify { applicantRepository.findById(applicantId) }
        verify { personRepository.findById(personId) }
        assert(result.id == identifierId)
        assert(result.type == IdentifierType.PAN)
        assert(result.identifier == "ABCDE1234F")
    }

    @Test
    fun `getByIdentifierId should throw exception when applicant not found`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()

        every { applicantRepository.findById(applicantId) } returns Optional.empty()

        // When & Then
        assertThrows<ApplicantNotFoundException> {
            applicantReadService.getByIdentifierId(applicantId, identifierId)
        }
        verify { applicantRepository.findById(applicantId) }
        verify(exactly = 0) { personRepository.findById(any()) }
    }

    @Test
    fun `getByIdentifierId should throw exception when person not found`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.REVIEWED
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
        every { personRepository.findById(personId) } returns Optional.empty()

        // When & Then
        assertThrows<ResourceNotFoundException> {
            applicantReadService.getByIdentifierId(applicantId, identifierId)
        }
        verify { applicantRepository.findById(applicantId) }
        verify { personRepository.findById(personId) }
    }

    @Test
    fun `getByIdentifierId should throw exception when identifier not found`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.REVIEWED
        )

        val person = Person(
            id = personId,
            firstName = null,
            middleName = null,
            lastName = null,
            mobileNumber = null,
            email = null,
            dateOfBirth = null,
            gender = null,
            identifiers = emptyList(),
            addresses = null,
            dataExt = null
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When & Then
        assertThrows<ResourceNotFoundException> {
            applicantReadService.getByIdentifierId(applicantId, identifierId)
        }
        verify { applicantRepository.findById(applicantId) }
        verify { personRepository.findById(personId) }
    }

    @Test
    fun `getByIdentifierId should handle null identifiers list`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.REVIEWED
        )

        val person = Person(
            id = personId,
            firstName = null,
            middleName = null,
            lastName = null,
            mobileNumber = null,
            email = null,
            dateOfBirth = null,
            gender = null,
            identifiers = null,
            addresses = null,
            dataExt = null
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When & Then
        assertThrows<ResourceNotFoundException> {
            applicantReadService.getByIdentifierId(applicantId, identifierId)
        }
        verify { applicantRepository.findById(applicantId) }
        verify { personRepository.findById(personId) }
    }

    @Test
    fun `getByIdentifierId should handle different identifier types correctly`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.REVIEWED
        )

        val identifier = Identifier(
            id = identifierId.toString(),
            identifier = "123456789012",
            type = IdentifierType.VOTER
        )

        val person = Person(
            id = personId,
            firstName = null,
            middleName = null,
            lastName = null,
            mobileNumber = null,
            email = null,
            dateOfBirth = null,
            gender = null,
            identifiers = listOf(identifier),
            addresses = null,
            dataExt = null
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When
        val result = applicantReadService.getByIdentifierId(applicantId, identifierId)

        // Then
        verify { applicantRepository.findById(applicantId) }
        verify { personRepository.findById(personId) }
        assert(result.id == identifierId)
        assert(result.type == IdentifierType.VOTER)
        assert(result.identifier == "123456789012")
    }

    @Test
    fun `getByIdentifierId should work with multiple identifiers`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId1 = UUID.randomUUID()
        val identifierId2 = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.REVIEWED
        )

        val identifier1 = Identifier(
            id = identifierId1.toString(),
            identifier = "ABCDE1234F",
            type = IdentifierType.PAN
        )

        val identifier2 = Identifier(
            id = identifierId2.toString(),
            identifier = "123456789012",
            type = IdentifierType.VOTER
        )

        val person = Person(
            id = personId,
            firstName = null,
            middleName = null,
            lastName = null,
            mobileNumber = null,
            email = null,
            dateOfBirth = null,
            gender = null,
            identifiers = listOf(identifier1, identifier2),
            addresses = null,
            dataExt = null
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When
        val result1 = applicantReadService.getByIdentifierId(applicantId, identifierId1)
        val result2 = applicantReadService.getByIdentifierId(applicantId, identifierId2)

        // Then
        assert(result1.id == identifierId1)
        assert(result1.type == IdentifierType.PAN)
        assert(result1.identifier == "ABCDE1234F")

        assert(result2.id == identifierId2)
        assert(result2.type == IdentifierType.VOTER)
        assert(result2.identifier == "123456789012")
    }

    @Test
    fun `getByIdentifierId should work with all identifier types`() {
        // Test all possible IdentifierType values
        val testCases = listOf(
            IdentifierType.PAN,
            IdentifierType.VOTER
        )

        testCases.forEach { identifierType ->
            val applicantId = UUID.randomUUID()
            val identifierId = UUID.randomUUID()
            val personId = UUID.randomUUID()
            val leadId = UUID.randomUUID()

            val applicant = Applicant(
                id = applicantId,
                personId = personId,
                leadId = leadId,
                applicantType = ApplicantType.PRIMARY,
                relationshipToPrimary = RelationshipToPrimary.SELF,
                status = ApplicantStatus.REVIEWED
            )

            val identifier = Identifier(
                id = identifierId.toString(),
                identifier = "TEST_VALUE",
                type = identifierType
            )

            val person = Person(
                id = personId,
                firstName = null,
                middleName = null,
                lastName = null,
                mobileNumber = null,
                email = null,
                dateOfBirth = null,
                gender = null,
                identifiers = listOf(identifier),
                addresses = null,
                dataExt = null
            )

            every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
            every { personRepository.findById(personId) } returns Optional.of(person)

            val result = applicantReadService.getByIdentifierId(applicantId, identifierId)

            assert(result.id == identifierId)
            assert(result.type == identifierType)
            assert(result.identifier == "TEST_VALUE")
        }
    }

    @Test
    fun `getByIdentifierId should handle identifier with null value`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.REVIEWED
        )

        val identifier = Identifier(
            id = identifierId.toString(),
            identifier = null,
            type = IdentifierType.PAN
        )

        val person = Person(
            id = personId,
            firstName = null,
            middleName = null,
            lastName = null,
            mobileNumber = null,
            email = null,
            dateOfBirth = null,
            gender = null,
            identifiers = listOf(identifier),
            addresses = null,
            dataExt = null
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When
        val result = applicantReadService.getByIdentifierId(applicantId, identifierId)

        // Then
        verify { applicantRepository.findById(applicantId) }
        verify { personRepository.findById(personId) }
        assert(result.id == identifierId)
        assert(result.type == IdentifierType.PAN)
        assert(result.identifier == null)
    }

    @Test
    fun `getByIdentifierId should handle edge case with empty identifier list`() {
        // Given
        val applicantId = UUID.randomUUID()
        val identifierId = UUID.randomUUID()
        val personId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val applicant = Applicant(
            id = applicantId,
            personId = personId,
            leadId = leadId,
            applicantType = ApplicantType.PRIMARY,
            relationshipToPrimary = RelationshipToPrimary.SELF,
            status = ApplicantStatus.REVIEWED
        )

        val person = Person(
            id = personId,
            firstName = null,
            middleName = null,
            lastName = null,
            mobileNumber = null,
            email = null,
            dateOfBirth = null,
            gender = null,
            identifiers = emptyList(),
            addresses = null,
            dataExt = null
        )

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)
        every { personRepository.findById(personId) } returns Optional.of(person)

        // When & Then
        assertThrows<ResourceNotFoundException> {
            applicantReadService.getByIdentifierId(applicantId, identifierId)
        }
        verify { applicantRepository.findById(applicantId) }
        verify { personRepository.findById(personId) }
    }
}
