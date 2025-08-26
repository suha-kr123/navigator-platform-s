package com.nivasafinance.features.advisor.exception

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("Advisor Exception Tests")
class AdvisorExceptionTest {

    private val messageSource = mockk<MessageSource>()

    @Test
    @DisplayName("should create AdvisorNotFoundException with correct message")
    fun `AdvisorNotFoundException should have correct message`() {
        // Given
        val advisorId = UUID.randomUUID()
        val expectedMessage = "Advisor not found with id: $advisorId"
        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        // When
        val exception = AdvisorNotFoundException(advisorId, messageSource)

        // Then
        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should create AdvisorMobileNotFoundException with correct message")
    fun `AdvisorMobileNotFoundException should have correct message`() {
        // Given
        val mobileNo = "1234567890"
        val expectedMessage = "Advisor not found with mobile number: $mobileNo"
        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        // When
        val exception = AdvisorMobileNotFoundException(mobileNo, messageSource)

        // Then
        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should create AdvisorConflictException with correct message")
    fun `AdvisorConflictException should have correct message`() {
        // Given
        val advisorCode = "ADV001"
        val expectedMessage = "Advisor with code $advisorCode already exists"
        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        // When
        val exception = AdvisorConflictException(advisorCode, messageSource)

        // Then
        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should create AdvisorMobileAlreadyExistsException with correct message")
    fun `AdvisorMobileAlreadyExistsException should have correct message`() {
        // Given
        val mobileNo = "1234567890"
        val expectedMessage = "Advisor with mobile number $mobileNo already exists"
        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        // When
        val exception = AdvisorMobileAlreadyExistsException(mobileNo, messageSource)

        // Then
        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should create AdvisorValidationException with correct message")
    fun `AdvisorValidationException should have correct message`() {
        // Given
        val messageKey = "error.advisor.code.required"
        val expectedMessage = "Advisor code is required"
        every { messageSource.getMessage(messageKey, null, any()) } returns expectedMessage

        // When
        val exception = AdvisorValidationException(messageKey, messageSource)

        // Then
        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }
}
