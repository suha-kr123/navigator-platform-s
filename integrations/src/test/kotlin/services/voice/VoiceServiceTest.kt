package services.voice

import framework.ServiceFactory
import framework.config.BusinessContext
import framework.config.ThirdPartyServiceList
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.voice.dto.CallLogRequest
import services.voice.dto.VoiceCallRequest
import services.voice.dto.VoiceCallResponse
import services.voice.service.CallLogService

class VoiceServiceTest {

    private lateinit var voiceService: VoiceService
    private lateinit var serviceFactory: ServiceFactory<VoiceHandler>
    private lateinit var callLogService: CallLogService
    private lateinit var mockVoiceHandler: VoiceHandler
    private lateinit var businessContext: BusinessContext

    @BeforeEach
    fun setUp() {
        serviceFactory = mockk<ServiceFactory<VoiceHandler>>()
        callLogService = mockk<CallLogService>()
        mockVoiceHandler = mockk<VoiceHandler>()
        voiceService = VoiceService(serviceFactory, callLogService)

        businessContext = BusinessContext(
            entityName = "Lead",
            entityId = "123",
            businessPurpose = "Test call"
        )

        every { serviceFactory.getHandler(ThirdPartyServiceList.VOICE) } returns mockVoiceHandler
        every { callLogService.createCallLog(any()) } returns mockk()
        every { callLogService.updateCallStatus(any(), any(), any()) } returns mockk()
    }

