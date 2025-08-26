package com.nivasafinance.features.master.pincode.exception

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("PincodeNotFoundException Tests")
class PincodeNotFoundExceptionTest {

    @Test
    @DisplayName("should create exception with message")
    fun `should create exception with message`() {
        // Given
        val message = "Pincode not found"

        // When
        val exception = PincodeNotFoundException(message)

        // Then
        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create exception with pincode and message source")
    fun `should create exception with pincode and message source`() {
        // Given
        val pincode = "123456"
        val expectedMessage = "Pincode 123456 not found"
        val messageSource = mockk<MessageSource>()
        
        every { 
            messageSource.getMessage(
                PincodeNotFoundException.PINCODE_NOT_FOUND_KEY,
                arrayOf(pincode),
                any()
            ) 
        } returns expectedMessage

        // When
        val exception = PincodeNotFoundException(pincode, messageSource)

        // Then
        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should have correct error key constant")
    fun `should have correct error key constant`() {
        // When & Then
        assertEquals("error.pincode.not.found", PincodeNotFoundException.PINCODE_NOT_FOUND_KEY)
    }
}
