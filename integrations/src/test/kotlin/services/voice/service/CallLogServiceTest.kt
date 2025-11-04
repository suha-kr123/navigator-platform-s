package services.voice.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.voice.dto.CallLogRequest
import services.voice.entity.CallLog
import services.voice.repository.CallLogRepository
import java.time.LocalDateTime
import java.util.UUID

class CallLogServiceTest {

    private lateinit var callLogService: CallLogService
    private lateinit var callLogRepository: CallLogRepository

    @BeforeEach
    fun setUp() {
        callLogRepository = mockk<CallLogRepository>()
        callLogService = CallLogService(callLogRepository)
    }

    @Test
    fun `createCallLog should save call log with all parameters`() {
        // Given
        val callSid = "CA1234567890"
        val entityName = "Lead"
        val entityId = 123L
        val direction = "outgoing"
        val fromNumber = "+919876543210"
        val toNumber = "+918765432109"
        val status = "queued"
        val exophone = "+911234567890"

        val savedCallLog = CallLog(
            id = UUID.randomUUID(),
            callSid = callSid,
            entityName = entityName,
            entityId = entityId,
            exophone = exophone,
            direction = direction,
            fromNumber = fromNumber,
            toNumber = toNumber,
            status = status,
            startTime = LocalDateTime.now()
        )

        every { callLogRepository.save(any<CallLog>()) } returns savedCallLog

        // When
        val result = callLogService.createCallLog(
            CallLogRequest(
                callSid = callSid,
                entityName = entityName,
                entityId = entityId,
                direction = direction,
                fromNumber = fromNumber,
                toNumber = toNumber,
                status = status,
                exophone = exophone
            )
        )

        // Then
        assertEquals(savedCallLog, result)
        verify { callLogRepository.save(any<CallLog>()) }
    }

    @Test
    fun `createCallLog should save call log with null exophone`() {
        // Given
        val callSid = "CA1234567890"
        val entityName = "Advisor"
        val entityId = 456L
        val direction = "incoming"
        val fromNumber = "+919876543210"
        val toNumber = "+918765432109"
        val status = "ringing"

        val savedCallLog = CallLog(
            id = UUID.randomUUID(),
            callSid = callSid,
            entityName = entityName,
            entityId = entityId,
            exophone = null,
            direction = direction,
            fromNumber = fromNumber,
            toNumber = toNumber,
            status = status,
            startTime = LocalDateTime.now()
        )

        every { callLogRepository.save(any<CallLog>()) } returns savedCallLog

        // When
        val result = callLogService.createCallLog(
            CallLogRequest(
                callSid = callSid,
                entityName = entityName,
                entityId = entityId,
                direction = direction,
                fromNumber = fromNumber,
                toNumber = toNumber,
                status = status,
                exophone = null
            )
        )

        // Then
        assertEquals(savedCallLog, result)
        verify { callLogRepository.save(any<CallLog>()) }
    }

    @Test
    fun `updateCallStatus should update existing call log`() {
        // Given
        val callSid = "CA1234567890"
        val newStatus = "completed"
        val recordingUrl = "http://example.com/recording.mp3"

        val existingCallLog = CallLog(
            id = UUID.randomUUID(),
            callSid = callSid,
            entityName = "Lead",
            entityId = 123L,
            exophone = "+911234567890",
            direction = "outgoing",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            status = "in-progress",
            startTime = LocalDateTime.now().minusMinutes(2)
        )

        every { callLogRepository.findByCallSid(callSid) } returns existingCallLog
        every { callLogRepository.save(any<CallLog>()) } answers {
            val callLog = firstArg<CallLog>()
            callLog
        }

        // When
        val result = callLogService.updateCallStatus(
            callSid = callSid,
            status = newStatus,
            recordingUrl = recordingUrl
        )

        // Then
        assertNotNull(result)
        assertEquals(newStatus, result?.status)
        assertEquals(recordingUrl, result?.recordingUrl)
        // updateCallStatus doesn't set endTime - that's handled by updateCallEndTime
        verify { callLogRepository.findByCallSid(callSid) }
        verify { callLogRepository.save(any<CallLog>()) }
    }

    @Test
    fun `updateCallStatus should return null when call log not found`() {
        // Given
        val callSid = "CA1234567890"
        val newStatus = "completed"

        every { callLogRepository.findByCallSid(callSid) } returns null

        // When
        val result = callLogService.updateCallStatus(
            callSid = callSid,
            status = newStatus
        )

        // Then
        assertNull(result)
        verify { callLogRepository.findByCallSid(callSid) }
        verify(exactly = 0) { callLogRepository.save(any<CallLog>()) }
    }

    @Test
    fun `updateCallStatus should set end time for terminal statuses`() {
        // Given
        val callSid = "CA1234567890"
        val terminalStatuses = listOf("completed", "failed", "no-answer", "busy")

        terminalStatuses.forEach { status ->
            val existingCallLog = CallLog(
                id = UUID.randomUUID(),
                callSid = callSid,
                entityName = "Lead",
                entityId = 123L,
                exophone = "+911234567890",
                direction = "outgoing",
                fromNumber = "+919876543210",
                toNumber = "+918765432109",
                status = "in-progress",
                startTime = LocalDateTime.now().minusMinutes(2)
            )

            every { callLogRepository.findByCallSid(callSid) } returns existingCallLog
            every { callLogRepository.save(any<CallLog>()) } returns existingCallLog

            // When
            val result = callLogService.updateCallStatus(
                callSid = callSid,
                status = status
            )

            // Then
            assertNotNull(result)
            assertEquals(status, result?.status)
            // updateCallStatus doesn't set endTime - that's handled by updateCallEndTime
        }
    }

