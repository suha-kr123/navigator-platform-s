package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauAccountSecurityDetails;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreditBureauAccountSecurityDetailsRepositoryWrapper {

    private final CreditBureauAccountSecurityDetailsRepository creditBureauAccountSecurityDetailsRepository;
    private final MessageSource messageSource;

    public CreditBureauAccountSecurityDetailsRepositoryWrapper(
            CreditBureauAccountSecurityDetailsRepository creditBureauAccountSecurityDetailsRepository,
            MessageSource messageSource) {
        this.creditBureauAccountSecurityDetailsRepository = creditBureauAccountSecurityDetailsRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauAccountSecurityDetails saveWithException(CreditBureauAccountSecurityDetails securityDetails) {
        try {
            return creditBureauAccountSecurityDetailsRepository.save(securityDetails);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauAccountSecurityDetails findByIdWithException(Long id) {
        try {
            return creditBureauAccountSecurityDetailsRepository.findById(id)
                    .orElseThrow(() -> CreditBureauExceptionFactory.notFoundById(id, messageSource));
        } catch (com.nivasafinance.features.creditbureau.exception.CreditBureauNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<CreditBureauAccountSecurityDetails> findByIdentifier(UUID identifier) {
        return creditBureauAccountSecurityDetailsRepository.findByIdentifier(identifier);
    }

    public List<CreditBureauAccountSecurityDetails> findByTradelineId(Long tradelineId) {
        return creditBureauAccountSecurityDetailsRepository.findByTradelineId(tradelineId);
    }

    public void deleteByTradelineId(Long tradelineId) {
        try {
            creditBureauAccountSecurityDetailsRepository.deleteByTradelineId(tradelineId);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
