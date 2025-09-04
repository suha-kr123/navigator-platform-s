package com.nivasafinance.features.person.exception

import com.nivasafinance.features.person.enum.IdentifierType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.MessageSource
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("Person Exception Tests")
class PersonExceptionTest {

    @Test
    @DisplayName("should create PersonNotFoundException with message")
    fun `PersonNotFoundException should create with message`() {
        val message = "Person not found"
        val exception = PersonNotFoundException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create PersonNotFoundException with id and message source")
    fun `PersonNotFoundException should create with id and message source`() {
        val personId = UUID.randomUUID()
        val messageSource = mockk<MessageSource>()
        val expectedMessage = "Person with id $personId not found"

        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        val exception = PersonNotFoundException(personId, messageSource)

        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should create InvalidIdentifierTypeException with message")
    fun `InvalidIdentifierTypeException should create with message`() {
        val message = "Invalid identifier type"
        val exception = InvalidIdentifierTypeException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create InvalidIdentifierTypeException with valid types")
    fun `InvalidIdentifierTypeException should create with valid types`() {
        val invalidType = "INVALID_TYPE"
        val messageSource = mockk<MessageSource>()
        val expectedMessage = "Invalid identifier type: $invalidType. Valid types are: " +
            "${IdentifierType.values().joinToString(", ")}"

        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        val exception = InvalidIdentifierTypeException.withValidTypes(invalidType, messageSource)

        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should create InvalidIdentifierTypeException for enum")
    fun `InvalidIdentifierTypeException should create for enum`() {
        val invalidType = "INVALID_TYPE"
        val expectedMessage = "Invalid identifier type: $invalidType. Valid types are: " +
            "${IdentifierType.values().joinToString(", ")}"

        val exception = InvalidIdentifierTypeException.forEnum(invalidType)

        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should create DuplicateIdentifierTypeException with message")
    fun `DuplicateIdentifierTypeException should create with message`() {
        val message = "Duplicate identifier type"
        val exception = DuplicateIdentifierTypeException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create PersonIdentifierNotFoundException with message")
    fun `PersonIdentifierNotFoundException should create with message`() {
        val message = "Person identifier not found"
        val exception = PersonIdentifierNotFoundException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create PersonAddressMappingNotFoundException with message")
    fun `PersonAddressMappingNotFoundException should create with message`() {
        val message = "Person address mapping not found"
        val exception = PersonAddressMappingNotFoundException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create InvalidMobileNumberException with message")
    fun `InvalidMobileNumberException should create with message`() {
        val message = "Invalid mobile number"
        val exception = InvalidMobileNumberException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create PrimaryMobileNumberAlreadyExistsException with message")
    fun `PrimaryMobileNumberAlreadyExistsException should create with message`() {
        val message = "Primary mobile number already exists"
        val exception = PrimaryMobileNumberAlreadyExistsException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create DuplicatePrimaryMobileNumberException with message")
    fun `DuplicatePrimaryMobileNumberException should create with message`() {
        val message = "Duplicate primary mobile number"
        val exception = DuplicatePrimaryMobileNumberException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create InvalidAddressTypeException with message")
    fun `InvalidAddressTypeException should create with message`() {
        val message = "Invalid address type"
        val exception = InvalidAddressTypeException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create EmploymentDetailsNotFoundException with message")
    fun `EmploymentDetailsNotFoundException should create with message`() {
        val message = "Employment details not found"
        val exception = EmploymentDetailsNotFoundException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create EmploymentDetailsNotFoundException with personId and message source")
    fun `EmploymentDetailsNotFoundException should create with personId and message source`() {
        val personId = UUID.randomUUID()
        val messageSource = mockk<MessageSource>()
        val expectedMessage = "Employment details not found for person with id $personId"

        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        val exception = EmploymentDetailsNotFoundException(personId, messageSource)

        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("should create EmploymentDetailsAlreadyExistsException with message")
    fun `EmploymentDetailsAlreadyExistsException should create with message`() {
        val message = "Employment details already exist"
        val exception = EmploymentDetailsAlreadyExistsException(message)

        assertNotNull(exception)
        assertEquals(message, exception.message)
    }

    @Test
    @DisplayName("should create EmploymentDetailsAlreadyExistsException with personId and message source")
    fun `EmploymentDetailsAlreadyExistsException should create with personId and message source`() {
        val personId = UUID.randomUUID()
        val messageSource = mockk<MessageSource>()
        val expectedMessage = "Employment details already exist for person with id $personId"

        every { messageSource.getMessage(any(), any(), any()) } returns expectedMessage

        val exception = EmploymentDetailsAlreadyExistsException(personId, messageSource)

        assertNotNull(exception)
        assertEquals(expectedMessage, exception.message)
    }


    @Test
    @DisplayName("should verify EmploymentDetailsNotFoundException has correct key")
    fun `EmploymentDetailsNotFoundException should have correct key`() {
        assertEquals("error.employment.details.not.found", EmploymentDetailsNotFoundException.KEY)
    }

    @Test
    @DisplayName("should verify EmploymentDetailsAlreadyExistsException has correct key")
    fun `EmploymentDetailsAlreadyExistsException should have correct key`() {
        assertEquals("error.employment.details.already.exists", EmploymentDetailsAlreadyExistsException.KEY)
    }
}
