package com.nivasafinance.features.address.exception

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AddressNotFoundException Tests")
class AddressNotFoundExceptionTest {

    @Test
    @DisplayName("should create exception with message")
    fun `should create exception with message`() {
        // Given
        val message = "Address not found"

        // When
        val exception = AddressNotFoundException(message)

        // Then
        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create exception with id and message source")
    fun `should create exception with id and message source`() {
        // Given
        val addressId = UUID.randomUUID()
        val expectedMessage = "Address not found"
        val messageSource = mockk<MessageSource>()

        every {
            messageSource.getMessage(
                AddressNotFoundException.ADDRESS_ID_NOT_FOUND_KEY,
                arrayOf(addressId),
                any()
            )
        } returns expectedMessage

        // When
        val exception = AddressNotFoundException(addressId, messageSource)

        // Then
        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should have correct error key constant")
    fun `should have correct error key constant`() {
        // When & Then
        assertEquals("error.address.id.not.found", AddressNotFoundException.ADDRESS_ID_NOT_FOUND_KEY)
    }
}
