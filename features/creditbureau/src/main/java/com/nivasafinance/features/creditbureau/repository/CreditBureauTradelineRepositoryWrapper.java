package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauTradeline;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreditBureauTradelineRepositoryWrapper {

    private final CreditBureauTradelineRepository creditBureauTradelineRepository;
    private final MessageSource messageSource;

    public CreditBureauTradelineRepositoryWrapper(CreditBureauTradelineRepository creditBureauTradelineRepository, MessageSource messageSource) {
        this.creditBureauTradelineRepository = creditBureauTradelineRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauTradeline saveWithException(CreditBureauTradeline tradeline) {
        try {
            return creditBureauTradelineRepository.save(tradeline);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauTradeline findByIdWithException(Long id) {
        try {
            return creditBureauTradelineRepository.findById(id)
                    .orElseThrow(() -> CreditBureauExceptionFactory.notFoundById(id, messageSource));
        } catch (com.nivasafinance.features.creditbureau.exception.CreditBureauNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<CreditBureauTradeline> findByIdentifier(UUID identifier) {
        return creditBureauTradelineRepository.findByIdentifier(identifier);
    }

    public List<CreditBureauTradeline> findByEnquiryId(Long enquiryId) {
        return creditBureauTradelineRepository.findByEnquiryId(enquiryId);
    }

    public void deleteByEnquiryId(Long enquiryId) {
        try {
            creditBureauTradelineRepository.deleteByEnquiryId(enquiryId);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
