package services.voice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import services.voice.dto.CallLogRequest
import services.voice.entity.CallLog
import services.voice.repository.CallLogRepository
import java.time.LocalDateTime

@Service
@Transactional
class CallLogService(
    private val callLogRepository: CallLogRepository
) {

    fun saveCallLog(callLog: CallLog): CallLog {
        return callLogRepository.save(callLog)
    }

    fun updateCallStatus(callSid: String, status: String, recordingUrl: String? = null): CallLog? {
        val callLog = callLogRepository.findByCallSid(callSid)
        return if (callLog != null) {
            callLog.status = status
            if (recordingUrl != null) {
                callLog.recordingUrl = recordingUrl
            }
            callLogRepository.save(callLog)
        } else {
            null
        }
    }

    fun updateCallEndTime(callSid: String, endTime: LocalDateTime, duration: Int? = null): CallLog? {
        val callLog = callLogRepository.findByCallSid(callSid)
        return if (callLog != null) {
            callLog.endTime = endTime
            if (duration != null) {
                callLog.duration = duration
            }
            callLogRepository.save(callLog)
        } else {
            null
        }
    }

    fun getCallLogBySid(callSid: String): CallLog? {
        return callLogRepository.findByCallSid(callSid)
    }

    fun getCallLogsByEntity(entityName: String, entityId: Long): List<CallLog> {
        return callLogRepository.findByEntity(entityName, entityId)
    }

    fun getCallLogsByPhoneNumber(phoneNumber: String): List<CallLog> {
        return callLogRepository.findByPhoneNumber(phoneNumber, phoneNumber)
    }

    fun getCallLogsByStatus(status: String): List<CallLog> {
        return callLogRepository.findByStatus(status)
    }

    fun getCallLogsByDirection(direction: String): List<CallLog> {
        return callLogRepository.findByDirection(direction)
    }

    fun createCallLog(request: CallLogRequest): CallLog {
        val callLog = CallLog(
            callSid = request.callSid,
            entityName = request.entityName,
            entityId = request.entityId,
            direction = request.direction,
            fromNumber = request.fromNumber,
            toNumber = request.toNumber,
            status = request.status,
            exophone = request.exophone,
            startTime = LocalDateTime.now()
        )
        return saveCallLog(callLog)
    }
}
