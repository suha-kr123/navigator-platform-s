package com.nivasafinance.features.otp.core.service;

import com.nivasafinance.features.otp.core.entity.OtpConfiguration;

public interface OtpConfigurationService {
    OtpConfiguration getByReference(String reference);
}
