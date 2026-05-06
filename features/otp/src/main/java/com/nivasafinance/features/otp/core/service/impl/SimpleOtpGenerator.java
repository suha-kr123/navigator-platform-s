package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.features.otp.core.dto.OtpGenerationContext;
import com.nivasafinance.features.otp.core.service.OtpGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SimpleOtpGenerator implements OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String getMethod() {
        return "simple_otp_generation";
    }

    @Override
    public String generate(OtpGenerationContext context) {
        int first = RANDOM.nextInt(10);
        int second = RANDOM.nextInt(10);
        int third = RANDOM.nextInt(10);
        int fourth = RANDOM.nextInt(10);
        return String.format("%d%d%d%d", first, second, third, fourth);
    }
}
