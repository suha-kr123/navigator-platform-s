package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditDetailsResponse {
    private String underwriter;
    private CodeValueResponse occupationProfile;
    private CodeValueResponse roofProfile;
    private CodeValueResponse ltv;
    private CodeValueResponse foir;
    private CodeValueResponse monthlyFamilyIncome;
    private CodeValueResponse propertyDocumentType;
    private CodeValueResponse eligibleLoanAmount;
    private CodeValueResponse location;
    private CodeValueResponse bureauRating;
    private CodeValueResponse customerProfiles;
}