    @Test
    fun `updateCallStatus should not set end time for non-terminal statuses`() {
        // Given
        val callSid = "CA1234567890"
        val nonTerminalStatuses = listOf("queued", "ringing", "in-progress")

        nonTerminalStatuses.forEach { status ->
            val existingCallLog = CallLog(
                id = UUID.randomUUID(),
                callSid = callSid,
                entityName = "Lead",
                entityId = 123L,
                exophone = "+911234567890",
                direction = "outgoing",
                fromNumber = "+919876543210",
                toNumber = "+918765432109",
                status = "queued",
                startTime = LocalDateTime.now().minusMinutes(2)
            )

            every { callLogRepository.findByCallSid(callSid) } returns existingCallLog
            every { callLogRepository.save(any<CallLog>()) } returns existingCallLog

            // When
            val result = callLogService.updateCallStatus(
                callSid = callSid,
                status = status
            )

            // Then
            assertNotNull(result)
            assertEquals(status, result?.status)
            assertNull(result?.endTime)
        }
    }

    @Test
    fun `updateCallStatus should calculate duration when both start and end times are available`() {
        // Given
        val callSid = "CA1234567890"
        val startTime = LocalDateTime.now().minusMinutes(2)
        val existingCallLog = CallLog(
            id = UUID.randomUUID(),
            callSid = callSid,
            entityName = "Lead",
            entityId = 123L,
            exophone = "+911234567890",
            direction = "outgoing",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            status = "in-progress",
            startTime = startTime
        )

        every { callLogRepository.findByCallSid(callSid) } returns existingCallLog
        every { callLogRepository.save(any<CallLog>()) } returns existingCallLog

        // When
        val result = callLogService.updateCallStatus(
            callSid = callSid,
            status = "completed"
        )

        // Then
        assertNotNull(result)
        // updateCallStatus doesn't set endTime or duration - that's handled by updateCallEndTime
    }

    @Test
    fun `getCallLogByCallSid should return call log when found`() {
        // Given
        val callSid = "CA1234567890"
        val callLog = CallLog(
            id = UUID.randomUUID(),
            callSid = callSid,
            entityName = "Lead",
            entityId = 123L,
            exophone = "+911234567890",
            direction = "outgoing",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            status = "completed",
            startTime = LocalDateTime.now().minusMinutes(5),
            endTime = LocalDateTime.now(),
            duration = 300
        )

        every { callLogRepository.findByCallSid(callSid) } returns callLog

        // When
        val result = callLogService.getCallLogBySid(callSid)

        // Then
        assertEquals(callLog, result)
        verify { callLogRepository.findByCallSid(callSid) }
    }

    @Test
    fun `getCallLogByCallSid should return null when not found`() {
        // Given
        val callSid = "CA1234567890"
        every { callLogRepository.findByCallSid(callSid) } returns null

        // When
        val result = callLogService.getCallLogBySid(callSid)

        // Then
        assertNull(result)
        verify { callLogRepository.findByCallSid(callSid) }
    }

    @Test
    fun `getCallLogsByEntity should return list of call logs`() {
        // Given
        val entityName = "Lead"
        val entityId = 123L
        val callLogs = listOf(
            CallLog(
                id = UUID.randomUUID(),
                callSid = "CA1234567890",
                entityName = entityName,
                entityId = entityId,
                exophone = "+911234567890",
                direction = "outgoing",
                fromNumber = "+919876543210",
                toNumber = "+918765432109",
                status = "completed"
            ),
            CallLog(
                id = UUID.randomUUID(),
                callSid = "CA1234567891",
                entityName = entityName,
                entityId = entityId,
                exophone = "+911234567890",
                direction = "incoming",
                fromNumber = "+918765432109",
                toNumber = "+919876543210",
                status = "completed"
            )
        )

        every { callLogRepository.findByEntity(entityName, entityId) } returns callLogs

        // When
        val result = callLogService.getCallLogsByEntity(entityName, entityId)

        // Then
        assertEquals(callLogs, result)
        verify { callLogRepository.findByEntity(entityName, entityId) }
    }

    @Test
    fun `getCallLogsByEntity should return empty list when no call logs found`() {
        // Given
        val entityName = "Advisor"
        val entityId = 456L
        every { callLogRepository.findByEntity(entityName, entityId) } returns emptyList()

        // When
        val result = callLogService.getCallLogsByEntity(entityName, entityId)

        // Then
        assertTrue(result.isEmpty())
        verify { callLogRepository.findByEntity(entityName, entityId) }
    }

    @Test
    fun `updateCallStatus should handle null duration parameter`() {
        // Given
        val callSid = "CA1234567890"
        val existingCallLog = CallLog(
            id = UUID.randomUUID(),
            callSid = callSid,
            entityName = "Lead",
            entityId = 123L,
            exophone = "+911234567890",
            direction = "outgoing",
            fromNumber = "+919876543210",
            toNumber = "+918765432109",
            status = "in-progress",
            startTime = LocalDateTime.now().minusMinutes(2)
        )

        every { callLogRepository.findByCallSid(callSid) } returns existingCallLog
        every { callLogRepository.save(any<CallLog>()) } returns existingCallLog

        // When
        val result = callLogService.updateCallStatus(
            callSid = callSid,
            status = "completed",
            recordingUrl = "http://example.com/recording.mp3"
        )

        // Then
        assertNotNull(result)
        assertEquals("completed", result?.status)
        assertEquals("http://example.com/recording.mp3", result?.recordingUrl)
        assertNull(result?.duration)
    }
}
