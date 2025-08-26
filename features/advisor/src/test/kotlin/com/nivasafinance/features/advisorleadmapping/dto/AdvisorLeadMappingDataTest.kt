package com.nivasafinance.features.advisorleadmapping.dto

import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorLeadMappingData Tests")
class AdvisorLeadMappingDataTest {

    @Test
    @DisplayName("should create AdvisorLeadMappingData from entity")
    fun `fromEntity should create AdvisorLeadMappingData from entity`() {
        val id = UUID.randomUUID()
        val advisorId = UUID.randomUUID()
        val leadId = UUID.randomUUID()
        val paymentId = UUID.randomUUID()

        val entity = AdvisorLeadMapping(
            id = id,
            advisorId = advisorId,
            leadId = leadId,
            paymentId = paymentId,
            remarks = "Test mapping",
            extData = mapOf("key" to "value")
        )

        val data = AdvisorLeadMappingData.fromEntity(entity)

        assertNotNull(data)
        assertEquals(id, data.id)
        assertEquals(advisorId, data.advisorId)
        assertEquals(leadId, data.leadId)
        assertEquals(paymentId, data.paymentId)
        assertEquals("Test mapping", data.remarks)
        assertEquals(mapOf("key" to "value"), data.extData)
    }

    @Test
    @DisplayName("should handle null values in entity")
    fun `fromEntity should handle null values in entity`() {
        val id = UUID.randomUUID()
        val advisorId = UUID.randomUUID()
        val leadId = UUID.randomUUID()

        val entity = AdvisorLeadMapping(
            id = id,
            advisorId = advisorId,
            leadId = leadId,
            paymentId = null,
            remarks = null,
            extData = null
        )

        val data = AdvisorLeadMappingData.fromEntity(entity)

        assertNotNull(data)
        assertEquals(id, data.id)
        assertEquals(advisorId, data.advisorId)
        assertEquals(leadId, data.leadId)
        assertEquals(null, data.paymentId)
        assertEquals(null, data.remarks)
        assertEquals(null, data.extData)
    }
}
