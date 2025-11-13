package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLog;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CallLogRepositoryWrapper {

    private final CallLogRepository callLogRepository;

    public CallLog saveWithException(CallLog callLog) {
        try {
            return callLogRepository.save(callLog);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to save call log", e);
        }
    }
}

