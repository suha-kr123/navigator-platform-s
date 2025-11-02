package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.enums.TenureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursementDetailsResponse {
    private BigDecimal disbursedAmount;
    private BigDecimal roi;
    private Integer tenureValue;
    private TenureType tenureType;
    private LocalDate disbursedDate;
    private BigDecimal processingFees;
    private List<TrancheResponse> tranches;
}

