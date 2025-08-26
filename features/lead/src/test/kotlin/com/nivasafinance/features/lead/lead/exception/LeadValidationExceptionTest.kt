package com.nivasafinance.features.lead.lead.exception

import com.nivasafinance.features.lead.exception.LeadValidationException
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
