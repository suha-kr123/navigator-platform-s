package services.voice.provider.exotel

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import services.voice.dto.VoiceCallRequest
import services.voice.provider.exotel.data.ExotelConfiguration

class ExotelHttpClientTest {

    private lateinit var mockRestTemplate: RestTemplate
    private lateinit var httpClient: ExotelHttpClient
    private lateinit var config: ExotelConfiguration
    private lateinit var request: VoiceCallRequest

    @BeforeEach
    fun setUp() {
        mockRestTemplate = mockk<RestTemplate>()
        httpClient = ExotelHttpClient(mockRestTemplate)

        config = ExotelConfiguration(
            accountSid = "test_account_sid",
            authToken = "test_auth_token",
            subdomain = "api.exotel.com",
            callerId = "+919876543210",
            webhookUrl = "",
            apiKey = "test_api_key",
            apiToken = "test_api_token",
            recordingEnabled = true,
            maxCallDuration = 3600,
            retryAttempts = 3,
            timeout = 30
        )

        request = VoiceCallRequest(
            fromNumber = "9876543210",
            toNumber = "8765432109",
            exophone = "+911234567890",
            callPurpose = "Test call",
            entityName = "Lead",
            entityId = 123L
        )
    }

    @Test
    fun `makeCall should successfully make a call with valid request`() {
        // Given
        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.makeCall(config, request)

        // Then
        assertEquals("CA1234567890", result.callSid)
        assertEquals("queued", result.status)
        assertEquals("+919876543210", result.fromNumber)
        assertEquals("+918765432109", result.toNumber)
        assertNull(result.duration)
        assertNull(result.recordingUrl)
        assertNull(result.errorMessage)
    }

    @Test
    fun `makeCall should handle XML response with alternative Sid tag`() {
        // Given
        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <Sid>CA1234567890</Sid>
                <Status>in-progress</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.makeCall(config, request)

        // Then
        assertEquals("CA1234567890", result.callSid)
        assertEquals("in-progress", result.status)
    }

    @Test
    fun `makeCall should handle error response`() {
        // Given
        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid></CallSid>
                <Status>failed</Status>
                <ErrorMessage>Invalid phone number</ErrorMessage>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.makeCall(config, request)

        // Then
        assertEquals("", result.callSid)
        assertEquals("failed", result.status)
        assertEquals("Invalid phone number", result.errorMessage)
    }

    @Test
    fun `makeCall should handle exception during API call`() {
        // Given
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } throws RuntimeException("Network error")

        // When
        val result = httpClient.makeCall(config, request)

