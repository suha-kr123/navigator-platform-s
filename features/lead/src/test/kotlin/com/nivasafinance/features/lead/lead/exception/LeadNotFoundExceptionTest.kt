package com.nivasafinance.features.lead.lead.exception

import com.nivasafinance.features.lead.exception.LeadNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.UUID

@DisplayName("LeadNotFoundException Tests")
class LeadNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with correct message")
    fun `should create exception with correct message`() {
        val leadId = UUID.randomUUID()
        val messageSource = mockk<MessageSource>()
        val expectedMessage = "Lead not found with id: $leadId"

        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        val exception = LeadNotFoundException(leadId, messageSource)

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("Should create exception with custom message")
    fun `should create exception with custom message`() {
        val customMessage = "Custom lead not found message"

        val exception = LeadNotFoundException(customMessage)

        assertEquals(customMessage, exception.message)
    }
}
