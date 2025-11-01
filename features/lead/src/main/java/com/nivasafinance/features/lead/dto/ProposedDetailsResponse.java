package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.enums.TenureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposedDetailsResponse {
    private BigDecimal proposedLoanAmount;
    private BigDecimal roi;
    private Integer tenureValue;
    private TenureType tenureType;
    private BigDecimal emi;
}

