package com.nivasafinance.features.otp.core.service;

import com.nivasafinance.features.otp.core.dto.OtpGenerationContext;

public interface OtpGenerator {
    String getMethod();
    String generate(OtpGenerationContext context);
}
