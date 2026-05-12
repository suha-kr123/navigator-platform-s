package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLogLead;
import com.nivasafinance.features.call.exception.CallLogExceptionFactory;
import com.nivasafinance.features.call.exception.CallLogOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CallLogLeadRepositoryWrapper {

    private final CallLogLeadRepository callLogLeadRepository;
    private final MessageSource messageSource;

    public Page<CallLogLead> findByLeadId(Long leadId, Pageable pageable) {
        return callLogLeadRepository.findByLeadIdOrderByCallLogIdDesc(leadId, pageable);
    }

    public Page<CallLogLead> findByLeadIdWithAiAnalysis(Long leadId, Pageable pageable) {
        return callLogLeadRepository.findByLeadIdWithAiAnalysis(leadId, pageable);
    }

    public List<CallLogLead> findAllByLeadIdOrderByCallLogIdDesc(Long leadId) {
        return callLogLeadRepository.findAllByLeadIdOrderByCallLogIdDesc(leadId);
    }

    public Optional<CallLogLead> findByCallLogId(Long callLogId) {
        try {
            return callLogLeadRepository.findById(callLogId);
        } catch (DataAccessException e) {
            CallLogOperationException exception = CallLogExceptionFactory.retrieveByIdFailed(callLogId, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CallLogLead saveWithException(CallLogLead link) {
        try {
            return callLogLeadRepository.save(link);
        } catch (DataAccessException e) {
            CallLogOperationException exception = CallLogExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
