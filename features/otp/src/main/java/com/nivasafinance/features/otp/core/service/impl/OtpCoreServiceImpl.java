package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.otp.core.config.OtpRuntimeConfig;
import com.nivasafinance.features.otp.core.dto.OtpGenerationContext;
import com.nivasafinance.features.otp.core.dto.OtpSendCommand;
import com.nivasafinance.features.otp.core.dto.OtpSendResult;
import com.nivasafinance.features.otp.core.dto.OtpTrackedToken;
import com.nivasafinance.features.otp.core.dto.OtpVerifyCommand;
import com.nivasafinance.features.otp.core.dto.OtpVerifyResult;
import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import com.nivasafinance.features.otp.core.enums.OtpReference;
import com.nivasafinance.features.otp.core.enums.OtpStatus;
import com.nivasafinance.features.otp.core.repository.OneTimeTokenRepositoryWrapper;
import com.nivasafinance.features.otp.core.service.OtpConfigurationService;
import com.nivasafinance.features.otp.core.service.OtpCoreService;
import com.nivasafinance.features.otp.core.service.OtpGenerator;
import com.nivasafinance.features.otp.core.service.OtpTrackingStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OtpCoreServiceImpl implements OtpCoreService {

    private final OtpConfigurationService otpConfigurationService;
    private final OtpGeneratorFactory otpGeneratorFactory;
    private final OneTimeTokenRepositoryWrapper oneTimeTokenRepositoryWrapper;

    
    @Override
    public OtpSendResult sendOtp(OtpSendCommand command, OtpTrackingStore trackingStore) {
        OtpConfiguration configuration = otpConfigurationService.getByReference(command.getReference());
        OtpRuntimeConfig runtimeConfig = configuration.getConfig();
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(runtimeConfig.getOtpValidityInMins());
        long attempts = trackingStore.countAttemptsSince(command.getReference(), command.getScope(), windowStart);
        if (attempts >= runtimeConfig.getMaxResendAttempts()) {
            throw new BadRequestException("Maximum resend attempts reached for this contact");
        }
        
        trackingStore.invalidateActiveTokens(command.getReference(), command.getScope());
        
        OtpGenerator generator = otpGeneratorFactory.getGenerator(runtimeConfig.getOtpGenerationMethod());
        String otp = generator.generate(OtpGenerationContext.builder()
        .reference(command.getReference())
        .relatesTo(command.getRelatesTo())
        .build());
        
        OneTimeToken token = oneTimeTokenRepositoryWrapper.saveWithException(OneTimeToken.builder()
        .otp(otp)
        .relatesTo(command.getRelatesTo())
        .build());
        
        Long trackingId = trackingStore.createTrackingRecord(token.getId(), command.getReference(), command.getScope(), OtpStatus.QUEUED);
        
        int remainingAttempts = (int) Math.max(0, runtimeConfig.getMaxResendAttempts() - (attempts + 1));
        return OtpSendResult.builder()
        .requestId(trackingId)
        .reference(command.getReference())
        .validityInMins(runtimeConfig.getOtpValidityInMins())
        .resendAttemptsRemaining(remainingAttempts)
        .build();
    }
    
    @Override
    public OtpVerifyResult verifyOtp(OtpVerifyCommand command, OtpTrackingStore trackingStore) {
        OtpConfiguration configuration = otpConfigurationService.getByReference(command.getReference());
        OtpRuntimeConfig runtimeConfig = configuration.getConfig();
        OtpTrackedToken trackedToken = trackingStore.findLatestActiveToken(command.getReference(), command.getScope())
        .orElseThrow(() -> new BadRequestException("No active OTP found for this contact"));
        
        if (trackedToken.getCreatedAt().plusMinutes(runtimeConfig.getOtpValidityInMins()).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }
        if (!trackedToken.getOtp().equals(command.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }
        
        trackingStore.updateStatus(trackedToken.getTrackingId(), OtpStatus.VERIFIED);
        return OtpVerifyResult.builder().verified(true).build();
    }

    public OtpCoreServiceImpl(
            OtpConfigurationService otpConfigurationService,
            OtpGeneratorFactory otpGeneratorFactory,
            OneTimeTokenRepositoryWrapper oneTimeTokenRepositoryWrapper) {
        this.otpConfigurationService = otpConfigurationService;
        this.otpGeneratorFactory = otpGeneratorFactory;
        this.oneTimeTokenRepositoryWrapper = oneTimeTokenRepositoryWrapper;
    }
}
