package services.voice.provider.exotel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExotelApiClientUnitTest {

    @Test
    fun `test phone number normalization for Indian numbers`() {
        // Test the private normalizePhoneNumber method using reflection
        val httpClient = ExotelHttpClient()
        val normalizeMethod = httpClient.javaClass.getDeclaredMethod("normalizePhoneNumber", String::class.java)
        normalizeMethod.isAccessible = true

        // Test cases
        val testCases = mapOf(
            "9876543210" to "+919876543210", // 10 digit Indian number
            "919876543210" to "+919876543210", // 12 digit with country code
            "+919876543210" to "+919876543210", // Already formatted
            "919876543210" to "+919876543210", // 12 digit without +
            "8765432109" to "+918765432109" // Another 10 digit
        )

        testCases.forEach { (input, expected) ->
            val result = normalizeMethod.invoke(httpClient, input) as String
            assertEquals(expected, result, "Failed for input: $input")
        }
    }

    @Test
    fun `test phone number normalization for international numbers`() {
        val httpClient = ExotelHttpClient()
        val normalizeMethod = httpClient.javaClass.getDeclaredMethod("normalizePhoneNumber", String::class.java)
        normalizeMethod.isAccessible = true

        val testCases = mapOf(
            "+1234567890" to "+1234567890", // Already formatted international
            "1234567890" to "+1234567890", // International without +
            "+44123456789" to "+44123456789" // UK number
        )

        testCases.forEach { (input, expected) ->
            val result = normalizeMethod.invoke(httpClient, input) as String
            assertEquals(expected, result, "Failed for input: $input")
        }
    }

    @Test
    fun `test phone number validation`() {
        val httpClient = ExotelHttpClient()
        val isValidMethod = httpClient.javaClass.getDeclaredMethod("isValidPhoneNumber", String::class.java)
        isValidMethod.isAccessible = true

        val validNumbers = listOf(
            "+919876543210",
            "+1234567890",
            "+44123456789"
        )

        val invalidNumbers = listOf(
            "9876543210", // Missing +
            "123", // Too short
            "+12345678901234567890", // Too long
            "abc", // Non-numeric
            "" // Empty
        )

        validNumbers.forEach { number ->
            val result = isValidMethod.invoke(httpClient, number) as Boolean
            assertTrue(result, "Should be valid: $number")
        }

        invalidNumbers.forEach { number ->
            val result = isValidMethod.invoke(httpClient, number) as Boolean
            assertFalse(result, "Should be invalid: $number")
        }
    }

    @Test
    fun `test caller ID normalization`() {
        val httpClient = ExotelHttpClient()
        val normalizeCallerIdMethod = httpClient.javaClass.getDeclaredMethod("normalizeCallerId", String::class.java)
        normalizeCallerIdMethod.isAccessible = true

        val testCases = mapOf(
            "+91-9876-543-210" to "+919876543210",
            "(987) 654-3210" to "9876543210",
            "9876-543-210" to "9876543210",
            "+919876543210" to "+919876543210"
        )

        testCases.forEach { (input, expected) ->
            val result = normalizeCallerIdMethod.invoke(httpClient, input) as String
            assertEquals(expected, result, "Failed for input: $input")
        }
    }

    @Test
    fun `test XML value extraction`() {
        val httpClient = ExotelHttpClient()
        val extractXmlMethod = httpClient.javaClass.getDeclaredMethod(
            "extractXmlValue",
            String::class.java,
            String::class.java
        )
        extractXmlMethod.isAccessible = true

        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
                <ErrorMessage>Test error</ErrorMessage>
            </Response>
        """.trimIndent()

        val callSid = extractXmlMethod.invoke(httpClient, xml, "CallSid") as String?
        val status = extractXmlMethod.invoke(httpClient, xml, "Status") as String?
        val errorMessage = extractXmlMethod.invoke(httpClient, xml, "ErrorMessage") as String?
        val nonExistent = extractXmlMethod.invoke(httpClient, xml, "NonExistent") as String?

        assertEquals("CA1234567890", callSid)
        assertEquals("queued", status)
        assertEquals("Test error", errorMessage)
        assertNull(nonExistent)
    }

    @Test
    fun `test JSON value extraction`() {
        val httpClient = ExotelHttpClient()
        val extractJsonMethod = httpClient.javaClass.getDeclaredMethod(
            "extractJsonValue",
            String::class.java,
            String::class.java
        )
        extractJsonMethod.isAccessible = true

        val json = """
            {
                "CallSid": "CA1234567890",
                "Status": "completed",
                "Duration": 120,
                "RecordingUrl": "http://example.com/recording.mp3"
            }
        """.trimIndent()

        val callSid = extractJsonMethod.invoke(httpClient, json, "CallSid") as String?
        val status = extractJsonMethod.invoke(httpClient, json, "Status") as String?
        val duration = extractJsonMethod.invoke(httpClient, json, "Duration") as String?
        val recordingUrl = extractJsonMethod.invoke(httpClient, json, "RecordingUrl") as String?
        val nonExistent = extractJsonMethod.invoke(httpClient, json, "NonExistent") as String?

        assertEquals("CA1234567890", callSid)
        assertEquals("completed", status)
        assertEquals("120", duration)
        assertEquals("http://example.com/recording.mp3", recordingUrl)
        assertNull(nonExistent)
    }

    @Test
    fun `test phone number encoding`() {
        val httpClient = ExotelHttpClient()
        val encodeMethod = httpClient.javaClass.getDeclaredMethod("encodePhoneNumber", String::class.java)
        encodeMethod.isAccessible = true

        val testCases = mapOf(
            "+919876543210" to "%2B919876543210", // URL encoded
            "+91-9876-543-210" to "%2B919876543210", // URL encoded
            "(987) 654-3210" to "9876543210"
        )

        testCases.forEach { (input, expected) ->
            val result = encodeMethod.invoke(httpClient, input) as String
            assertEquals(expected, result, "Failed for input: $input")
        }
    }

    @Test
    fun `test basic auth generation`() {
        val httpClient = ExotelHttpClient()
        val getBasicAuthMethod = httpClient.javaClass.getDeclaredMethod(
            "getBasicAuth",
            String::class.java,
            String::class.java
        )
        getBasicAuthMethod.isAccessible = true

        val result = getBasicAuthMethod.invoke(httpClient, "test_user", "test_pass") as String
        assertNotNull(result)
        assertTrue(result.length > 0)
    }

    @Test
    fun `test Indian mobile number detection`() {
        val httpClient = ExotelHttpClient()
        val isIndianMethod = httpClient.javaClass.getDeclaredMethod("isIndianMobileNumber", String::class.java)
        isIndianMethod.isAccessible = true

        val indianNumbers = listOf("9876543210", "8765432109", "6123456789")
        val nonIndianNumbers = listOf("1234567890", "5123456789", "12345678901")

        indianNumbers.forEach { number ->
            val result = isIndianMethod.invoke(httpClient, number) as Boolean
            assertTrue(result, "Should be Indian mobile: $number")
        }

        nonIndianNumbers.forEach { number ->
            val result = isIndianMethod.invoke(httpClient, number) as Boolean
            assertFalse(result, "Should not be Indian mobile: $number")
        }
    }

    @Test
    fun `test Indian number normalization`() {
        val httpClient = ExotelHttpClient()
        val normalizeIndianMethod = httpClient.javaClass.getDeclaredMethod("normalizeIndianNumber", String::class.java)
        normalizeIndianMethod.isAccessible = true

        val testCases = mapOf(
            "9876543210" to "+919876543210", // 10 digits
            "919876543210" to "+919876543210", // 12 digits
            "+919876543210" to "+919876543210" // Already formatted
        )

        testCases.forEach { (input, expected) ->
            val result = normalizeIndianMethod.invoke(httpClient, input) as String
            assertEquals(expected, result, "Failed for input: $input")
        }
    }
}
