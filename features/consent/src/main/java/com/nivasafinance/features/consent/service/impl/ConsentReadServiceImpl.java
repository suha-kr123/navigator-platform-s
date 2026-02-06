package com.nivasafinance.features.consent.service.impl;

import com.nivasafinance.features.consent.dto.ConsentResponse;
import com.nivasafinance.features.consent.repository.ConsentRepositoryWrapper;
import com.nivasafinance.features.consent.service.ConsentReadService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ConsentReadServiceImpl implements ConsentReadService {

    private final ConsentRepositoryWrapper consentRepositoryWrapper;

    public ConsentReadServiceImpl(ConsentRepositoryWrapper consentRepositoryWrapper) {
        this.consentRepositoryWrapper = consentRepositoryWrapper;
    }

    @Override
    public Optional<ConsentResponse> findById(Long id) {
        return consentRepositoryWrapper.findById(id).map(ConsentResponse::toConsentResponse);
    }

    @Override
    public ConsentResponse findByIdWithException(Long id) {
        return ConsentResponse.toConsentResponse(consentRepositoryWrapper.findByIdWithException(id));
    }

    @Override
    public ConsentResponse findByIdentifierWithException(UUID identifier) {
        return ConsentResponse.toConsentResponse(consentRepositoryWrapper.findByIdentifierWithException(identifier));
    }
}
