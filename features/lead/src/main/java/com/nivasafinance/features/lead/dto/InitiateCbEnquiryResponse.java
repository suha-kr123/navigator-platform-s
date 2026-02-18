package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiateCbEnquiryResponse {
    private UUID enquiryIdentifier;
    private CreditBureauEnquiryStatus status;
    private UUID consentIdentifier;
}

