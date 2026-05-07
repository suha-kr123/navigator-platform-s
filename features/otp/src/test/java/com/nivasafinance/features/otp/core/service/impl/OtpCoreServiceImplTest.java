package com.nivasafinance.features.otp.core.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.otp.core.config.OtpRuntimeConfig;
import com.nivasafinance.features.otp.core.dto.OtpScope;
import com.nivasafinance.features.otp.core.dto.OtpSubject;
import com.nivasafinance.features.otp.core.dto.OtpSendCommand;
import com.nivasafinance.features.otp.core.dto.OtpTrackedToken;
import com.nivasafinance.features.otp.core.dto.OtpVerifyCommand;
import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import com.nivasafinance.features.otp.core.entity.OtpConfiguration;
import com.nivasafinance.features.otp.core.enums.OtpReference;
import com.nivasafinance.features.otp.core.enums.OtpStatus;
import com.nivasafinance.features.otp.core.enums.OtpSubjectType;
import com.nivasafinance.features.otp.core.repository.OneTimeTokenRepositoryWrapper;
import com.nivasafinance.features.otp.core.service.OtpConfigurationService;
import com.nivasafinance.features.otp.core.service.OtpGenerator;
import com.nivasafinance.features.otp.core.service.OtpTrackingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpCoreServiceImplTest {

    @Mock
    private OtpConfigurationService otpConfigurationService;
    @Mock
    private OtpGeneratorFactory otpGeneratorFactory;
    @Mock
    private OneTimeTokenRepositoryWrapper oneTimeTokenRepositoryWrapper;
    @Mock
    private OtpTrackingStore otpTrackingStore;
    @Mock
    private OtpGenerator otpGenerator;

    private OtpCoreServiceImpl otpCoreService;
    private OtpConfiguration configuration;
    private OtpScope scope;

    @BeforeEach
    void setUp() {
        otpCoreService = new OtpCoreServiceImpl(
                otpConfigurationService,
                otpGeneratorFactory,
                oneTimeTokenRepositoryWrapper);

        configuration = OtpConfiguration.builder()
                .uname(OtpReference.VERIFY_LEAD_FOR_CB.name())
                .config(OtpRuntimeConfig.builder()
                        .otpValidityInMins(10)
                        .otpGenerationMethod("simple_otp_generation")
                        .maxResendAttempts(3)
                        .build())
                .build();
        scope = OtpScope.builder()
                .primary(OtpSubject.builder().type(OtpSubjectType.LEAD).id(1L).build())
                .relatedSubjects(List.of(OtpSubject.builder().type(OtpSubjectType.CONTACT).id(2L).build()))
                .build();
    }

    @Test
    void sendOtp_successfullyPersistsAndQueues() {
        OtpSendCommand command = OtpSendCommand.builder()
                .reference(OtpReference.VERIFY_LEAD_FOR_CB)
                .relatesTo("9876543210")
                .scope(scope)
                .build();

        when(otpConfigurationService.getByReference(OtpReference.VERIFY_LEAD_FOR_CB)).thenReturn(configuration);
        when(otpTrackingStore.countAttemptsSince(eq(OtpReference.VERIFY_LEAD_FOR_CB), eq(scope), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(otpGeneratorFactory.getGenerator("simple_otp_generation")).thenReturn(otpGenerator);
        when(otpGenerator.generate(any())).thenReturn("2244");
        when(oneTimeTokenRepositoryWrapper.saveWithException(any(OneTimeToken.class)))
                .thenReturn(OneTimeToken.builder().id(11L).otp("2244").relatesTo("9876543210").build());
        when(otpTrackingStore.createTrackingRecord(11L, OtpReference.VERIFY_LEAD_FOR_CB, scope, OtpStatus.QUEUED))
                .thenReturn(21L);

        var result = otpCoreService.sendOtp(command, otpTrackingStore);

        assertEquals(21L, result.getRequestId());
        assertEquals(2, result.getResendAttemptsRemaining());
        verify(otpTrackingStore).invalidateActiveTokens(OtpReference.VERIFY_LEAD_FOR_CB, scope);
        verify(otpTrackingStore).createTrackingRecord(11L, OtpReference.VERIFY_LEAD_FOR_CB, scope, OtpStatus.QUEUED);
    }

    @Test
    void sendOtp_rejectsWhenMaxAttemptsReached() {
        OtpSendCommand command = OtpSendCommand.builder()
                .reference(OtpReference.VERIFY_LEAD_FOR_CB)
                .relatesTo("9876543210")
                .scope(scope)
                .build();

        when(otpConfigurationService.getByReference(OtpReference.VERIFY_LEAD_FOR_CB)).thenReturn(configuration);
        when(otpTrackingStore.countAttemptsSince(eq(OtpReference.VERIFY_LEAD_FOR_CB), eq(scope), any(LocalDateTime.class)))
                .thenReturn(3L);

        assertThrows(BadRequestException.class, () -> otpCoreService.sendOtp(command, otpTrackingStore));
    }

    @Test
    void verifyOtp_marksOtpVerified() {
        OtpVerifyCommand command = OtpVerifyCommand.builder()
                .reference(OtpReference.VERIFY_LEAD_FOR_CB)
                .otp("2244")
                .scope(scope)
                .build();

        when(otpConfigurationService.getByReference(OtpReference.VERIFY_LEAD_FOR_CB)).thenReturn(configuration);
        when(otpTrackingStore.findLatestActiveToken(OtpReference.VERIFY_LEAD_FOR_CB, scope))
                .thenReturn(java.util.Optional.of(OtpTrackedToken.builder()
                        .trackingId(21L)
                        .otp("2244")
                        .createdAt(LocalDateTime.now().minusMinutes(2))
                        .status(OtpStatus.QUEUED)
                        .build()));

        var result = otpCoreService.verifyOtp(command, otpTrackingStore);

        assertTrue(result.isVerified());
        verify(otpTrackingStore).updateStatus(21L, OtpStatus.VERIFIED);
    }

    @Test
    void verifyOtp_rejectsExpiredOtp() {
        OtpVerifyCommand command = OtpVerifyCommand.builder()
                .reference(OtpReference.VERIFY_LEAD_FOR_CB)
                .otp("2244")
                .scope(scope)
                .build();

        when(otpConfigurationService.getByReference(OtpReference.VERIFY_LEAD_FOR_CB)).thenReturn(configuration);
        when(otpTrackingStore.findLatestActiveToken(OtpReference.VERIFY_LEAD_FOR_CB, scope))
                .thenReturn(java.util.Optional.of(OtpTrackedToken.builder()
                        .trackingId(21L)
                        .otp("2244")
                        .createdAt(LocalDateTime.now().minusMinutes(11))
                        .status(OtpStatus.SENT)
                        .build()));

        assertThrows(BadRequestException.class, () -> otpCoreService.verifyOtp(command, otpTrackingStore));
    }
}
