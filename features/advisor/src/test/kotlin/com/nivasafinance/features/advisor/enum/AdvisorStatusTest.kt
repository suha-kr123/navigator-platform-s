package com.nivasafinance.features.advisor.enum

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AdvisorStatus Tests")
class AdvisorStatusTest {

    @Test
    @DisplayName("should have correct enum values")
    fun `AdvisorStatus should have correct enum values`() {
        val values = AdvisorStatus.values()

        assertEquals(3, values.size)
        assertEquals(AdvisorStatus.CREATED, values[0])
        assertEquals(AdvisorStatus.ACTIVE, values[1])
        assertEquals(AdvisorStatus.INACTIVE, values[2])
    }

    @Test
    @DisplayName("should return correct enum by name")
    fun `AdvisorStatus should return correct enum by name`() {
        val created = AdvisorStatus.valueOf("CREATED")
        val active = AdvisorStatus.valueOf("ACTIVE")
        val inactive = AdvisorStatus.valueOf("INACTIVE")

        assertNotNull(created)
        assertNotNull(active)
        assertNotNull(inactive)

        assertEquals(AdvisorStatus.CREATED, created)
        assertEquals(AdvisorStatus.ACTIVE, active)
        assertEquals(AdvisorStatus.INACTIVE, inactive)
    }

    @Test
    @DisplayName("should have correct ordinal values")
    fun `AdvisorStatus should have correct ordinal values`() {
        assertEquals(0, AdvisorStatus.CREATED.ordinal)
        assertEquals(1, AdvisorStatus.ACTIVE.ordinal)
        assertEquals(2, AdvisorStatus.INACTIVE.ordinal)
    }
}
