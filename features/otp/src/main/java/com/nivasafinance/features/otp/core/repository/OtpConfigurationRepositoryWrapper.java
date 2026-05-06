package com.nivasafinance.features.otp.core.repository;

import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class OtpConfigurationRepositoryWrapper {

    private final OtpConfigurationRepository repository;

    public OtpConfigurationRepositoryWrapper(OtpConfigurationRepository repository) {
        this.repository = repository;
    }

    public OtpConfiguration findByUnameWithException(String uname) {
        try {
            return repository.findByUname(uname)
                    .orElseThrow(() -> new ResourceNotFoundException("OTP configuration not found for reference: " + uname));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve OTP configuration", e);
        }
    }
}
