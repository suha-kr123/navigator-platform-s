package com.nivasafinance.features.lead.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("InvalidLeadStatusException Tests")
class InvalidLeadStatusExceptionTest {

    @Test
    @DisplayName("Should create exception with correct message")
    fun `should create exception with correct message`() {
        val status = "INVALID_STATUS"
        val exception = InvalidLeadStatusException(status)

        assertEquals("Invalid lead status: $status", exception.message)
    }
}
