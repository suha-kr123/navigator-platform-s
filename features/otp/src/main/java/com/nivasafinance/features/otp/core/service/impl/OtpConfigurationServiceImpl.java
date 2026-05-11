package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.features.otp.core.config.OtpChannelConfig;
import com.nivasafinance.features.otp.core.config.OtpRuntimeConfig;
import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import com.nivasafinance.features.otp.core.enums.OtpChannel;
import com.nivasafinance.features.otp.core.exception.OtpExceptionFactory;
import com.nivasafinance.features.otp.core.repository.OtpConfigurationRepositoryWrapper;
import com.nivasafinance.features.otp.core.service.OtpConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpConfigurationServiceImpl implements OtpConfigurationService {

    private final OtpConfigurationRepositoryWrapper repositoryWrapper;

    @Override
    public OtpConfiguration getByReference(String reference) {
        OtpConfiguration configuration = repositoryWrapper.findByUnameWithException(reference);
        validate(configuration, reference);
        return configuration;
    }

    private void validate(OtpConfiguration configuration, String reference) {
        OtpRuntimeConfig runtimeConfig = configuration.getConfig();
        if (runtimeConfig == null) {
            throw OtpExceptionFactory.invalidConfiguration("OTP config is required for reference: " + reference);
        }
        if (runtimeConfig.getOtpValidityInMins() == null || runtimeConfig.getOtpValidityInMins() <= 0) {
            throw OtpExceptionFactory.invalidConfiguration("Invalid OTP validity for reference: " + reference);
        }
        if (runtimeConfig.getOtpGenerationMethod() == null || runtimeConfig.getOtpGenerationMethod().isBlank()) {
            throw OtpExceptionFactory.invalidConfiguration("OTP generation method is required for reference: " + reference);
        }
        if (runtimeConfig.getOtpChannels() == null || runtimeConfig.getOtpChannels().isEmpty()) {
            throw OtpExceptionFactory.invalidConfiguration("OTP channels are required for reference: " + reference);
        }

        for (OtpChannelConfig channelConfig : runtimeConfig.getOtpChannels()) {
            if (channelConfig.getChannelName() == null) {
                throw OtpExceptionFactory.invalidConfiguration("OTP channel name is required for reference: " + reference);
            }
            if (channelConfig.getChannelName() == OtpChannel.WHATSAPP
                    && (channelConfig.resolveTemplateName() == null || channelConfig.resolveTemplateName().isBlank())) {
                throw OtpExceptionFactory.invalidConfiguration("WhatsApp template name is required for reference: " + reference);
            }
        }
    }
}
