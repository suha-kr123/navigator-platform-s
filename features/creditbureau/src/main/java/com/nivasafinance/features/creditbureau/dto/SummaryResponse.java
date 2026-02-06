package com.nivasafinance.features.creditbureau.dto;

import com.nivasafinance.features.creditbureau.entity.CreditBureauSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryResponse {
    private UUID identifier;
    private Integer creditScore;
    private String scoreVersion;
    private String scoreName;
    private Integer totalAccounts;
    private Integer activeAccounts;
    private Integer overdueAccounts;
    private Integer closedAccounts;
    private Integer securedAccounts;
    private Integer unsecuredAccounts;
    private Integer untaggedAccounts;
    private BigDecimal totalCurrentBalance;
    private BigDecimal currentBalanceSecured;
    private BigDecimal currentBalanceUnsecured;
    private BigDecimal totalOverdueAmount;
    private BigDecimal totalSanctionedAmount;
    private BigDecimal totalDisbursedAmount;
    private Integer noOfOwnMfis;
    private Integer noOfOtherMfis;
    private BigDecimal totalOwnCurrentBalance;
    private BigDecimal totalOwnInstallmentAmount;
    private BigDecimal totalOwnDisbursedAmount;
    private BigDecimal totalOtherInstallmentAmount;
    private BigDecimal totalOtherDisbursedAmount;
    private BigDecimal totalOtherOverdueAmount;
    private Integer maxWorstDelinquency;
    private Integer accountCount;
    private List<ScoreFactor> scoreFactorDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreFactor {
        private String factorType;
        private String factorDescription;
    }

    public static SummaryResponse toSummaryResponse(CreditBureauSummary summary) {
        if (summary == null) {
            return null;
        }
        return SummaryResponse.builder()
                .identifier(summary.getIdentifier())
                .creditScore(summary.getCreditScore())
                .scoreVersion(summary.getScoreVersion())
                .scoreName(summary.getScoreName())
                .totalAccounts(summary.getTotalAccounts())
                .activeAccounts(summary.getActiveAccounts())
                .overdueAccounts(summary.getOverdueAccounts())
                .closedAccounts(summary.getClosedAccounts())
                .securedAccounts(summary.getSecuredAccounts())
                .unsecuredAccounts(summary.getUnsecuredAccounts())
                .untaggedAccounts(summary.getUntaggedAccounts())
                .totalCurrentBalance(summary.getTotalCurrentBalance())
                .currentBalanceSecured(summary.getCurrentBalanceSecured())
                .currentBalanceUnsecured(summary.getCurrentBalanceUnsecured())
                .totalOverdueAmount(summary.getTotalOverdueAmount())
                .totalSanctionedAmount(summary.getTotalSanctionedAmount())
                .totalDisbursedAmount(summary.getTotalDisbursedAmount())
                .noOfOwnMfis(summary.getNoOfOwnMfis())
                .noOfOtherMfis(summary.getNoOfOtherMfis())
                .totalOwnCurrentBalance(summary.getTotalOwnCurrentBalance())
                .totalOwnInstallmentAmount(summary.getTotalOwnInstallmentAmount())
                .totalOwnDisbursedAmount(summary.getTotalOwnDisbursedAmount())
                .totalOtherInstallmentAmount(summary.getTotalOtherInstallmentAmount())
                .totalOtherDisbursedAmount(summary.getTotalOtherDisbursedAmount())
                .totalOtherOverdueAmount(summary.getTotalOtherOverdueAmount())
                .maxWorstDelinquency(summary.getMaxWorstDelinquency())
                .accountCount(summary.getAccountCount())
                .scoreFactorDetails(summary.getScoreFactorDetails() != null ?
                        summary.getScoreFactorDetails().stream()
                                .map(sf -> ScoreFactor.builder()
                                        .factorType(sf.getFactorType())
                                        .factorDescription(sf.getFactorDescription())
                                        .build())
                                .toList() : null)
                .build();
    }
}