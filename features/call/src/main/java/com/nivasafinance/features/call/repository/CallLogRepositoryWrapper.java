package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.exception.CallLogExceptionFactory;
import com.nivasafinance.features.call.exception.CallLogNotFoundException;
import com.nivasafinance.features.call.exception.CallLogOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CallLogRepositoryWrapper {

    private final CallLogRepository callLogRepository;
    private final MessageSource messageSource;

    public CallLog saveWithException(CallLog callLog) {
        try {
            return callLogRepository.save(callLog);
        } catch (DataAccessException e) {
            CallLogOperationException exception = CallLogExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CallLog findByIdWithException(Long id) {
        try {
            return callLogRepository.findById(id).orElseThrow(() ->
                    CallLogExceptionFactory.notFoundById(id, messageSource));
        } catch (CallLogNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CallLogOperationException exception = CallLogExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public CallLog findByIdentifierWithException(UUID identifier) {
        try {
            return callLogRepository.findByIdentifier(identifier).orElseThrow(() ->
                    CallLogExceptionFactory.notFoundByIdentifier(identifier, messageSource));
        } catch (CallLogNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CallLogOperationException exception = CallLogExceptionFactory.retrieveByIdentifierFailed(identifier, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CallLog findByProviderIdWithException(String providerId) {
        try {
            return callLogRepository.findByProviderId(providerId).orElseThrow(() ->
                    CallLogExceptionFactory.notFoundByProviderId(providerId, messageSource));
        } catch (CallLogNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw CallLogExceptionFactory.retrieveByProviderIdFailed(providerId, messageSource);
        }
    }

    public List<CallLog> findByIdsWithException(List<Long> ids) {
        try {
            return callLogRepository.findAllById(ids);
        } catch (DataAccessException e) {
            CallLogOperationException exception = CallLogExceptionFactory.retrieveByIdsFailed(ids, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}

