package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.otp.core.config.OtpChannelConfig;
import com.nivasafinance.features.otp.core.config.OtpRuntimeConfig;
import com.nivasafinance.features.otp.core.dto.OtpGenerationContext;
import com.nivasafinance.features.otp.core.dto.OtpSendCommand;
import com.nivasafinance.features.otp.core.dto.OtpSendResult;
import com.nivasafinance.features.otp.core.dto.OtpVerifyCommand;
import com.nivasafinance.features.otp.core.dto.OtpVerifyResult;
import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import com.nivasafinance.features.otp.core.exception.OtpExceptionFactory;
import com.nivasafinance.features.otp.core.enums.OtpChannel;
import com.nivasafinance.features.otp.core.repository.OneTimeTokenRepositoryWrapper;
import com.nivasafinance.features.otp.core.service.OtpConfigurationService;
import com.nivasafinance.features.otp.core.service.OtpCoreService;
import com.nivasafinance.features.otp.core.service.OtpDeliveryService;
import com.nivasafinance.features.otp.core.service.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpCoreServiceImpl implements OtpCoreService {

    private final OtpConfigurationService otpConfigurationService;
    private final OtpGeneratorFactory otpGeneratorFactory;
    private final OneTimeTokenRepositoryWrapper oneTimeTokenRepositoryWrapper;
    private final List<OtpDeliveryService> deliveryServices;

    @Override
    public OtpSendResult sendOtp(OtpSendCommand command) {
        OtpConfiguration configuration = otpConfigurationService.getByReference(command.getReference());
        OtpRuntimeConfig runtimeConfig = configuration.getConfig();

        OtpGenerator generator = otpGeneratorFactory.getGenerator(runtimeConfig.getOtpGenerationMethod());
        String otp = generator.generate(OtpGenerationContext.builder()
                .reference(command.getReference())
                .build());

        OneTimeToken token = oneTimeTokenRepositoryWrapper.saveWithException(OneTimeToken.builder()
                .otp(otp)
                .relatesTo(command.getRecipient())
                .build());

        boolean delivered = false;
        List<String> deliveryErrors = new ArrayList<>();
        for (OtpChannelConfig channelConfig : runtimeConfig.getOtpChannels()) {
            OtpChannel channel = channelConfig.getChannelName();
            OtpDeliveryService deliveryService = findDeliveryService(channel);
            if (deliveryService == null) {
                deliveryErrors.add("No OTP delivery service configured for channel " + channel.name());
                continue;
            }

            String templateName = channelConfig.resolveTemplateName();
            try {
                deliveryService.send(command.getRecipient(), otp, templateName, command.getReference());
                delivered = true;
            } catch (BadRequestException ex) {
                String message = ex.getMessage() != null ? ex.getMessage() : "Unknown delivery failure";
                deliveryErrors.add(channel.name() + ": " + message);
                log.warn("OTP delivery failed for reference {} on channel {}", command.getReference(), channel, ex);
            }
        }

        if (!delivered) {
            throw OtpExceptionFactory.deliveryFailed("Failed to send OTP on configured channels: "
                    + String.join(" | ", deliveryErrors));
        }

        return OtpSendResult.builder()
                .oneTimeTokenId(token.getId())
                .reference(command.getReference())
                .validityInMins(runtimeConfig.getOtpValidityInMins())
                .build();
    }

    @Override
    public OtpVerifyResult verifyOtp(OtpVerifyCommand command) {
        OtpConfiguration configuration = otpConfigurationService.getByReference(command.getReference());
        OtpRuntimeConfig runtimeConfig = configuration.getConfig();
        OneTimeToken token = oneTimeTokenRepositoryWrapper.findById(command.getOneTimeTokenId())
                .orElseThrow(OtpExceptionFactory::tokenNotFound);

        if (token.getCreatedAt().plusMinutes(runtimeConfig.getOtpValidityInMins()).isBefore(LocalDateTime.now())) {
            throw OtpExceptionFactory.expiredOtp();
        }
        if (!token.getOtp().equals(command.getOtp())) {
            throw OtpExceptionFactory.invalidOtp();
        }

        return OtpVerifyResult.builder().verified(true).build();
    }

    private OtpDeliveryService findDeliveryService(OtpChannel channel) {
        for (OtpDeliveryService deliveryService : deliveryServices) {
            if (deliveryService.getChannel() == channel) {
                return deliveryService;
            }
        }
        return null;
    }
}
