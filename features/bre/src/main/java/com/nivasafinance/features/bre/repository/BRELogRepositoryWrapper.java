package com.nivasafinance.features.bre.repository;

import com.nivasafinance.features.bre.entity.BRELogs;
import com.nivasafinance.features.bre.exception.BREConfigExceptionFactory;
import com.nivasafinance.features.bre.exception.BREConfigNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BRELogRepositoryWrapper {

    private final BRELogRepository breLogRepository;
    private final MessageSource messageSource;

    public BRELogs saveWithException(BRELogs entity) {
        try {
            return breLogRepository.save(entity);
        } catch (DataAccessException e) {
            throw BREConfigExceptionFactory.logSaveFailed(messageSource);
        }
    }

    public BRELogs findByIdWithException(Long id) {
        try {
            return breLogRepository.findById(id).orElseThrow(() ->
                    BREConfigExceptionFactory.logNotFoundById(id, messageSource));
        } catch (BREConfigNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw BREConfigExceptionFactory.logRetrieveByIdFailed(id, messageSource);
        }
    }
}
