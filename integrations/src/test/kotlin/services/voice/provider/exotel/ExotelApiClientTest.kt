package services.voice.provider.exotel

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.voice.dto.VoiceCallRequest
import services.voice.dto.VoiceCallResponse
import services.voice.provider.exotel.data.ExotelConfiguration

class ExotelApiClientTest {

    private lateinit var mockHttpClient: ExotelHttpClient
    private lateinit var apiClient: ExotelApiClient
    private lateinit var config: ExotelConfiguration
    private lateinit var request: VoiceCallRequest

    @BeforeEach
    fun setUp() {
        mockHttpClient = mockk<ExotelHttpClient>()
        apiClient = ExotelApiClient(mockHttpClient)

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
    fun `makeCall should delegate to httpClient and return response`() {
        // Given
        val expectedResponse = VoiceCallResponse(
            callSid = "CA1234567890",
            status = "queued",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockHttpClient.makeCall(config, request) } returns expectedResponse

        // When
        val result = apiClient.makeCall(config, request)

        // Then
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `getCallStatus should delegate to httpClient and return response`() {
        // Given
        val callSid = "CA1234567890"
        val expectedResponse = VoiceCallResponse(
            callSid = callSid,
            status = "completed",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = 120,
            recordingUrl = "http://example.com/recording.mp3",
            errorMessage = null
        )

        every { mockHttpClient.getCallStatus(config, callSid) } returns expectedResponse

        // When
        val result = apiClient.getCallStatus(config, callSid)

        // Then
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `makeCall should handle error response from httpClient`() {
        // Given
        val errorResponse = VoiceCallResponse(
            callSid = "",
            status = "failed",
            fromNumber = "",
            toNumber = "",
            duration = null,
            recordingUrl = null,
            errorMessage = "Invalid phone number"
        )

        every { mockHttpClient.makeCall(config, request) } returns errorResponse

        // When
        val result = apiClient.makeCall(config, request)

        // Then
        assertEquals(errorResponse, result)
        assertEquals("", result.callSid)
        assertEquals("failed", result.status)
        assertEquals("Invalid phone number", result.errorMessage)
    }

    @Test
    fun `getCallStatus should handle error response from httpClient`() {
        // Given
        val callSid = "CA1234567890"
        val errorResponse = VoiceCallResponse(
            callSid = callSid,
            status = "failed",
            fromNumber = "",
            toNumber = "",
            duration = null,
            recordingUrl = null,
            errorMessage = "Call not found"
        )

        every { mockHttpClient.getCallStatus(config, callSid) } returns errorResponse

        // When
        val result = apiClient.getCallStatus(config, callSid)

        // Then
        assertEquals(errorResponse, result)
        assertEquals(callSid, result.callSid)
        assertEquals("failed", result.status)
        assertEquals("Call not found", result.errorMessage)
    }

    @Test
    fun `makeCall should handle different phone number formats`() {
        // Given
        val internationalRequest = request.copy(
            fromNumber = "+1234567890",
            toNumber = "+1987654321"
        )
        val expectedResponse = VoiceCallResponse(
            callSid = "CA_INTERNATIONAL",
            status = "queued",
            fromNumber = "+1234567890",
            toNumber = "+1987654321",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockHttpClient.makeCall(config, internationalRequest) } returns expectedResponse

        // When
        val result = apiClient.makeCall(config, internationalRequest)

        // Then
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `getCallStatus should handle in-progress call`() {
        // Given
        val callSid = "CA_IN_PROGRESS"
        val inProgressResponse = VoiceCallResponse(
            callSid = callSid,
            status = "in-progress",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockHttpClient.getCallStatus(config, callSid) } returns inProgressResponse

        // When
        val result = apiClient.getCallStatus(config, callSid)

        // Then
        assertEquals(inProgressResponse, result)
        assertEquals("in-progress", result.status)
    }
}
