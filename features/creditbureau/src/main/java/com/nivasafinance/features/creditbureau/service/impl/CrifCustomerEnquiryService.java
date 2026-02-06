package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivasafinance.features.creditbureau.entity.CreditBureauCustomerEnquiry;
import com.nivasafinance.features.creditbureau.repository.CreditBureauCustomerEnquiryRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrifCustomerEnquiryService {

    private final CreditBureauCustomerEnquiryRepositoryWrapper creditBureauCustomerEnquiryRepositoryWrapper;

    public void parseAndStore(Long enquiryId, JsonNode reportData) {
        try {
            log.info("Parsing credit bureau customer enquiries for enquiry ID: {}", enquiryId);

            creditBureauCustomerEnquiryRepositoryWrapper.deleteByEnquiryId(enquiryId);

            JsonNode standardData = reportData.path("STANDARD-DATA");
            if (standardData.isMissingNode()) {
                log.warn("STANDARD-DATA not found for enquiry ID: {}", enquiryId);
                return;
            }

            JsonNode inquiryHistoryArray = standardData.path("INQUIRY-HISTORY");
            if (inquiryHistoryArray.isArray() && inquiryHistoryArray.size() > 0) {
                for (JsonNode inquiryNode : inquiryHistoryArray) {
                    CreditBureauCustomerEnquiry customerEnquiry = CreditBureauCustomerEnquiry.builder()
                            .enquiryId(enquiryId)
                            .lenderName(getTextValue(inquiryNode, "LENDER-NAME"))
                            .inquiryDate(parseDate(getTextValue(inquiryNode, "INQUIRY-DT")))
                            .ownershipType(getTextValue(inquiryNode, "OWNERSHIP-TYPE"))
                            .creditInquiryPurposeType(getTextValue(inquiryNode, "CREDIT-INQ-PURPS-TYPE"))
                            .inquiryAmount(parseNumericFromString(getTextValue(inquiryNode, "AMOUNT")))
                            .build();

                    creditBureauCustomerEnquiryRepositoryWrapper.saveWithException(customerEnquiry);
                }
                log.info("Successfully parsed and stored {} customer enquiries for enquiry ID: {}", 
                    inquiryHistoryArray.size(), enquiryId);
            } else {
                log.info("No customer enquiries found in response for enquiry ID: {}", enquiryId);
            }

        } catch (Exception e) {
            log.error("Failed to parse customer enquiries for enquiry ID: {}", enquiryId, e);
            throw new RuntimeException("Failed to parse customer enquiries for enquiry ID: " + enquiryId, e);
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText(null);
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
