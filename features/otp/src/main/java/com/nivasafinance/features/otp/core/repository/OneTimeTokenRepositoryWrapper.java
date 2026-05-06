package com.nivasafinance.features.otp.core.repository;

import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class OneTimeTokenRepositoryWrapper {

    private final OneTimeTokenRepository repository;

    public OneTimeTokenRepositoryWrapper(OneTimeTokenRepository repository) {
        this.repository = repository;
    }

    public OneTimeToken saveWithException(OneTimeToken token) {
        try {
            return repository.saveAndFlush(token);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to save one time token", e);
        }
    }
}
