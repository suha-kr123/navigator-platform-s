package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeObligationDetailsResponse {

    private List<IncomeDetailData> incomeDetails;
    private ObligationsData obligations;
    private BigDecimal monthlyFamilyIncome;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeDetailData {
        private CodeValueResponse incomeSource;
        private BigDecimal amount;
        private List<IncomeDocumentChecklistData> documentChecklist;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeDocumentChecklistData {
        private CodeValueResponse documentType;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObligationsData {
        private BigDecimal existingEmi;
    }
}
