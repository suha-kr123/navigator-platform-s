package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauSummary;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CreditBureauSummaryRepositoryWrapper {

    private final CreditBureauSummaryRepository creditBureauSummaryRepository;
    private final MessageSource messageSource;

    public CreditBureauSummaryRepositoryWrapper(CreditBureauSummaryRepository creditBureauSummaryRepository, MessageSource messageSource) {
        this.creditBureauSummaryRepository = creditBureauSummaryRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauSummary saveWithException(CreditBureauSummary summary) {
        try {
            return creditBureauSummaryRepository.save(summary);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauSummary findByIdWithException(Long id) {
        try {
            return creditBureauSummaryRepository.findById(id)
                    .orElseThrow(() -> CreditBureauExceptionFactory.notFoundById(id, messageSource));
        } catch (com.nivasafinance.features.creditbureau.exception.CreditBureauNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<CreditBureauSummary> findByIdentifier(UUID identifier) {
        return creditBureauSummaryRepository.findByIdentifier(identifier);
    }

    public Optional<CreditBureauSummary> findByEnquiryId(Long enquiryId) {
        return creditBureauSummaryRepository.findByEnquiryId(enquiryId);
    }
}
