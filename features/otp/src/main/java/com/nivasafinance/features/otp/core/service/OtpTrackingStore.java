package com.nivasafinance.features.otp.core.service;

import com.nivasafinance.features.otp.core.dto.OtpScope;
import com.nivasafinance.features.otp.core.dto.OtpTrackedToken;
import com.nivasafinance.features.otp.core.enums.OtpReference;
import com.nivasafinance.features.otp.core.enums.OtpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTrackingStore {
    long countAttemptsSince(OtpReference reference, OtpScope scope, LocalDateTime since);
    void invalidateActiveTokens(OtpReference reference, OtpScope scope);
    Long createTrackingRecord(Long tokenId, OtpReference reference, OtpScope scope, OtpStatus status);
    Optional<OtpTrackedToken> findLatestActiveToken(OtpReference reference, OtpScope scope);
    void updateStatus(Long trackingId, OtpStatus status);
}
