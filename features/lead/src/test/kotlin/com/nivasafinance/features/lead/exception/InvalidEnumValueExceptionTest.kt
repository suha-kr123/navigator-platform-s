package com.nivasafinance.features.lead.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("InvalidEnumValueException Tests")
class InvalidEnumValueExceptionTest {

    @Test
    @DisplayName("Should create exception with correct message")
    fun `should create exception with correct message`() {
        val field = "status"
        val value = "INVALID_VALUE"
        val validValues = listOf("ACTIVE", "INACTIVE", "PENDING")
        val exception = InvalidEnumValueException(field, value, validValues)

        assertEquals(
            "Invalid value '$value' for field '$field'. Valid values are: ${validValues.joinToString(", ")}",
            exception.message
        )
    }
}
