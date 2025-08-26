package com.nivasafinance.features.advisor.repository

import com.nivasafinance.TestUtils.createTestAdvisor
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorRepository Tests")
class AdvisorRepositoryTest {

    private lateinit var advisorRepository: AdvisorRepository
    private lateinit var advisor: Advisor
    private val advisorId = UUID.randomUUID()
    private val personId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        advisorRepository = mockk<AdvisorRepository>()
        advisor = createTestAdvisor(
            id = advisorId,
            personId = personId,
            advisorCode = "ADV001",
            status = AdvisorStatus.ACTIVE
        )
    }

    @Test
    @DisplayName("should find advisor by id when exists")
    fun `findById should return advisor when exists`() {
        // Given
        every { advisorRepository.findById(advisorId) } returns Optional.of(advisor)

        // When
        val foundAdvisor = advisorRepository.findById(advisorId)

        // Then
        assertNotNull(foundAdvisor)
        assert(foundAdvisor.isPresent)
        assertEquals(advisorId, foundAdvisor.get().id)
        assertEquals(personId, foundAdvisor.get().personId)
        assertEquals("ADV001", foundAdvisor.get().advisorCode)

        verify(exactly = 1) { advisorRepository.findById(advisorId) }
    }

    @Test
    @DisplayName("should return empty when advisor not found by id")
    fun `findById should return empty when not found`() {
        // Given
        val nonExistentId = UUID.randomUUID()
        every { advisorRepository.findById(nonExistentId) } returns Optional.empty()

        // When
        val foundAdvisor = advisorRepository.findById(nonExistentId)

        // Then
        assert(foundAdvisor.isEmpty)
        verify(exactly = 1) { advisorRepository.findById(nonExistentId) }
    }

    @Test
    @DisplayName("should find advisor by person id when exists")
    fun `findByPersonId should return advisor when exists`() {
        // Given
        every { advisorRepository.findByPersonId(personId) } returns advisor

        // When
        val foundAdvisor = advisorRepository.findByPersonId(personId)

        // Then
        assertNotNull(foundAdvisor)
        assertEquals(advisorId, foundAdvisor!!.id)
        assertEquals(personId, foundAdvisor.personId)
        assertEquals("ADV001", foundAdvisor.advisorCode)

        verify(exactly = 1) { advisorRepository.findByPersonId(personId) }
    }

    @Test
    @DisplayName("should return null when advisor not found by person id")
    fun `findByPersonId should return null when not found`() {
        // Given
        val nonExistentPersonId = UUID.randomUUID()
        every { advisorRepository.findByPersonId(nonExistentPersonId) } returns null

        // When
        val foundAdvisor = advisorRepository.findByPersonId(nonExistentPersonId)

        // Then
        assertEquals(null, foundAdvisor)
        verify(exactly = 1) { advisorRepository.findByPersonId(nonExistentPersonId) }
    }

    @Test
    @DisplayName("should save advisor successfully")
    fun `save should save advisor successfully`() {
        // Given
        every { advisorRepository.save(advisor) } returns advisor

        // When
        val savedAdvisor = advisorRepository.save(advisor)

        // Then
        assertNotNull(savedAdvisor)
        assertEquals(advisorId, savedAdvisor.id)
        assertEquals(personId, savedAdvisor.personId)
        assertEquals("ADV001", savedAdvisor.advisorCode)

        verify(exactly = 1) { advisorRepository.save(advisor) }
    }

    @Test
    @DisplayName("should delete advisor successfully")
    fun `deleteById should delete advisor successfully`() {
        // Given
        every { advisorRepository.deleteById(advisorId) } returns Unit

        // When
        advisorRepository.deleteById(advisorId)

        // Then
        verify(exactly = 1) { advisorRepository.deleteById(advisorId) }
    }
}
