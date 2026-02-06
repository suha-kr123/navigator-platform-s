package com.nivasafinance.features.consent.dto;

import com.nivasafinance.features.consent.entity.Consent;
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
public class ConsentResponse {

    private Long id;
    private UUID identifier;
    private ConsentStatus status;
    private LocalDateTime consentSentTime;
    private LocalDateTime consentReceivedTime;
    private LocalDateTime consentWithdrawlRequestedTime;

    public static ConsentResponse toConsentResponse(Consent consent) {
        if (consent == null) {
            return null;
        }
        return ConsentResponse.builder()
                .id(consent.getId())
                .identifier(consent.getIdentifier())
                .status(consent.getStatus())
                .consentSentTime(consent.getConsentSentDetails() != null
                        ? consent.getConsentSentDetails().getConsentSentTime() : null)
                .consentReceivedTime(consent.getConsentReceivedDetails() != null
                        ? consent.getConsentReceivedDetails().getConsentReceivedTime() : null)
                .consentWithdrawlRequestedTime(consent.getConsentWithdrawnDetails() != null
                        ? consent.getConsentWithdrawnDetails().getConsentWithdrawalRequestedTime() : null)
                .build();
    }
}
