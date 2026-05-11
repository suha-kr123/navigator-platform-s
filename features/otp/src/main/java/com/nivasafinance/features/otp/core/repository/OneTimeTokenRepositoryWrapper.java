package com.nivasafinance.features.otp.core.repository;

import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import com.nivasafinance.features.otp.core.exception.OtpExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OneTimeTokenRepositoryWrapper {

    private final OneTimeTokenRepository repository;

    public OneTimeToken saveWithException(OneTimeToken token) {
        try {
            return repository.saveAndFlush(token);
        } catch (DataAccessException e) {
            throw OtpExceptionFactory.saveTokenFailed(e);
        }
    }

    public Optional<OneTimeToken> findById(Long tokenId) {
        try {
            return repository.findById(tokenId);
        } catch (DataAccessException e) {
            throw OtpExceptionFactory.retrieveTokenFailed(e);
        }
    }
}
