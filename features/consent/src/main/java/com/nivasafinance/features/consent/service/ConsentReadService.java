package com.nivasafinance.features.consent.service;

import com.nivasafinance.features.consent.dto.ConsentResponse;

import java.util.Optional;
import java.util.UUID;

public interface ConsentReadService {

    Optional<ConsentResponse> findById(Long id);

    ConsentResponse findByIdWithException(Long id);

    ConsentResponse findByIdentifierWithException(UUID identifier);
}
