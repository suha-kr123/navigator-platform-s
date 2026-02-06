package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivasafinance.features.creditbureau.entity.CreditBureauAccountPaymentHistory;
import com.nivasafinance.features.creditbureau.entity.CreditBureauAccountSecurityDetails;
import com.nivasafinance.features.creditbureau.entity.CreditBureauTradeline;
import com.nivasafinance.features.creditbureau.repository.CreditBureauAccountPaymentHistoryRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauAccountSecurityDetailsRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauTradelineRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrifTradelineService {

    private final CreditBureauTradelineRepositoryWrapper creditBureauTradelineRepositoryWrapper;
    private final CreditBureauAccountPaymentHistoryRepositoryWrapper creditBureauAccountPaymentHistoryRepositoryWrapper;
    private final CreditBureauAccountSecurityDetailsRepositoryWrapper creditBureauAccountSecurityDetailsRepositoryWrapper;

    public void parseAndStore(Long enquiryId, JsonNode reportData) {
        try {
            log.info("Parsing credit bureau tradelines for enquiry ID: {}", enquiryId);

            JsonNode standardData = reportData.path("STANDARD-DATA");
            if (standardData.isMissingNode()) {
                log.warn("STANDARD-DATA not found for enquiry ID: {}", enquiryId);
                return;
            }

            creditBureauTradelineRepositoryWrapper.deleteByEnquiryId(enquiryId);

            JsonNode tradelinesArray = standardData.path("TRADELINES");
            if (tradelinesArray.isArray() && tradelinesArray.size() > 0) {
                for (JsonNode tradelineNode : tradelinesArray) {
                    CreditBureauTradeline tradeline = parseTradeline(enquiryId, tradelineNode);
                    CreditBureauTradeline savedTradeline = creditBureauTradelineRepositoryWrapper.saveWithException(tradeline);

                    parseAndStorePaymentHistory(savedTradeline.getId(), tradelineNode);
                    parseAndStoreSecurityDetails(savedTradeline.getId(), tradelineNode);
                }
                log.info("Successfully parsed and stored {} tradelines for enquiry ID: {}", 
                    tradelinesArray.size(), enquiryId);
            } else {
                log.info("No tradelines found in response for enquiry ID: {}", enquiryId);
            }

        } catch (Exception e) {
            log.error("Failed to parse credit bureau tradelines for enquiry ID: {}", enquiryId, e);
            throw new RuntimeException("Failed to parse credit bureau tradelines for enquiry ID: " + enquiryId, e);
        }
    }

    private CreditBureauTradeline parseTradeline(Long enquiryId, JsonNode tradelineNode) {
        return CreditBureauTradeline.builder()
                .enquiryId(enquiryId)
                .accountNumber(getTextValue(tradelineNode, "ACCT-NUMBER"))
                .creditGrantor(getTextValue(tradelineNode, "CREDIT-GRANTOR"))
                .creditGrantorGroup(getTextValue(tradelineNode, "CREDIT-GRANTOR-GROUP"))
                .creditGrantorType(getTextValue(tradelineNode, "CREDIT-GRANTOR-TYPE"))
                .accountType(getTextValue(tradelineNode, "ACCT-TYPE"))
                .accountStatus(getTextValue(tradelineNode, "ACCOUNT-STATUS"))
                .reportedDate(parseDate(getTextValue(tradelineNode, "REPORTED-DT")))
                .closedDate(parseDate(getTextValue(tradelineNode, "CLOSED-DT")))
                .ownershipType(getTextValue(tradelineNode, "OWNERSHIP-TYPE"))
                .disbursedAmount(parseNumericFromString(getTextValue(tradelineNode, "DISBURSED-AMT")))
                .disbursedDate(parseDate(getTextValue(tradelineNode, "DISBURSED-DT")))
                .currentBalance(parseNumericFromString(getTextValue(tradelineNode, "CURRENT-BAL")))
                .creditLimit(parseNumericFromString(getTextValue(tradelineNode, "CREDIT-LIMIT")))
                .cashLimit(parseNumericFromString(getTextValue(tradelineNode, "CASH-LIMIT")))
                .overdueAmount(parseNumericFromString(getTextValue(tradelineNode, "OVERDUE-AMT")))
                .installmentAmount(getTextValue(tradelineNode, "INSTALLMENT-AMT"))
                .installmentFrequency(getTextValue(tradelineNode, "INSTALLMENT-FREQUENCY"))
                .originalTerm(parseInteger(tradelineNode, "ORIGINAL-TERM"))
                .termToMaturity(parseInteger(tradelineNode, "TERM-TO-MATURITY"))
                .repaymentTenure(getTextValue(tradelineNode, "REPAYMENT-TENURE"))
                .interestRate(getTextValue(tradelineNode, "INTEREST-RATE"))
                .lastPaymentDate(parseDateTime(getTextValue(tradelineNode, "LAST-PAYMENT-DT")))
                .lastPaidAmount(parseNumericFromString(getTextValue(tradelineNode, "LAST-PAID-AMOUNT")))
                .actualPayment(parseNumericFromString(getTextValue(tradelineNode, "ACTUAL-PAYMENT")))
                .writeOffAmount(parseNumericFromString(getTextValue(tradelineNode, "WRITE-OFF-AMT")))
                .principalWriteOffAmount(parseNumericFromString(getTextValue(tradelineNode, "PRINCIPAL-WRITE-OFF-AMT")))
                .settlementAmount(parseNumericFromString(getTextValue(tradelineNode, "SETTLEMENT-AMT")))
                .writeOffDate(parseDate(getTextValue(tradelineNode, "WRITE-OFF-DT")))
                .securityStatus(getTextValue(tradelineNode, "SECURITY-STATUS"))
                .obligation(parseNumericFromString(getTextValue(tradelineNode, "OBLIGATION")))
                .accountRemarks(getTextValue(tradelineNode, "ACCOUNT-REMARKS"))
                .accountInDispute(parseBoolean(getTextValue(tradelineNode, "ACCT-IN-DISPUTE")))
                .suitFiledWilfulDefaultStatus(getTextValue(tradelineNode, "SUIT-FILED-WILFUL-DEFAULT-STATUS"))
                .writtenOffSettledStatus(getTextValue(tradelineNode, "WRITTEN-OFF-SETTLED-STATUS"))
                .suitFiledDate(parseDate(getTextValue(tradelineNode, "SUIT-FILED-DT")))
                .occupation(getTextValue(tradelineNode, "OCCUPATION"))
                .incomeFrequency(getTextValue(tradelineNode, "INCOME-FREQUENCY"))
                .incomeAmount(parseNumericFromString(getTextValue(tradelineNode, "INCOME-AMOUNT")))
                .build();
    }

    private void parseAndStorePaymentHistory(Long tradelineId, JsonNode tradelineNode) {
        try {
            creditBureauAccountPaymentHistoryRepositoryWrapper.deleteByTradelineId(tradelineId);

            JsonNode historyArray = tradelineNode.path("HISTORY");
            if (historyArray.isArray() && historyArray.size() > 0) {
                for (JsonNode historyNode : historyArray) {
                    String historyType = getTextValue(historyNode, "NAME");
                    String datesString = getTextValue(historyNode, "DATES");
                    String valuesString = getTextValue(historyNode, "VALUES");

                    if (historyType != null && datesString != null && valuesString != null) {
                        String[] dates = datesString.split("\\|");
                        String[] values = valuesString.split("\\|");

                        int minLength = Math.min(dates.length, values.length);
                        for (int i = 0; i < minLength; i++) {
                            String monthYear = dates[i].trim();
                            String historyValue = values[i].trim();

                            if (monthYear.isEmpty() || historyValue.isEmpty()) {
                                continue;
                            }

                            CreditBureauAccountPaymentHistory paymentHistory = CreditBureauAccountPaymentHistory.builder()
                                    .tradelineId(tradelineId)
                                    .historyType(historyType)
                                    .monthYear(monthYear)
                                    .historyValue(historyValue)
                                    .build();

                            creditBureauAccountPaymentHistoryRepositoryWrapper.saveWithException(paymentHistory);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse payment history for tradeline ID: {}", tradelineId, e);
        }
    }

    private void parseAndStoreSecurityDetails(Long tradelineId, JsonNode tradelineNode) {
        try {
            creditBureauAccountSecurityDetailsRepositoryWrapper.deleteByTradelineId(tradelineId);

            JsonNode securityDetailsArray = tradelineNode.path("SECURITY-DETAILS");
            if (securityDetailsArray.isArray() && securityDetailsArray.size() > 0) {
                for (JsonNode securityDetailNode : securityDetailsArray) {
                    CreditBureauAccountSecurityDetails securityDetails = CreditBureauAccountSecurityDetails.builder()
                            .tradelineId(tradelineId)
                            .securityType(getTextValue(securityDetailNode, "SECURITY-TYPE"))
                            .ownerName(getTextValue(securityDetailNode, "OWNER-NAME"))
                            .securityValuation(parseNumericFromString(getTextValue(securityDetailNode, "SECURITY-VALUATION")))
                            .dateOfValuation(parseDate(getTextValue(securityDetailNode, "DATE-OF-VALUATION")))
                            .securityCharge(getTextValue(securityDetailNode, "SECURITY-CHARGE"))
                            .propertyAddress(getTextValue(securityDetailNode, "PROPERTY-ADDRESS"))
                            .automobileType(getTextValue(securityDetailNode, "AUTOMOBILE-TYPE"))
                            .yearOfManufacturing(parseInteger(securityDetailNode, "YEAR-OF-MANUFACTURING"))
                            .registrationNumber(getTextValue(securityDetailNode, "REGISTRATION-NUMBER"))
                            .engineNumber(getTextValue(securityDetailNode, "ENGINE-NUMBER"))
                            .chassisNumber(getTextValue(securityDetailNode, "CHASSIE-NUMBER"))
                            .build();

                    creditBureauAccountSecurityDetailsRepositoryWrapper.saveWithException(securityDetails);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse security details for tradeline ID: {}", tradelineId, e);
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

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            String trimmed = dateString.trim();
            if (trimmed.contains("-") && trimmed.length() == 10) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                return LocalDate.parse(trimmed, formatter);
            } else if (trimmed.contains("-") && trimmed.length() > 10) {
                String datePart = trimmed.substring(0, 10);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(datePart, formatter);
            }
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateString);
        }
        return null;
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            String trimmed = dateTimeString.trim();
            if (trimmed.contains(" ") && trimmed.length() >= 19) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(trimmed.substring(0, 19), formatter);
            }
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse datetime: {}", dateTimeString);
        }
        return null;
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if ("true".equalsIgnoreCase(trimmed) || "Y".equalsIgnoreCase(trimmed) || "1".equals(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed) || "N".equalsIgnoreCase(trimmed) || "0".equals(trimmed)) {
            return false;
        }
        return null;
    }

    private BigDecimal parseNumericFromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            String trimmed = value.trim();
            trimmed = trimmed.replace(",", "");
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse numeric value: {}", value);
            return null;
        }
    }
}
