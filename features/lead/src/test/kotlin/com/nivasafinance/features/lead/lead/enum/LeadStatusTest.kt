package com.nivasafinance.features.lead.lead.enum

import com.nivasafinance.features.lead.enum.LeadStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("LeadStatus Tests")
class LeadStatusTest {

    @Test
    @DisplayName("Should have correct enum values")
    fun `should have correct enum values`() {
        assertEquals(LeadStatus.ACTIVE, LeadStatus.valueOf("ACTIVE"))
        assertEquals(LeadStatus.ON_HOLD, LeadStatus.valueOf("ON_HOLD"))
        assertEquals(LeadStatus.REJECTED, LeadStatus.valueOf("REJECTED"))
        assertEquals(LeadStatus.CANCELLED, LeadStatus.valueOf("CANCELLED"))
        assertEquals(LeadStatus.COMPLETED, LeadStatus.valueOf("COMPLETED"))
    }

    @Test
    @DisplayName("Should have correct number of enum values")
    fun `should have correct number of enum values`() {
        assertEquals(5, LeadStatus.values().size)
    }
}
