package com.nivasafinance.features.lead.repository

import com.nivasafinance.features.TestUtils.createTestLead
import com.nivasafinance.features.lead.entity.Lead
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

@DisplayName("LeadRepository Tests")
class LeadRepositoryTest {

    private lateinit var leadRepository: LeadRepository

    @BeforeEach
    fun setUp() {
        leadRepository = mockk()
    }

    @Test
    @DisplayName("Should save lead successfully")
    fun `save should save lead successfully`() {
        val lead = createTestLead()
        val savedLead = createTestLead(id = UUID.randomUUID())

        every { leadRepository.save(lead) } returns savedLead

        val result = leadRepository.save(lead)

        assertNotNull(result.id)
        assertEquals(savedLead.requestedAmount, result.requestedAmount)
        assertEquals(savedLead.purpose, result.purpose)
        assertEquals(savedLead.productCode, result.productCode)
        assertEquals(savedLead.sourcingChannel, result.sourcingChannel)
        assertEquals(savedLead.stage, result.stage)
        assertEquals(savedLead.status, result.status)

        verify { leadRepository.save(lead) }
    }

    @Test
    @DisplayName("Should find lead by id successfully")
    fun `findById should find lead when exists`() {
        val leadId = UUID.randomUUID()
        val lead = createTestLead(id = leadId)

        every { leadRepository.findById(leadId) } returns Optional.of(lead)

        val result = leadRepository.findById(leadId)

        assertNotNull(result)
        assertEquals(leadId, result.get().id)
        assertEquals(lead.requestedAmount, result.get().requestedAmount)
        assertEquals(lead.purpose, result.get().purpose)
        assertEquals(lead.productCode, result.get().productCode)
        assertEquals(lead.sourcingChannel, result.get().sourcingChannel)
        assertEquals(lead.stage, result.get().stage)
        assertEquals(lead.status, result.get().status)

        verify { leadRepository.findById(leadId) }
    }

    @Test
    @DisplayName("Should return empty when lead not found")
    fun `findById should return empty when not found`() {
        val nonExistentId = UUID.randomUUID()

        every { leadRepository.findById(nonExistentId) } returns Optional.empty()

        val result = leadRepository.findById(nonExistentId)

        assertEquals(Optional.empty<Lead>(), result)

        verify { leadRepository.findById(nonExistentId) }
    }

    @Test
    @DisplayName("Should find all leads successfully")
    fun `findAll should return all leads`() {
        val lead1 = createTestLead()
        val lead2 = createTestLead()
        val leads = listOf(lead1, lead2)

        every { leadRepository.findAll() } returns leads

        val result = leadRepository.findAll()

        assertEquals(2, result.size)
        assertEquals(lead1.id, result[0].id)
        assertEquals(lead2.id, result[1].id)

        verify { leadRepository.findAll() }
    }

    @Test
    @DisplayName("Should delete lead successfully")
    fun `delete should delete lead successfully`() {
        val lead = createTestLead()

        every { leadRepository.delete(lead) } returns Unit

        leadRepository.delete(lead)

        verify { leadRepository.delete(lead) }
    }

    @Test
    @DisplayName("Should count leads correctly")
    fun `count should return correct number of leads`() {
        every { leadRepository.count() } returns 2L

        val count = leadRepository.count()

        assertEquals(2L, count)

        verify { leadRepository.count() }
    }
}
