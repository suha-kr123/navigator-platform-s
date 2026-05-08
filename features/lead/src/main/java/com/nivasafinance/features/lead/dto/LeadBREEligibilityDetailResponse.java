package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.leadbre.enums.LeadBREResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadBREEligibilityDetailResponse {

    private UUID identifier;
    private LeadBREResultStatus status;
    private String locationTier;
    private Integer bureauTrackScore;
    private BigDecimal crifCreditScore;
    private Boolean deedOk;
    private String propertyKhataGroup;
    private String roofType;
    private BigDecimal crifObligation;
    private ProfileMatch profileMatch;
    private LoanCalculation loanCalculation;
    private Boolean softOfferEligible;
    private Boolean consumerVisible;
    private BigDecimal bankItrGstIncomePer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileMatch {
        private String profileName;
        private BigDecimal roiMin;
        private BigDecimal roiMax;
        private String profileMatchStatus;
        private List<String> matchingProfiles;
        private Boolean consumerVisibleProfile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoanCalculation {
        private BigDecimal propertyValue;
        private BigDecimal ltvMaxLoan;
        private BigDecimal totalApplicableIncome;
        private BigDecimal totalObligations;
        private BigDecimal eligibleEmi;
        private BigDecimal incomeMaxLoan;
        private BigDecimal eligibleLoanAmount;
        private Integer minTenureMonths;
        private String tenureDisplayRange;
        private BigDecimal emiRangeMin;
        private BigDecimal emiRangeMax;
        private String bindingConstraint;
    }
}
