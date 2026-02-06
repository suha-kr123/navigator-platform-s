package com.nivasafinance.features.consent.dto;

import com.nivasafinance.features.consent.enums.ConsentStatus;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WithdrawConsentResponse {

    private ConsentStatus consentStatus;
}
