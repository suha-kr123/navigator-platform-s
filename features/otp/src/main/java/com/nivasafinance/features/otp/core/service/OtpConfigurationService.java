package com.nivasafinance.features.otp.core.service;

import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import com.nivasafinance.features.otp.core.enums.OtpReference;

public interface OtpConfigurationService {
    OtpConfiguration getByReference(OtpReference reference);
}
