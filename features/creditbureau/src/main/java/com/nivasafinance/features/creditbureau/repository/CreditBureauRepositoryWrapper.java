package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauNotFoundException;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CreditBureauRepositoryWrapper {

    private final CreditBureauRepository creditBureauRepository;
    private final MessageSource messageSource;

    public CreditBureauRepositoryWrapper(CreditBureauRepository creditBureauRepository, MessageSource messageSource) {
        this.creditBureauRepository = creditBureauRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauEnquiry saveWithException(CreditBureauEnquiry creditBureauEnquiry) {
        try {
            return creditBureauRepository.save(creditBureauEnquiry);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauEnquiry findByIdWithException(Long id) {
        try {
            return creditBureauRepository.findById(id)
                    .orElseThrow(() -> CreditBureauExceptionFactory.notFoundById(id, messageSource));
        } catch (CreditBureauNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<CreditBureauEnquiry> findByIdentifier(UUID identifier) {
        try {
            return creditBureauRepository.findByIdentifier(identifier);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdentifierFailed(identifier, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauEnquiry findByIdentifierWithException(UUID identifier) {
        return findByIdentifier(identifier)
                .orElseThrow(() -> CreditBureauExceptionFactory.notFoundByIdentifier(identifier, messageSource));
    }
}

