package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CreditBureauDerivedAttribute;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditBureauDerivedAttributeRepositoryWrapper {

    private final CreditBureauDerivedAttributeRepository cbDerivedAttributeRepository;
    private final MessageSource messageSource;

    public void deleteByEnquiryId(Long enquiryId) {
        try {
            cbDerivedAttributeRepository.deleteByEnquiryId(enquiryId);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<CreditBureauDerivedAttribute> saveAllWithException(List<CreditBureauDerivedAttribute> entities) {
        try {
            return cbDerivedAttributeRepository.saveAll(entities);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
