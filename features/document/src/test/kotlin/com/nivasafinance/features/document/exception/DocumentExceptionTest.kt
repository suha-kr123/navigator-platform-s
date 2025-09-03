package com.nivasafinance.features.document.exception

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.UUID

@DisplayName("Document Exception Tests")
class DocumentExceptionTest {

    private val messageSource = mockk<MessageSource>()

    @BeforeEach
    fun setUp() {
        // Mock MessageSource for all tests
        every { messageSource.getMessage(any(), any(), any()) } returns "Test error message"
    }

    @Nested
    @DisplayName("DocumentNotFoundException Tests")
    inner class DocumentNotFoundExceptionTests {

        @Test
        @DisplayName("Should create DocumentNotFoundException with document ID")
        fun `should create DocumentNotFoundException with document ID`() {
            val documentId = UUID.randomUUID()
            val exception = DocumentNotFoundException(documentId, messageSource)

            assertNotNull(exception)
            assertNotNull(exception.message)
        }

        @Test
        @DisplayName("Should create DocumentNotFoundException with different document IDs")
        fun `should create DocumentNotFoundException with different document IDs`() {
            val documentId1 = UUID.randomUUID()
            val documentId2 = UUID.randomUUID()

            val exception1 = DocumentNotFoundException(documentId1, messageSource)
            val exception2 = DocumentNotFoundException(documentId2, messageSource)

            assertNotNull(exception1)
            assertNotNull(exception2)
            assertNotNull(exception1.message)
            assertNotNull(exception2.message)
        }
    }

    @Nested
    @DisplayName("DocumentValidationException Tests")
    inner class DocumentValidationExceptionTests {

        @Test
        @DisplayName("Should create DocumentValidationException with message")
        fun `should create DocumentValidationException with message`() {
            val message = "File name cannot be empty"
            val exception = DocumentValidationException(message, messageSource)

            assertNotNull(exception)
            assertNotNull(exception.message)
        }

        @Test
        @DisplayName("Should create DocumentValidationException with different messages")
        fun `should create DocumentValidationException with different messages`() {
            val message1 = "File name cannot be empty"
            val message2 = "File size exceeds maximum limit"
            val message3 = "Invalid file type"

            val exception1 = DocumentValidationException(message1, messageSource)
            val exception2 = DocumentValidationException(message2, messageSource)
            val exception3 = DocumentValidationException(message3, messageSource)

            assertNotNull(exception1)
            assertNotNull(exception2)
            assertNotNull(exception3)
            assertNotNull(exception1.message)
            assertNotNull(exception2.message)
            assertNotNull(exception3.message)
        }

        @Test
        @DisplayName("Should create DocumentValidationException with empty message")
        fun `should create DocumentValidationException with empty message`() {
            val exception = DocumentValidationException("", messageSource)

            assertNotNull(exception)
            assertNotNull(exception.message)
        }

        @Test
        @DisplayName("Should create DocumentValidationException with specific message")
        fun `should create DocumentValidationException with specific message`() {
            val message = "test message"
            val exception = DocumentValidationException(message, messageSource)

            assertNotNull(exception)
            assertNotNull(exception.message)
        }
    }
}