    @Test
    fun `makeCall should delegate to voice handler and return response`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            exophone = "+911234567890",
            callPurpose = "Test call",
            entityName = "Lead",
            entityId = 123L
        )

        val expectedResponse = VoiceCallResponse(
            callSid = "CA1234567890",
            status = "queued",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockVoiceHandler.makeCall(request, businessContext) } returns expectedResponse

        // When
        val result = voiceService.makeCall(request, businessContext)

        // Then
        assertEquals(expectedResponse, result)
        verify { serviceFactory.getHandler(ThirdPartyServiceList.VOICE) }
        verify { mockVoiceHandler.makeCall(request, businessContext) }
    }

    @Test
    fun `makeCall should create call log when entity information is provided`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            exophone = "+911234567890",
            callPurpose = "Test call",
            entityName = "Lead",
            entityId = 123L
        )

        val response = VoiceCallResponse(
            callSid = "CA1234567890",
            status = "queued",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockVoiceHandler.makeCall(request, businessContext) } returns response
        every { callLogService.createCallLog(any()) } returns mockk()

        // When
        voiceService.makeCall(request, businessContext)

        // Then
        verify {
            callLogService.createCallLog(
                CallLogRequest(
                    callSid = "CA1234567890",
                    entityName = "Lead",
                    entityId = 123L,
                    direction = "outgoing",
                    fromNumber = "+919876543210",
                    toNumber = "+918765432109",
                    status = "queued",
                    exophone = "+911234567890"
                )
            )
        }
    }

    @Test
    fun `makeCall should not create call log when entity information is missing`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "+919876543210",
            toNumber = "+918765432109"
        )

        val response = VoiceCallResponse(
            callSid = "CA1234567890",
            status = "queued",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockVoiceHandler.makeCall(request, businessContext) } returns response

        // When
        voiceService.makeCall(request, businessContext)

        // Then
        verify(exactly = 0) { callLogService.createCallLog(any()) }
    }

    @Test
    fun `makeCall should not create call log when entity name is null`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            entityId = 123L
        )

        val response = VoiceCallResponse(
            callSid = "CA1234567890",
            status = "queued",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockVoiceHandler.makeCall(request, businessContext) } returns response

        // When
        voiceService.makeCall(request, businessContext)

        // Then
        verify(exactly = 0) { callLogService.createCallLog(any()) }
    }

    @Test
    fun `makeCall should not create call log when entity id is null`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            entityName = "Lead"
        )

        val response = VoiceCallResponse(
            callSid = "CA1234567890",
            status = "queued",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockVoiceHandler.makeCall(request, businessContext) } returns response

        // When
        voiceService.makeCall(request, businessContext)

        // Then
        verify(exactly = 0) { callLogService.createCallLog(any()) }
    }

    @Test
    fun `makeCall should handle call log creation exception gracefully`() {
        // Given
        val request = VoiceCallRequest(
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            exophone = "+911234567890",
            callPurpose = "Test call",
            entityName = "Lead",
            entityId = 123L
        )

        val response = VoiceCallResponse(
            callSid = "CA1234567890",
            status = "queued",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockVoiceHandler.makeCall(request, businessContext) } returns response
        every {
            callLogService.createCallLog(any())
        } throws RuntimeException("Database error")

        // When
        val result = voiceService.makeCall(request, businessContext)

        // Then
        assertEquals(response, result) // Should still return the response despite call log error
        verify { callLogService.createCallLog(any()) }
    }

    @Test
    fun `getCallStatus should delegate to voice handler and return response`() {
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

        every { mockVoiceHandler.getCallStatus(callSid, businessContext) } returns expectedResponse

        // When
        val result = voiceService.getCallStatus(callSid, businessContext)

        // Then
        assertEquals(expectedResponse, result)
        verify { serviceFactory.getHandler(ThirdPartyServiceList.VOICE) }
        verify { mockVoiceHandler.getCallStatus(callSid, businessContext) }
    }

    @Test
    fun `getCallStatus should update call log when call sid is not empty`() {
        // Given
        val callSid = "CA1234567890"
        val response = VoiceCallResponse(
            callSid = callSid,
            status = "completed",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = 120,
            recordingUrl = "http://example.com/recording.mp3",
            errorMessage = null
        )

        every { mockVoiceHandler.getCallStatus(callSid, businessContext) } returns response
        every { callLogService.updateCallStatus(any(), any(), any()) } returns mockk()

        // When
        voiceService.getCallStatus(callSid, businessContext)

        // Then
        verify {
            callLogService.updateCallStatus(
                callSid = callSid,
                status = "completed",
                recordingUrl = "http://example.com/recording.mp3"
            )
        }
    }

    @Test
    fun `getCallStatus should not update call log when call sid is empty`() {
        // Given
        val callSid = "CA1234567890"
        val response = VoiceCallResponse(
            callSid = "", // Empty call sid
            status = "failed",
            fromNumber = "",
            toNumber = "",
            duration = null,
            recordingUrl = null,
            errorMessage = "Invalid call"
        )

        every { mockVoiceHandler.getCallStatus(callSid, businessContext) } returns response

        // When
        voiceService.getCallStatus(callSid, businessContext)

        // Then
        verify(exactly = 0) { callLogService.updateCallStatus(any(), any(), any()) }
    }

    @Test
    fun `getCallStatus should handle call log update exception gracefully`() {
        // Given
        val callSid = "CA1234567890"
        val response = VoiceCallResponse(
            callSid = callSid,
            status = "completed",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = 120,
            recordingUrl = "http://example.com/recording.mp3",
            errorMessage = null
        )

        every { mockVoiceHandler.getCallStatus(callSid, businessContext) } returns response
        every { callLogService.updateCallStatus(any(), any(), any()) } throws RuntimeException("Database error")

        // When
        val result = voiceService.getCallStatus(callSid, businessContext)

        // Then
        assertEquals(response, result) // Should still return the response despite call log error
        verify { callLogService.updateCallStatus(any(), any(), any()) }
    }

    @Test
    fun `getCallStatus should handle null recording url`() {
        // Given
        val callSid = "CA1234567890"
        val response = VoiceCallResponse(
            callSid = callSid,
            status = "in-progress",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockVoiceHandler.getCallStatus(callSid, businessContext) } returns response
        every { callLogService.updateCallStatus(any(), any(), any()) } returns mockk()

        // When
        voiceService.getCallStatus(callSid, businessContext)

        // Then
        verify {
            callLogService.updateCallStatus(
                callSid = callSid,
                status = "in-progress",
                recordingUrl = null
            )
        }
    }

    @Test
    fun `getCallStatus should handle null duration`() {
        // Given
        val callSid = "CA1234567890"
        val response = VoiceCallResponse(
            callSid = callSid,
            status = "ringing",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            duration = null,
            recordingUrl = null,
            errorMessage = null
        )

        every { mockVoiceHandler.getCallStatus(callSid, businessContext) } returns response
        every { callLogService.updateCallStatus(any(), any(), any()) } returns mockk()

        // When
        voiceService.getCallStatus(callSid, businessContext)

        // Then
        verify {
            callLogService.updateCallStatus(
                callSid = callSid,
                status = "ringing",
                recordingUrl = null
            )
        }
    }
}
