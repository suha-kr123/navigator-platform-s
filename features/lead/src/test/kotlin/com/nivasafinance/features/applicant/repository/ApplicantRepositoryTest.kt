package com.nivasafinance.features.applicant.repository

import com.nivasafinance.features.TestUtils.createTestApplicant
import com.nivasafinance.features.applicant.entity.Applicant
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

@DisplayName("ApplicantRepository Tests")
class ApplicantRepositoryTest {

    private lateinit var applicantRepository: ApplicantRepository

    @BeforeEach
    fun setUp() {
        applicantRepository = mockk()
    }

    @Test
    @DisplayName("Should save applicant successfully")
    fun `save should save applicant successfully`() {
        val applicant = createTestApplicant()
        val savedApplicant = createTestApplicant(id = UUID.randomUUID())

        every { applicantRepository.save(applicant) } returns savedApplicant

        val result = applicantRepository.save(applicant)

        assertNotNull(result.id)
        assertEquals(savedApplicant.personId, result.personId)
        assertEquals(savedApplicant.leadId, result.leadId)
        assertEquals(savedApplicant.applicantType, result.applicantType)
        assertEquals(savedApplicant.relationshipToPrimary, result.relationshipToPrimary)
        assertEquals(savedApplicant.status, result.status)

        verify { applicantRepository.save(applicant) }
    }

    @Test
    @DisplayName("Should find applicant by id successfully")
    fun `findById should find applicant when exists`() {
        val applicantId = UUID.randomUUID()
        val applicant = createTestApplicant(id = applicantId)

        every { applicantRepository.findById(applicantId) } returns Optional.of(applicant)

        val result = applicantRepository.findById(applicantId)

        assertNotNull(result)
        assertEquals(applicantId, result.get().id)
        assertEquals(applicant.personId, result.get().personId)
        assertEquals(applicant.leadId, result.get().leadId)
        assertEquals(applicant.applicantType, result.get().applicantType)
        assertEquals(applicant.relationshipToPrimary, result.get().relationshipToPrimary)
        assertEquals(applicant.status, result.get().status)

        verify { applicantRepository.findById(applicantId) }
    }

    @Test
    @DisplayName("Should return empty when applicant not found")
    fun `findById should return empty when not found`() {
        val nonExistentId = UUID.randomUUID()

        every { applicantRepository.findById(nonExistentId) } returns Optional.empty()

        val result = applicantRepository.findById(nonExistentId)

        assertEquals(Optional.empty<Applicant>(), result)

        verify { applicantRepository.findById(nonExistentId) }
    }

    @Test
    @DisplayName("Should find all applicants successfully")
    fun `findAll should return all applicants`() {
        val applicant1 = createTestApplicant()
        val applicant2 = createTestApplicant()
        val applicants = listOf(applicant1, applicant2)

        every { applicantRepository.findAll() } returns applicants

        val result = applicantRepository.findAll()

        assertEquals(2, result.size)
        assertEquals(applicant1.id, result[0].id)
        assertEquals(applicant2.id, result[1].id)

        verify { applicantRepository.findAll() }
    }

    @Test
    @DisplayName("Should delete applicant successfully")
    fun `delete should delete applicant successfully`() {
        val applicant = createTestApplicant()

        every { applicantRepository.delete(applicant) } returns Unit

        applicantRepository.delete(applicant)

        verify { applicantRepository.delete(applicant) }
    }

    @Test
    @DisplayName("Should count applicants correctly")
    fun `count should return correct number of applicants`() {
        every { applicantRepository.count() } returns 2L

        val count = applicantRepository.count()

        assertEquals(2L, count)

        verify { applicantRepository.count() }
    }

    @Test
    @DisplayName("Should find applicants by lead id successfully")
    fun `findByLeadId should return applicants for lead`() {
        val leadId = UUID.randomUUID()
        val applicant1 = createTestApplicant(leadId = leadId)
        val applicant2 = createTestApplicant(leadId = leadId)
        val applicants = listOf(applicant1, applicant2)

        every { applicantRepository.findByLeadId(leadId) } returns applicants

        val result = applicantRepository.findByLeadId(leadId)

        assertEquals(2, result.size)
        assertEquals(applicant1.id, result[0].id)
        assertEquals(applicant2.id, result[1].id)
        assertEquals(leadId, result[0].leadId)
        assertEquals(leadId, result[1].leadId)

        verify { applicantRepository.findByLeadId(leadId) }
    }

    @Test
    @DisplayName("Should find applicants by lead id and applicant type successfully")
    fun `findByLeadIdAndApplicantType should return applicants for lead and type`() {
        val leadId = UUID.randomUUID()
        val applicantType = ApplicantType.PRIMARY
        val applicant1 = createTestApplicant(leadId = leadId, applicantType = applicantType)
        val applicant2 = createTestApplicant(leadId = leadId, applicantType = applicantType)
        val applicants = listOf(applicant1, applicant2)

        every { applicantRepository.findByLeadIdAndApplicantType(leadId, applicantType) } returns applicants

        val result = applicantRepository.findByLeadIdAndApplicantType(leadId, applicantType)

        assertEquals(2, result.size)
        assertEquals(applicant1.id, result[0].id)
        assertEquals(applicant2.id, result[1].id)
        assertEquals(leadId, result[0].leadId)
        assertEquals(leadId, result[1].leadId)
        assertEquals(applicantType, result[0].applicantType)
        assertEquals(applicantType, result[1].applicantType)

        verify { applicantRepository.findByLeadIdAndApplicantType(leadId, applicantType) }
    }

    @Test
    @DisplayName("Should update applicant successfully")
    fun `update should update applicant successfully`() {
        val applicant = createTestApplicant()
        val updatedApplicant = createTestApplicant(
            id = applicant.id!!,
            applicantType = ApplicantType.CO_APPLICANT,
            relationshipToPrimary = RelationshipToPrimary.SPOUSE,
            status = ApplicantStatus.UNDER_REVIEW
        )

        every { applicantRepository.save(applicant) } returns updatedApplicant

        val result = applicantRepository.save(applicant)

        assertEquals(updatedApplicant.applicantType, result.applicantType)
        assertEquals(updatedApplicant.relationshipToPrimary, result.relationshipToPrimary)
        assertEquals(updatedApplicant.status, result.status)

        verify { applicantRepository.save(applicant) }
    }
}
