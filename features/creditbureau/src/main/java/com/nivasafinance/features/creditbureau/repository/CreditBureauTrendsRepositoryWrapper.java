package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.dto.TrendsResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauTrends;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.exception.CreditBureauOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CreditBureauTrendsRepositoryWrapper {

    private final CreditBureauTrendsRepository creditBureauTrendsRepository;
    private final MessageSource messageSource;

    public CreditBureauTrendsRepositoryWrapper(
            CreditBureauTrendsRepository creditBureauTrendsRepository,
            MessageSource messageSource) {
        this.creditBureauTrendsRepository = creditBureauTrendsRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauTrends saveWithException(CreditBureauTrends scoreTrends) {
        try {
            return creditBureauTrendsRepository.save(scoreTrends);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauTrends findByIdWithException(Long id) {
        try {
            return creditBureauTrendsRepository.findById(id)
                    .orElseThrow(() -> CreditBureauExceptionFactory.notFoundById(id, messageSource));
        } catch (com.nivasafinance.features.creditbureau.exception.CreditBureauNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<TrendsResponse> findByIdentifier(UUID identifier) {
        return creditBureauTrendsRepository.findByIdentifier(identifier)
                .map(TrendsResponse::toTrendsResponse);
    }

    public List<TrendsResponse> findByEnquiryId(Long enquiryId) {
        return creditBureauTrendsRepository.findByEnquiryId(enquiryId).stream()
                .map(TrendsResponse::toTrendsResponse)
                .collect(Collectors.toList());
    }

    public List<TrendsResponse> findByEnquiryIdOrderByDateDesc(Long enquiryId) {
        return creditBureauTrendsRepository.findByEnquiryIdOrderByDateDesc(enquiryId).stream()
                .map(TrendsResponse::toTrendsResponse)
                .collect(Collectors.toList());
    }


    public List<CreditBureauTrends> findByEnquiryIdEntity(Long enquiryId) {
        return creditBureauTrendsRepository.findByEnquiryId(enquiryId);
    }
}