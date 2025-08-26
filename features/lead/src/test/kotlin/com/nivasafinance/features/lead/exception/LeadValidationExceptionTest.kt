package com.nivasafinance.features.lead.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("LeadValidationException Tests")
class LeadValidationExceptionTest {

    @Test
    @DisplayName("Should create exception with correct message")
    fun `should create exception with correct message`() {
        val message = "Lead validation failed"
        val exception = LeadValidationException(message)

        assertEquals(message, exception.message)
    }
}
