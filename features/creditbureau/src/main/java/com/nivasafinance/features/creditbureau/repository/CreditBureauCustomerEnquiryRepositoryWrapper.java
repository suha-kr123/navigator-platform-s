package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauCustomerEnquiry;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreditBureauCustomerEnquiryRepositoryWrapper {

    private final CreditBureauCustomerEnquiryRepository creditBureauCustomerEnquiryRepository;
    private final MessageSource messageSource;

    public CreditBureauCustomerEnquiryRepositoryWrapper(
            CreditBureauCustomerEnquiryRepository creditBureauCustomerEnquiryRepository,
            MessageSource messageSource) {
        this.creditBureauCustomerEnquiryRepository = creditBureauCustomerEnquiryRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauCustomerEnquiry saveWithException(CreditBureauCustomerEnquiry customerEnquiry) {
        try {
            return creditBureauCustomerEnquiryRepository.save(customerEnquiry);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauCustomerEnquiry findByIdWithException(Long id) {
        try {
            return creditBureauCustomerEnquiryRepository.findById(id)
                    .orElseThrow(() -> CreditBureauExceptionFactory.notFoundById(id, messageSource));
        } catch (com.nivasafinance.features.creditbureau.exception.CreditBureauNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<CreditBureauCustomerEnquiry> findByIdentifier(UUID identifier) {
        return creditBureauCustomerEnquiryRepository.findByIdentifier(identifier);
    }

    public List<CreditBureauCustomerEnquiry> findByEnquiryId(Long enquiryId) {
        return creditBureauCustomerEnquiryRepository.findByEnquiryId(enquiryId);
    }

    public void deleteByEnquiryId(Long enquiryId) {
        try {
            creditBureauCustomerEnquiryRepository.deleteByEnquiryId(enquiryId);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
