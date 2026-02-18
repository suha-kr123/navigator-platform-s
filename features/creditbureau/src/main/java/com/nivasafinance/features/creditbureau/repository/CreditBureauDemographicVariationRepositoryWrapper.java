package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.dto.DemographicVariationResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauDemographicVariation;
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
public class CreditBureauDemographicVariationRepositoryWrapper {

    private final CreditBureauDemographicVariationRepository creditBureauDemographicVariationRepository;
    private final MessageSource messageSource;

    public CreditBureauDemographicVariationRepositoryWrapper(
            CreditBureauDemographicVariationRepository creditBureauDemographicVariationRepository,
            MessageSource messageSource) {
        this.creditBureauDemographicVariationRepository = creditBureauDemographicVariationRepository;
        this.messageSource = messageSource;
    }

    public CreditBureauDemographicVariation saveWithException(CreditBureauDemographicVariation demographicVariation) {
        try {
            return creditBureauDemographicVariationRepository.save(demographicVariation);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public CreditBureauDemographicVariation findByIdWithException(Long id) {
        try {
            return creditBureauDemographicVariationRepository.findById(id)
                    .orElseThrow(() -> CreditBureauExceptionFactory.notFoundById(id, messageSource));
        } catch (com.nivasafinance.features.creditbureau.exception.CreditBureauNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.retrieveByIdFailed(id, messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<CreditBureauDemographicVariation> findByIdentifier(UUID identifier) {
        return creditBureauDemographicVariationRepository.findByIdentifier(identifier);
    }

    public List<CreditBureauDemographicVariation> findByEnquiryId(Long enquiryId) {
        return creditBureauDemographicVariationRepository.findByEnquiryId(enquiryId);
    }

    public List<DemographicVariationResponse> findByEnquiryIdAsResponse(Long enquiryId) {
        return creditBureauDemographicVariationRepository.findByEnquiryId(enquiryId).stream()
                .map(DemographicVariationResponse::toDemographicVariationResponse)
                .collect(Collectors.toList());
    }

    public void deleteByEnquiryId(Long enquiryId) {
        try {
            creditBureauDemographicVariationRepository.deleteByEnquiryId(enquiryId);
        } catch (DataAccessException e) {
            CreditBureauOperationException exception = CreditBureauExceptionFactory.saveFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}
