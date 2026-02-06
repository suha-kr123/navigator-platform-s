package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.consent.enums.ConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryConsentStatusResponse {
    private UUID enquiryIdentifier;
    private UUID consentIdentifier;
    private ConsentStatus consentStatus;
    private LocalDateTime consentSentTime;
    private LocalDateTime consentReceivedTime;
    private LocalDateTime consentWithdrawalRequestedTime;
}
