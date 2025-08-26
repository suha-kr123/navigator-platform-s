package com.nivasafinance.features.lead.lead.exception

import com.nivasafinance.features.lead.exception.InvalidLeadStageException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("InvalidLeadStageException Tests")
class InvalidLeadStageExceptionTest {

    @Test
    @DisplayName("Should create exception with correct message")
    fun `should create exception with correct message`() {
        val stage = "INVALID_STAGE"
        val exception = InvalidLeadStageException(stage)

        assertEquals("Invalid lead stage: $stage", exception.message)
    }
}
