package com.nivasafinance.features.call.repository;

import com.nivasafinance.features.call.entity.ReconciliationLog;
import com.nivasafinance.features.call.exception.CallLogExceptionFactory;
import com.nivasafinance.features.call.exception.CallLogOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReconciliationLogRepositoryWrapper {

    private final ReconciliationLogRepository reconciliationLogRepository;
    private final MessageSource messageSource;

    public ReconciliationLog saveWithException(ReconciliationLog row) {
        try {
            return reconciliationLogRepository.save(row);
        } catch (DataAccessException e) {
            CallLogOperationException exception = CallLogExceptionFactory.reconciliationLogSaveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
