package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCreditDetailsRequest {
    private String occupationProfile;
    private String roofProfile;
    private String ltv;
    private String foir;
    private String monthlyFamilyIncome;
    private String propertyDocumentType;
    private String eligibleLoanAmount;
    private String location;
    private String bureauRating;
    private String customerProfiles;
}

