package com.nivasafinance.features.applicant.exception

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.UUID

@DisplayName("ApplicantNotFoundException Tests")
class ApplicantNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with correct message")
    fun `should create exception with correct message`() {
        val applicantId = UUID.randomUUID()
        val messageSource = mockk<MessageSource>()
        val expectedMessage = "Applicant not found with id: $applicantId"

        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        val exception = ApplicantNotFoundException(applicantId, messageSource)

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("Should create exception with custom message")
    fun `should create exception with custom message`() {
        val customMessage = "Custom applicant not found message"

        val exception = ApplicantNotFoundException(customMessage)

        assertEquals(customMessage, exception.message)
    }
}
