package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchIncomeAndObligationRequest {

    private Optional<List<IncomeDetailsData>> incomeDetails;
    private Optional<ObligationData> obligationDetails;
    private Optional<BigDecimal> monthlyFamilyIncome;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeDetailsData {

        private String incomeSource;
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObligationData {

        private BigDecimal existingEmi;
    }
}

