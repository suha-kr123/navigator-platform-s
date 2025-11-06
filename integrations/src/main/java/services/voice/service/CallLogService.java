package services.voice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import services.voice.dto.CallLogRequest;
import services.voice.entity.CallLog;
import services.voice.repository.CallLogRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CallLogService {

    private final CallLogRepository callLogRepository;

    @Autowired
    public CallLogService(CallLogRepository callLogRepository) {
        this.callLogRepository = callLogRepository;
    }

    public CallLog saveCallLog(CallLog callLog) {
        return callLogRepository.save(callLog);
    }

    public CallLog updateCallStatus(String callSid, String status, String recordingUrl) {
        return callLogRepository.findByCallSid(callSid)
                .map(callLog -> {
                    callLog.setStatus(status);
                    if (recordingUrl != null) {
                        callLog.setRecordingUrl(recordingUrl);
                    }
                    return callLogRepository.save(callLog);
                })
                .orElse(null);
    }

    public CallLog updateCallStatus(String callSid, String status) {
        return updateCallStatus(callSid, status, null);
    }

    public CallLog updateCallEndTime(String callSid, LocalDateTime endTime, Integer duration) {
        return callLogRepository.findByCallSid(callSid)
                .map(callLog -> {
                    callLog.setEndTime(endTime);
                    if (duration != null) {
                        callLog.setDuration(duration);
                    }
                    return callLogRepository.save(callLog);
                })
                .orElse(null);
    }

    public CallLog updateCallEndTime(String callSid, LocalDateTime endTime) {
        return updateCallEndTime(callSid, endTime, null);
    }

    public CallLog getCallLogBySid(String callSid) {
        return callLogRepository.findByCallSid(callSid).orElse(null);
    }

    public List<CallLog> getCallLogsByEntity(String entityName, Long entityId) {
        return callLogRepository.findByEntity(entityName, entityId);
    }

    public List<CallLog> getCallLogsByPhoneNumber(String phoneNumber) {
        return callLogRepository.findByPhoneNumber(phoneNumber, phoneNumber);
    }

    public List<CallLog> getCallLogsByStatus(String status) {
        return callLogRepository.findByStatus(status);
    }

    public List<CallLog> getCallLogsByDirection(String direction) {
        return callLogRepository.findByDirection(direction);
    }

    public CallLog createCallLog(CallLogRequest request) {
        CallLog callLog = new CallLog();
        callLog.setCallSid(request.getCallSid());
        callLog.setEntityName(request.getEntityName());
        callLog.setEntityId(request.getEntityId());
        callLog.setDirection(request.getDirection());
        callLog.setFromNumber(request.getFromNumber());
        callLog.setToNumber(request.getToNumber());
        callLog.setStatus(request.getStatus());
        callLog.setExophone(request.getExophone());
        callLog.setStartTime(LocalDateTime.now());
        return saveCallLog(callLog);
    }
}

