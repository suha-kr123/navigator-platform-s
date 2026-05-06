package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.otp.core.service.OtpGenerator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OtpGeneratorFactory {

    private final Map<String, OtpGenerator> generators;

    public OtpGeneratorFactory(List<OtpGenerator> generatorList) {
        this.generators = new HashMap<>();
        for (OtpGenerator generator : generatorList) {
            generators.put(generator.getMethod(), generator);
        }
    }

    public OtpGenerator getGenerator(String method) {
        OtpGenerator generator = generators.get(method);
        if (generator == null) {
            throw new BadRequestException("Unsupported OTP generation method: " + method);
        }
        return generator;
    }
}
