package com.nivasafinance.features.person.enum

import com.nivasafinance.features.person.exception.InvalidIdentifierTypeException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("IdentifierType Enum Tests")
class IdentifierTypeTest {

    @Test
    @DisplayName("should have correct enum values")
    fun `enum should have correct values`() {
        val expectedValues = listOf("PAN", "VOTER", "AADHAR", "PASSPORT", "DRIVING_LICENSE")
        val actualValues = IdentifierType.values().map { it.name }

        assertEquals(expectedValues, actualValues)
    }

    @Test
    @DisplayName("should return true for valid identifier type")
    fun `isValid should return true for valid identifier type`() {
        assertTrue(IdentifierType.isValid("PAN"))
        assertTrue(IdentifierType.isValid("AADHAR"))
        assertTrue(IdentifierType.isValid("PASSPORT"))
    }

    @Test
    @DisplayName("should return false for invalid identifier type")
    fun `isValid should return false for invalid identifier type`() {
        assertFalse(IdentifierType.isValid("INVALID"))
        assertFalse(IdentifierType.isValid(""))
        assertFalse(IdentifierType.isValid(null))
    }

    @Test
    @DisplayName("should return correct identifier type from string")
    fun `fromString should return correct identifier type from string`() {
        assertEquals(IdentifierType.PAN, IdentifierType.fromString("PAN"))
        assertEquals(IdentifierType.AADHAR, IdentifierType.fromString("AADHAR"))
        assertEquals(IdentifierType.PASSPORT, IdentifierType.fromString("PASSPORT"))
        assertEquals(IdentifierType.VOTER, IdentifierType.fromString("VOTER"))
        assertEquals(IdentifierType.DRIVING_LICENSE, IdentifierType.fromString("DRIVING_LICENSE"))
    }

    @Test
    @DisplayName("should throw exception for invalid identifier type")
    fun `fromString should throw exception for invalid identifier type`() {
        assertThrows<InvalidIdentifierTypeException> {
            IdentifierType.fromString("INVALID")
        }
    }

    @Test
    @DisplayName("should return valid types as string")
    fun `getValidTypes should return valid types as string`() {
        val validTypes = IdentifierType.getValidTypes()
        val expectedTypes = "PAN, VOTER, AADHAR, PASSPORT, DRIVING_LICENSE"

        assertEquals(expectedTypes, validTypes)
    }

    @Test
    @DisplayName("should handle case sensitivity correctly")
    fun `should handle case sensitivity correctly`() {
        assertFalse(IdentifierType.isValid("pan"))
        assertFalse(IdentifierType.isValid("Pan"))
        assertTrue(IdentifierType.isValid("PAN"))
    }

    @Test
    @DisplayName("should handle empty string correctly")
    fun `should handle empty string correctly`() {
        assertFalse(IdentifierType.isValid(""))
        assertFalse(IdentifierType.isValid("   "))
    }

    @Test
    @DisplayName("should handle null value correctly")
    fun `should handle null value correctly`() {
        assertFalse(IdentifierType.isValid(null))
    }
}
