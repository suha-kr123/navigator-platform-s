package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.leadbre.enums.LeadBREResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadEligibilityResponse {

    private UUID identifier;
    private LeadBREResultStatus status;
    private BigDecimal eligibleEmi;
    private BigDecimal incomeMaxLoan;
    private BigDecimal eligibleLoanAmount;
    private Integer minTenureMonths;
    private String tenureDisplayRange;
    private BigDecimal emiRangeMin;
    private BigDecimal emiRangeMax;
    private BigDecimal roiMin;
    private BigDecimal roiMax;
    private Boolean softOfferEligible;
    private Boolean consumerVisible;
    private String profileName;
}
