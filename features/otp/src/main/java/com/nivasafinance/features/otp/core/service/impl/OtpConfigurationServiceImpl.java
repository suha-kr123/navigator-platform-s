package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.otp.core.config.OtpChannelConfig;
import com.nivasafinance.features.otp.core.config.OtpRuntimeConfig;
import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import com.nivasafinance.features.otp.core.enums.OtpChannel;
import com.nivasafinance.features.otp.core.enums.OtpReference;
import com.nivasafinance.features.otp.core.repository.OtpConfigurationRepositoryWrapper;
import com.nivasafinance.features.otp.core.service.OtpConfigurationService;
import org.springframework.stereotype.Service;

@Service
public class OtpConfigurationServiceImpl implements OtpConfigurationService {

    private final OtpConfigurationRepositoryWrapper repositoryWrapper;

    public OtpConfigurationServiceImpl(OtpConfigurationRepositoryWrapper repositoryWrapper) {
        this.repositoryWrapper = repositoryWrapper;
    }

    @Override
    public OtpConfiguration getByReference(OtpReference reference) {
        OtpConfiguration configuration = repositoryWrapper.findByUnameWithException(reference.getConfigKey());
        validate(configuration, reference);
        return configuration;
    }

    private void validate(OtpConfiguration configuration, OtpReference reference) {
        OtpRuntimeConfig runtimeConfig = configuration.getConfig();
        if (runtimeConfig == null) {
            throw new BadRequestException("OTP config is required for reference: " + reference.name());
        }
        if (runtimeConfig.getOtpValidityInMins() == null || runtimeConfig.getOtpValidityInMins() <= 0) {
            throw new BadRequestException("Invalid OTP validity for reference: " + reference.name());
        }
        if (runtimeConfig.getOtpGenerationMethod() == null || runtimeConfig.getOtpGenerationMethod().isBlank()) {
            throw new BadRequestException("OTP generation method is required for reference: " + reference.name());
        }
        if (runtimeConfig.getOtpChannels() == null || runtimeConfig.getOtpChannels().isEmpty()) {
            throw new BadRequestException("OTP channels are required for reference: " + reference.name());
        }
        if (runtimeConfig.getMaxResendAttempts() == null || runtimeConfig.getMaxResendAttempts() < 1) {
            throw new BadRequestException("Invalid max resend attempts for reference: " + reference.name());
        }
        runtimeConfig.getOtpChannels().forEach(channelConfig -> validateChannelConfig(reference, channelConfig));
    }

    private void validateChannelConfig(OtpReference reference, OtpChannelConfig channelConfig) {
        if (channelConfig.getChannelName() == null) {
            throw new BadRequestException("Channel name is required for reference: " + reference.name());
        }
        if (channelConfig.getChannelName() == OtpChannel.WHATSAPP && channelConfig.resolveTemplateName().isEmpty()) {
            throw new BadRequestException("Template name is required for whatsapp channel for reference: " + reference.name());
        }
    }
}
