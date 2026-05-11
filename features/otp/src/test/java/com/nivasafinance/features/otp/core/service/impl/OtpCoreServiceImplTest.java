package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.otp.core.config.OtpChannelConfig;
import com.nivasafinance.features.otp.core.config.OtpChannelRuntimeConfig;
import com.nivasafinance.features.otp.core.config.OtpRuntimeConfig;
import com.nivasafinance.features.otp.core.dto.OtpSendCommand;
import com.nivasafinance.features.otp.core.dto.OtpVerifyCommand;
import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import com.nivasafinance.features.otp.core.enums.OtpChannel;
import com.nivasafinance.features.otp.core.exception.OtpDeliveryFailedException;
import com.nivasafinance.features.otp.core.repository.OneTimeTokenRepositoryWrapper;
import com.nivasafinance.features.otp.core.service.OtpConfigurationService;
import com.nivasafinance.features.otp.core.service.OtpDeliveryService;
import com.nivasafinance.features.otp.core.service.OtpGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpCoreServiceImplTest {

    private static final String REFERENCE = "VERIFY_LEAD_FOR_CB";

    @Mock
    private OtpConfigurationService otpConfigurationService;
    @Mock
    private OtpGeneratorFactory otpGeneratorFactory;
    @Mock
    private OneTimeTokenRepositoryWrapper oneTimeTokenRepositoryWrapper;
    @Mock
    private OtpGenerator otpGenerator;
    @Mock
    private OtpDeliveryService otpDeliveryService;

    private OtpCoreServiceImpl otpCoreService;
    private OtpConfiguration configuration;

    @BeforeEach
    void setUp() {
        otpCoreService = new OtpCoreServiceImpl(
                otpConfigurationService,
                otpGeneratorFactory,
                oneTimeTokenRepositoryWrapper,
                List.of(otpDeliveryService));

        configuration = OtpConfiguration.builder()
                .uname(REFERENCE)
                .config(OtpRuntimeConfig.builder()
                        .otpValidityInMins(10)
                        .otpGenerationMethod("simple_otp_generation")
                        .otpChannels(List.of(OtpChannelConfig.builder()
                                .channelName(OtpChannel.WHATSAPP)
                                .channelConfig(List.of(OtpChannelRuntimeConfig.builder()
                                        .templateName(REFERENCE)
                                        .build()))
                                .build()))
                        .build())
                .build();
    }

    @Test
    void sendOtp_successfullyPersistsAndSends() {
        OtpSendCommand command = OtpSendCommand.builder()
                .reference(REFERENCE)
                .recipient("9876543210")
                .build();

        when(otpDeliveryService.getChannel()).thenReturn(OtpChannel.WHATSAPP);
        when(otpConfigurationService.getByReference(REFERENCE)).thenReturn(configuration);
        when(otpGeneratorFactory.getGenerator("simple_otp_generation")).thenReturn(otpGenerator);
        when(otpGenerator.generate(any())).thenReturn("2244");
        when(oneTimeTokenRepositoryWrapper.saveWithException(any(OneTimeToken.class)))
                .thenReturn(OneTimeToken.builder().id(11L).otp("2244").relatesTo("9876543210").build());

        var result = otpCoreService.sendOtp(command);

        assertEquals(11L, result.getOneTimeTokenId());
        assertEquals(REFERENCE, result.getReference());
        assertEquals(10, result.getValidityInMins());
        verify(otpDeliveryService).send("9876543210", "2244", REFERENCE, REFERENCE);
    }

    @Test
    void sendOtp_throwsCustomExceptionWhenAllConfiguredChannelsFail() {
        OtpSendCommand command = OtpSendCommand.builder()
                .reference(REFERENCE)
                .recipient("9876543210")
                .build();

        when(otpDeliveryService.getChannel()).thenReturn(OtpChannel.WHATSAPP);
        when(otpConfigurationService.getByReference(REFERENCE)).thenReturn(configuration);
        when(otpGeneratorFactory.getGenerator("simple_otp_generation")).thenReturn(otpGenerator);
        when(otpGenerator.generate(any())).thenReturn("2244");
        when(oneTimeTokenRepositoryWrapper.saveWithException(any(OneTimeToken.class)))
                .thenReturn(OneTimeToken.builder().id(11L).otp("2244").relatesTo("9876543210").build());
        doThrow(new BadRequestException("Provider down"))
                .when(otpDeliveryService).send("9876543210", "2244", REFERENCE, REFERENCE);

        OtpDeliveryFailedException exception = assertThrows(OtpDeliveryFailedException.class, () -> otpCoreService.sendOtp(command));

        assertTrue(exception.getMessage().contains("Failed to send OTP on configured channels"));
        assertTrue(exception.getMessage().contains("WHATSAPP: Provider down"));
    }

    @Test
    void verifyOtp_returnsVerifiedWhenTokenMatchesAndIsNotExpired() {
        OtpVerifyCommand command = OtpVerifyCommand.builder()
                .reference(REFERENCE)
                .oneTimeTokenId(11L)
                .otp("2244")
                .build();
        OneTimeToken token = OneTimeToken.builder().id(11L).otp("2244").relatesTo("9876543210").build();
        token.setCreatedAt(LocalDateTime.now().minusMinutes(2));

        when(otpConfigurationService.getByReference(REFERENCE)).thenReturn(configuration);
        when(oneTimeTokenRepositoryWrapper.findById(11L)).thenReturn(Optional.of(token));

        var result = otpCoreService.verifyOtp(command);

        assertTrue(result.isVerified());
    }

    @Test
    void verifyOtp_rejectsExpiredOtp() {
        OtpVerifyCommand command = OtpVerifyCommand.builder()
                .reference(REFERENCE)
                .oneTimeTokenId(11L)
                .otp("2244")
                .build();
        OneTimeToken token = OneTimeToken.builder().id(11L).otp("2244").relatesTo("9876543210").build();
        token.setCreatedAt(LocalDateTime.now().minusMinutes(11));

        when(otpConfigurationService.getByReference(REFERENCE)).thenReturn(configuration);
        when(oneTimeTokenRepositoryWrapper.findById(11L)).thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> otpCoreService.verifyOtp(command));
    }
}
