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

class CallLogServiceAdditionalTest {

    private lateinit var callLogService: CallLogService
    private lateinit var callLogRepository: CallLogRepository

    @BeforeEach
    fun setUp() {
        callLogRepository = mockk<CallLogRepository>()
        callLogService = CallLogService(callLogRepository)
    }

    @Test
    fun `createCallLog should handle all fields correctly`() {
        // Given
        val callSid = "CA1234567890"
        val entityName = "Advisor"
        val entityId = 456L
        val direction = "incoming"
        val fromNumber = "+919876543210"
        val toNumber = "+918765432109"
        val status = "ringing"
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
    fun `updateCallStatus should handle null recording url`() {
        // Given
        val callSid = "CA1234567890"
        val newStatus = "in-progress"
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
        every { callLogRepository.save(any<CallLog>()) } answers {
            val callLog = firstArg<CallLog>()
            callLog
        }

        // When
        val result = callLogService.updateCallStatus(
            callSid = callSid,
            status = newStatus,
            recordingUrl = null
        )

        // Then
        assertNotNull(result)
        assertEquals(newStatus, result?.status)
        assertNull(result?.recordingUrl)
        verify { callLogRepository.findByCallSid(callSid) }
        verify { callLogRepository.save(any<CallLog>()) }
    }

    @Test
    fun `updateCallStatus should handle empty recording url`() {
        // Given
        val callSid = "CA1234567890"
        val newStatus = "completed"
        val recordingUrl = ""
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
        verify { callLogRepository.findByCallSid(callSid) }
        verify { callLogRepository.save(any<CallLog>()) }
    }

    @Test
    fun `getCallLogBySid should return call log when found`() {
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
    fun `getCallLogBySid should return null when not found`() {
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
        val entityName = "Advisor"
        val entityId = 456L
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
        val entityName = "Applicant"
        val entityId = 789L
        every { callLogRepository.findByEntity(entityName, entityId) } returns emptyList()

        // When
        val result = callLogService.getCallLogsByEntity(entityName, entityId)

        // Then
        assertTrue(result.isEmpty())
        verify { callLogRepository.findByEntity(entityName, entityId) }
    }

    @Test
    fun `getCallLogsByPhoneNumber should return call logs for phone number`() {
        // Given
        val phoneNumber = "+919876543210"
        val callLogs = listOf(
            CallLog(
                id = UUID.randomUUID(),
                callSid = "CA1234567890",
                entityName = "Lead",
                entityId = 123L,
                exophone = "+911234567890",
                direction = "outgoing",
                fromNumber = phoneNumber,
                toNumber = "+918765432109",
                status = "completed"
            ),
            CallLog(
                id = UUID.randomUUID(),
                callSid = "CA1234567891",
                entityName = "Advisor",
                entityId = 456L,
                exophone = "+911234567890",
                direction = "incoming",
                fromNumber = "+918765432109",
                toNumber = phoneNumber,
                status = "completed"
            )
        )

        every { callLogRepository.findByPhoneNumber(phoneNumber, phoneNumber) } returns callLogs

        // When
        val result = callLogService.getCallLogsByPhoneNumber(phoneNumber)

        // Then
        assertEquals(callLogs, result)
        verify { callLogRepository.findByPhoneNumber(phoneNumber, phoneNumber) }
    }

    @Test
    fun `getCallLogsByStatus should return call logs for status`() {
        // Given
        val status = "completed"
        val callLogs = listOf(
            CallLog(
                id = UUID.randomUUID(),
                callSid = "CA1234567890",
                entityName = "Lead",
                entityId = 123L,
                exophone = "+911234567890",
                direction = "outgoing",
                fromNumber = "+919876543210",
                toNumber = "+918765432109",
                status = status
            ),
            CallLog(
                id = UUID.randomUUID(),
                callSid = "CA1234567891",
                entityName = "Advisor",
                entityId = 456L,
                exophone = "+911234567890",
                direction = "incoming",
                fromNumber = "+918765432109",
                toNumber = "+919876543210",
                status = status
            )
        )

        every { callLogRepository.findByStatus(status) } returns callLogs

        // When
        val result = callLogService.getCallLogsByStatus(status)

        // Then
        assertEquals(callLogs, result)
        verify { callLogRepository.findByStatus(status) }
    }

    @Test
    fun `getCallLogsByDirection should return call logs for direction`() {
        // Given
        val direction = "outgoing"
        val callLogs = listOf(
            CallLog(
                id = UUID.randomUUID(),
                callSid = "CA1234567890",
                entityName = "Lead",
                entityId = 123L,
                exophone = "+911234567890",
                direction = direction,
                fromNumber = "+919876543210",
                toNumber = "+918765432109",
                status = "completed"
            ),
            CallLog(
                id = UUID.randomUUID(),
                callSid = "CA1234567891",
                entityName = "Advisor",
                entityId = 456L,
                exophone = "+911234567890",
                direction = direction,
                fromNumber = "+919876543210",
                toNumber = "+918765432109",
                status = "completed"
            )
        )

        every { callLogRepository.findByDirection(direction) } returns callLogs

        // When
        val result = callLogService.getCallLogsByDirection(direction)

        // Then
        assertEquals(callLogs, result)
        verify { callLogRepository.findByDirection(direction) }
    }

    @Test
    fun `updateCallEndTime should update call log with end time and duration`() {
        // Given
        val callSid = "CA1234567890"
        val endTime = LocalDateTime.now()
        val duration = 120
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
        val result = callLogService.updateCallEndTime(callSid, endTime, duration)

        // Then
        assertNotNull(result)
        assertEquals(endTime, result?.endTime)
        assertEquals(duration, result?.duration)
        verify { callLogRepository.findByCallSid(callSid) }
        verify { callLogRepository.save(any<CallLog>()) }
    }

    @Test
    fun `updateCallEndTime should handle null duration`() {
        // Given
        val callSid = "CA1234567890"
        val endTime = LocalDateTime.now()
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
        val result = callLogService.updateCallEndTime(callSid, endTime, null)

        // Then
        assertNotNull(result)
        assertEquals(endTime, result?.endTime)
        assertNull(result?.duration)
        verify { callLogRepository.findByCallSid(callSid) }
        verify { callLogRepository.save(any<CallLog>()) }
    }

    @Test
    fun `updateCallEndTime should return null when call log not found`() {
        // Given
        val callSid = "CA1234567890"
        val endTime = LocalDateTime.now()
        val duration = 120

        every { callLogRepository.findByCallSid(callSid) } returns null

        // When
        val result = callLogService.updateCallEndTime(callSid, endTime, duration)

        // Then
        assertNull(result)
        verify { callLogRepository.findByCallSid(callSid) }
        verify(exactly = 0) { callLogRepository.save(any<CallLog>()) }
    }
}
