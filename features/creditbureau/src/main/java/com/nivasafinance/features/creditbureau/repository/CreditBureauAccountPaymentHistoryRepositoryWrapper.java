package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauAccountPaymentHistory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreditBureauAccountPaymentHistoryRepositoryWrapper {

    private final CreditBureauAccountPaymentHistoryRepository creditBureauAccountPaymentHistoryRepository;
    private final MessageSource messageSource;

    public CreditBureauAccountPaymentHistoryRepositoryWrapper(
            CreditBureauAccountPaymentHistoryRepository creditBureauAccountPaymentHistoryRepository,
            MessageSource messageSource) {
        this.creditBureauAccountPaymentHistoryRepository = creditBureauAccountPaymentHistoryRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauAccountPaymentHistory saveWithException(CreditBureauAccountPaymentHistory paymentHistory) {
        try {
            return creditBureauAccountPaymentHistoryRepository.save(paymentHistory);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauAccountPaymentHistory findByIdWithException(Long id) {
        try {
            return creditBureauAccountPaymentHistoryRepository.findById(id)
                    .orElseThrow(() -> CreditBureauExceptionFactory.notFoundById(id, messageSource));
        } catch (com.nivasafinance.features.creditbureau.exception.CreditBureauNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<CreditBureauAccountPaymentHistory> findByIdentifier(UUID identifier) {
        return creditBureauAccountPaymentHistoryRepository.findByIdentifier(identifier);
    }

    public List<CreditBureauAccountPaymentHistory> findByTradelineId(Long tradelineId) {
        return creditBureauAccountPaymentHistoryRepository.findByTradelineId(tradelineId);
    }

    public void deleteByTradelineId(Long tradelineId) {
        try {
            creditBureauAccountPaymentHistoryRepository.deleteByTradelineId(tradelineId);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
