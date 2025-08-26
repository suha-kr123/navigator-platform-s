package com.nivasafinance.features.lead.applicant.exception

import com.nivasafinance.features.applicant.exception.PrimaryApplicantAlreadyExistsException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.UUID

@DisplayName("PrimaryApplicantAlreadyExistsException Tests")
class PrimaryApplicantAlreadyExistsExceptionTest {

    @Test
    @DisplayName("Should create exception with correct message")
    fun `should create exception with correct message`() {
        val leadId = UUID.randomUUID()
        val messageSource = mockk<MessageSource>()
        val expectedMessage = "Primary applicant already exists for lead: $leadId"

        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        val exception = PrimaryApplicantAlreadyExistsException(leadId, messageSource)

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("Should create exception with custom message")
    fun `should create exception with custom message`() {
        val customMessage = "Custom primary applicant exists message"

        val exception = PrimaryApplicantAlreadyExistsException(customMessage)

        assertEquals(customMessage, exception.message)
    }
}
