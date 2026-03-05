package com.nivasafinance.features.consent.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ResendConsentRequest {

    UUID consentIdentifier;

    UUID enquiryIdentifier;

    UUID leadIdentifier;

    UUID contactIdentifier;

    Long personId;

    String recipientPhone;
}
