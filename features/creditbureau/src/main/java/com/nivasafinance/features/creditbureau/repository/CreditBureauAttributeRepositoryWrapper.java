package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauAttribute;
import com.nivasafinance.features.creditbureau.enums.CbAttributeCategory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditBureauAttributeRepositoryWrapper {

    private final CreditBureauAttributeRepository creditBureauAttributeRepository;
    private final MessageSource messageSource;

    public CreditBureauAttributeRepositoryWrapper(
            CreditBureauAttributeRepository creditBureauAttributeRepository,
            MessageSource messageSource) {
        this.creditBureauAttributeRepository = creditBureauAttributeRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauAttribute saveWithException(CreditBureauAttribute attribute) {
        try {
            return creditBureauAttributeRepository.save(attribute);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<CreditBureauAttribute> saveAllWithException(List<CreditBureauAttribute> attributes) {
        try {
            return creditBureauAttributeRepository.saveAll(attributes);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<CreditBureauAttribute> findByEnquiryId(Long enquiryId) {
        return creditBureauAttributeRepository.findByEnquiryId(enquiryId);
    }

    public List<CreditBureauAttribute> findByEnquiryIdAndCategory(Long enquiryId, CbAttributeCategory category) {
        return creditBureauAttributeRepository.findByEnquiryIdAndCategory(enquiryId, category);
    }

    public void deleteByEnquiryId(Long enquiryId) {
        try {
            creditBureauAttributeRepository.deleteByEnquiryId(enquiryId);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
