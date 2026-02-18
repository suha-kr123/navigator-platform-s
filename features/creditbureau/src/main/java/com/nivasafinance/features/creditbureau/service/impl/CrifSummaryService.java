package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivasafinance.features.creditbureau.entity.CreditBureauSummary;
import com.nivasafinance.features.creditbureau.entity.CreditBureauSummary.ScoreFactor;
import com.nivasafinance.features.creditbureau.repository.CreditBureauSummaryRepository;
import com.nivasafinance.features.creditbureau.repository.CreditBureauSummaryRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrifSummaryService {

    private final CreditBureauSummaryRepositoryWrapper creditBureauSummaryRepositoryWrapper;
    private final CreditBureauSummaryRepository creditBureauSummaryRepository;

    public void parseAndStore(Long enquiryId, JsonNode reportData) {
        try {
            log.info("Parsing credit bureau summary for enquiry ID: {}", enquiryId);

            CreditBureauSummary summary = CreditBureauSummary.builder()
                    .enquiryId(enquiryId)
                    .build();

            parseScoreData(summary, reportData);
            parseAccountsSummary(summary, reportData);

            creditBureauSummaryRepositoryWrapper.findByEnquiryId(enquiryId)
                    .ifPresent(existing -> creditBureauSummaryRepository.delete(existing));

            creditBureauSummaryRepositoryWrapper.saveWithException(summary);

            log.info("Successfully parsed and stored credit bureau summary for enquiry ID: {}", enquiryId);

        } catch (Exception e) {
            log.error("Failed to parse credit bureau summary for enquiry ID: {}", enquiryId, e);
            throw new RuntimeException("Failed to parse credit bureau summary for enquiry ID: " + enquiryId, e);
        }
    }

    private void parseScoreData(CreditBureauSummary summary, JsonNode reportData) {
        JsonNode standardData = reportData.path("STANDARD-DATA");
        if (standardData.isMissingNode()) {
            return;
        }

        JsonNode scoreArray = standardData.path("SCORE");
        if (scoreArray.isArray() && scoreArray.size() > 0) {
            JsonNode firstScore = scoreArray.get(0);

            summary.setScoreName(getTextValue(firstScore, "NAME"));
            summary.setScoreVersion(getTextValue(firstScore, "VERSION"));

            String scoreValue = getTextValue(firstScore, "VALUE");
            if (scoreValue != null && !scoreValue.trim().isEmpty()) {
                try {
                    summary.setCreditScore(Integer.parseInt(scoreValue.trim()));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse credit score value: {}", scoreValue);
                }
            }

            JsonNode factorsArray = firstScore.path("FACTORS");
            if (factorsArray.isArray()) {
                List<ScoreFactor> factors = new ArrayList<>();
                for (JsonNode factorNode : factorsArray) {
                    ScoreFactor factor = ScoreFactor.builder()
                            .factorType(getTextValue(factorNode, "TYPE"))
                            .factorDescription(getTextValue(factorNode, "DESC"))
                            .build();
                    factors.add(factor);
                }
                summary.setScoreFactorDetails(factors);
            }
        }
    }

    private void parseAccountsSummary(CreditBureauSummary summary, JsonNode reportData) {
        JsonNode accountsSummary = reportData.path("ACCOUNTS-SUMMARY");
        if (accountsSummary.isMissingNode()) {
            return;
        }

        JsonNode primarySummary = accountsSummary.path("PRIMARY-ACCOUNTS-SUMMARY");
        if (!primarySummary.isMissingNode()) {
            summary.setTotalAccounts(parseInteger(primarySummary, "NUMBER-OF-ACCOUNTS"));
            summary.setActiveAccounts(parseInteger(primarySummary, "ACTIVE-ACCOUNTS"));
            summary.setOverdueAccounts(parseInteger(primarySummary, "OVERDUE-ACCOUNTS"));
            summary.setSecuredAccounts(parseInteger(primarySummary, "SECURED-ACCOUNTS"));
            summary.setUnsecuredAccounts(parseInteger(primarySummary, "UNSECURED-ACCOUNTS"));
            summary.setUntaggedAccounts(parseInteger(primarySummary, "UNTAGGED-ACCOUNTS"));

            summary.setTotalCurrentBalance(parseNumeric(primarySummary, "TOTAL-CURRENT-BALANCE"));
            summary.setCurrentBalanceSecured(parseNumeric(primarySummary, "CURRENT-BALANCE-SECURED"));
            summary.setCurrentBalanceUnsecured(parseNumeric(primarySummary, "CURRENT-BALANCE-UNSECURED"));
            summary.setTotalOverdueAmount(parseNumeric(primarySummary, "TOTAL-AMT-OVERDUE"));
            summary.setTotalSanctionedAmount(parseNumeric(primarySummary, "TOTAL-SANCTIONED-AMT"));
            summary.setTotalDisbursedAmount(parseNumeric(primarySummary, "TOTAL-DISBURSED-AMT"));
        }

        JsonNode mfiSummary = accountsSummary.path("MFI-GROUP-ACCOUNTS-SUMMARY");
        if (!mfiSummary.isMissingNode()) {
            summary.setClosedAccounts(parseInteger(mfiSummary, "CLOSED-ACCOUNTS"));
            summary.setNoOfOwnMfis(parseInteger(mfiSummary, "NO-OF-OWN-MFIS"));
            summary.setNoOfOtherMfis(parseInteger(mfiSummary, "NO-OF-OTHER-MFIS"));
            summary.setTotalOwnCurrentBalance(parseNumeric(mfiSummary, "TOTAL-OWN-CURRENT-BALANCE"));
            summary.setTotalOwnInstallmentAmount(parseNumeric(mfiSummary, "TOTAL-OWN-INSTALLMENT-AMT"));
            summary.setTotalOwnDisbursedAmount(parseNumeric(mfiSummary, "TOTAL-OWN-DISBURSED-AMT"));
            summary.setTotalOtherInstallmentAmount(parseNumeric(mfiSummary, "TOTAL-OTHER-INSTALLMENT-AMT"));
            summary.setTotalOtherDisbursedAmount(parseNumeric(mfiSummary, "TOTAL-OTHER-DISBURSED-AMT"));
            summary.setTotalOtherOverdueAmount(parseNumeric(mfiSummary, "TOTAL-OTHER-OVERDUE-AMT"));
            summary.setMaxWorstDelinquency(parseInteger(mfiSummary, "MAX-WORST-DELINQUENCY"));
        }

        if (summary.getAccountCount() == null && summary.getTotalAccounts() != null) {
            summary.setAccountCount(summary.getTotalAccounts());
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText(null);
    }

    private Integer parseInteger(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        if (fieldNode.isInt()) {
            return fieldNode.asInt();
        }
        if (fieldNode.isTextual()) {
            try {
                return Integer.parseInt(fieldNode.asText().trim());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse integer for field {}: {}", fieldName, fieldNode.asText());
                return null;
            }
        }
        return null;
    }

    private BigDecimal parseNumeric(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.decimalValue();
        }
        if (fieldNode.isTextual()) {
            try {
                String value = fieldNode.asText().trim();
                value = value.replace(",", "");
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse numeric for field {}: {}", fieldName, fieldNode.asText());
                return null;
            }
        }
        return null;
    }
}
