package com.nivasafinance.features.lead.enum

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("LeadStage Tests")
class LeadStageTest {

    @Test
    @DisplayName("Should have correct enum values")
    fun `should have correct enum values`() {
        assertEquals(LeadStage.INQUIRY, LeadStage.valueOf("INQUIRY"))
        assertEquals(LeadStage.DOCUMENTATION, LeadStage.valueOf("DOCUMENTATION"))
        assertEquals(LeadStage.PROCESSING, LeadStage.valueOf("PROCESSING"))
        assertEquals(LeadStage.SANCTION, LeadStage.valueOf("SANCTION"))
        assertEquals(LeadStage.DISBURSEMENT, LeadStage.valueOf("DISBURSEMENT"))
        assertEquals(LeadStage.CLOSED, LeadStage.valueOf("CLOSED"))
    }

    @Test
    @DisplayName("Should have correct number of enum values")
    fun `should have correct number of enum values`() {
        assertEquals(6, LeadStage.values().size)
    }
}