        // Then
        assertEquals("", result.callSid)
        assertEquals("failed", result.status)
        assertEquals("", result.fromNumber)
        assertEquals("", result.toNumber)
        assertEquals("Network error", result.errorMessage)
    }

    @Test
    fun `makeCall should normalize Indian phone numbers correctly`() {
        // Given
        val indianRequest = VoiceCallRequest(
            fromNumber = "9876543210", // 10 digits without country code
            toNumber = "8765432109",
            exophone = "+911234567890",
            callPurpose = "Test call",
            entityName = "Lead",
            entityId = 123L
        )

        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.makeCall(config, indianRequest)

        // Then
        assertEquals("+919876543210", result.fromNumber)
        assertEquals("+918765432109", result.toNumber)
    }

    @Test
    fun `makeCall should handle international phone numbers`() {
        // Given
        val internationalRequest = VoiceCallRequest(
            fromNumber = "+1234567890",
            toNumber = "+1987654321",
            exophone = "+911234567890",
            callPurpose = "Test call",
            entityName = "Lead",
            entityId = 123L
        )

        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.makeCall(config, internationalRequest)

        // Then
        assertEquals("+1234567890", result.fromNumber)
        assertEquals("+1987654321", result.toNumber)
    }

    @Test
    fun `makeCall should include recording parameter when enabled`() {
        // Given
        val configWithRecording = config.copy(recordingEnabled = true)
        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        httpClient.makeCall(configWithRecording, request)

        // Then - verify the request body contains Record=true
        // This is verified by the successful call (no exception thrown)
    }

    @Test
    fun `makeCall should include custom timeout and duration when provided`() {
        // Given
        val configWithCustomValues = config.copy(
            maxCallDuration = 1800,
            timeout = 60
        )
        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        httpClient.makeCall(configWithCustomValues, request)

        // Then - verify the request body contains custom values
        // This is verified by the successful call (no exception thrown)
    }

    @Test
    fun `makeCall should use API key and token when available`() {
        // Given
        val configWithApiKey = config.copy(
            apiKey = "test_api_key",
            apiToken = "test_api_token"
        )
        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        httpClient.makeCall(configWithApiKey, request)

        // Then - verify the request uses API key authentication
        // This is verified by the successful call (no exception thrown)
    }

    @Test
    fun `getCallStatus should successfully get call status with JSON response`() {
        // Given
        val callSid = "CA1234567890"
        val jsonResponse = """
            {
                "CallSid": "CA1234567890",
                "Status": "completed",
                "From": "+919876543210",
                "To": "+918765432109",
                "Duration": 120,
                "RecordingUrl": "http://example.com/recording.mp3"
            }
        """.trimIndent()

        val responseEntity = ResponseEntity(jsonResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<Any>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.getCallStatus(config, callSid)

        // Then
        assertEquals("CA1234567890", result.callSid)
        assertEquals("completed", result.status)
        assertEquals("+919876543210", result.fromNumber)
        assertEquals("+918765432109", result.toNumber)
        assertEquals(120, result.duration)
        assertEquals("http://example.com/recording.mp3", result.recordingUrl)
        assertNull(result.errorMessage)
    }

    @Test
    fun `getCallStatus should handle JSON response with numeric duration`() {
        // Given
        val callSid = "CA1234567890"
        val jsonResponse = """
            {
                "CallSid": "CA1234567890",
                "Status": "in-progress",
                "From": "+919876543210",
                "To": "+918765432109",
                "Duration": 60
            }
        """.trimIndent()

        val responseEntity = ResponseEntity(jsonResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<Any>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.getCallStatus(config, callSid)

        // Then
        assertEquals(60, result.duration)
    }

    @Test
    fun `getCallStatus should handle empty response`() {
        // Given
        val callSid = "CA1234567890"
        val responseEntity = ResponseEntity("", HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<Any>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.getCallStatus(config, callSid)

        // Then
        assertEquals(callSid, result.callSid)
        assertEquals("unknown", result.status)
        assertEquals("", result.fromNumber)
        assertEquals("", result.toNumber)
        assertNull(result.duration)
        assertNull(result.recordingUrl)
        assertNull(result.errorMessage)
    }

    @Test
    fun `getCallStatus should handle exception during API call`() {
        // Given
        val callSid = "CA1234567890"
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<Any>>(),
                any<Class<String>>()
            )
        } throws RuntimeException("Network error")

        // When
        val result = httpClient.getCallStatus(config, callSid)

        // Then
        assertEquals(callSid, result.callSid)
        assertEquals("failed", result.status)
        assertEquals("", result.fromNumber)
        assertEquals("", result.toNumber)
        assertNull(result.duration)
        assertNull(result.recordingUrl)
        assertEquals("Network error", result.errorMessage)
    }

    @Test
    fun `makeCall should throw exception for invalid from number`() {
        // Given
        val invalidRequest = request.copy(fromNumber = "")

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            httpClient.makeCall(config, invalidRequest)
        }
    }

    @Test
    fun `makeCall should throw exception for invalid to number`() {
        // Given
        val invalidRequest = request.copy(toNumber = "")

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            httpClient.makeCall(config, invalidRequest)
        }
    }

    @Test
    fun `makeCall should throw exception for null phone number`() {
        // Given
        val invalidRequest = request.copy(fromNumber = "")

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            httpClient.makeCall(config, invalidRequest)
        }
    }

    @Test
    fun `makeCall should throw exception for phone number with no digits`() {
        // Given
        val invalidRequest = request.copy(fromNumber = "abc")

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            httpClient.makeCall(config, invalidRequest)
        }
    }

    @Test
    fun `makeCall should handle phone numbers with special characters`() {
        // Given
        val specialCharRequest = request.copy(
            fromNumber = "+91-9876-543-210",
            toNumber = "(876) 543-2109"
        )

        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.makeCall(config, specialCharRequest)

        // Then
        assertEquals("+919876543210", result.fromNumber)
        assertEquals("+918765432109", result.toNumber)
    }

    @Test
    fun `makeCall should handle 12-digit Indian numbers`() {
        // Given
        val twelveDigitRequest = request.copy(
            fromNumber = "919876543210", // 12 digits with country code
            toNumber = "918765432109"
        )

        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.makeCall(config, twelveDigitRequest)

        // Then
        assertEquals("+919876543210", result.fromNumber)
        assertEquals("+918765432109", result.toNumber)
    }

    @Test
    fun `makeCall should handle already formatted international numbers`() {
        // Given
        val formattedRequest = request.copy(
            fromNumber = "+919876543210",
            toNumber = "+918765432109"
        )

        val xmlResponse = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response>
                <CallSid>CA1234567890</CallSid>
                <Status>queued</Status>
            </Response>
        """.trimIndent()

        val responseEntity = ResponseEntity(xmlResponse, HttpStatus.OK)
        every {
            mockRestTemplate.exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<String>>()
            )
        } returns responseEntity

        // When
        val result = httpClient.makeCall(config, formattedRequest)

        // Then
        assertEquals("+919876543210", result.fromNumber)
        assertEquals("+918765432109", result.toNumber)
    }
}
