package com.nivasafinance.features.consent.repository;

import com.nivasafinance.features.consent.entity.Consent;
import com.nivasafinance.features.consent.exception.ConsentExceptionFactory;
import com.nivasafinance.features.consent.exception.ConsentNotFoundException;
import com.nivasafinance.features.consent.exception.ConsentOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ConsentRepositoryWrapper {

    private final ConsentRepository consentRepository;
    private final MessageSource messageSource;

    public ConsentRepositoryWrapper(ConsentRepository consentRepository, MessageSource messageSource) {
        this.consentRepository = consentRepository;
        this.messageSource = messageSource;
    }

    public Optional<Consent> findById(Long id) {
        return consentRepository.findById(id);
    }

    public Optional<Consent> findByIdentifier(UUID identifier) {
        return consentRepository.findByIdentifier(identifier);
    }

    public Consent saveWithException(Consent consent) {
        try {
            return consentRepository.save(consent);
        } catch (DataAccessException e) {
            ConsentOperationException ex = ConsentExceptionFactory.saveFailed(messageSource);
            ex.initCause(e);
            throw ex;
        }
    }

    public Consent findByIdWithException(Long id) {
        try {
            return consentRepository.findById(id)
                    .orElseThrow(() -> ConsentExceptionFactory.notFoundById(id, messageSource));
        } catch (ConsentNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            ConsentOperationException ex = ConsentExceptionFactory.retrieveByIdFailed(id, messageSource);
            ex.initCause(e);
            throw ex;
        }
    }

    public Consent findByIdentifierWithException(UUID identifier) {
        try {
            return consentRepository.findByIdentifier(identifier)
                    .orElseThrow(() -> ConsentExceptionFactory.notFoundByIdentifier(identifier, messageSource));
        } catch (ConsentNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            ConsentOperationException ex = ConsentExceptionFactory.retrieveByIdentifierFailed(identifier, messageSource);
            ex.initCause(e);
            throw ex;
        }
    }
}
