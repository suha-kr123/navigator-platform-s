package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivasafinance.features.creditbureau.entity.CreditBureauDemographicVariation;
import com.nivasafinance.features.creditbureau.repository.CreditBureauDemographicVariationRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrifDemographicVariationService {

    private final CreditBureauDemographicVariationRepositoryWrapper creditBureauDemographicVariationRepositoryWrapper;

    public void parseAndStore(Long enquiryId, JsonNode reportData) {
        try {
            log.info("Parsing credit bureau demographic variations for enquiry ID: {}", enquiryId);

            creditBureauDemographicVariationRepositoryWrapper.deleteByEnquiryId(enquiryId);

            JsonNode standardData = reportData.path("STANDARD-DATA");
            if (standardData.isMissingNode()) {
                log.warn("STANDARD-DATA not found for enquiry ID: {}", enquiryId);
                return;
            }

            parseVariations(enquiryId, standardData);
            parseEmploymentDetails(enquiryId, standardData);

            log.info("Successfully parsed and stored demographic variations for enquiry ID: {}", enquiryId);

        } catch (Exception e) {
            log.error("Failed to parse demographic variations for enquiry ID: {}", enquiryId, e);
            throw new RuntimeException("Failed to parse demographic variations for enquiry ID: " + enquiryId, e);
        }
    }

    private void parseVariations(Long enquiryId, JsonNode standardData) {
        JsonNode demogs = standardData.path("DEMOGS");
        if (demogs.isMissingNode()) {
            return;
        }

        JsonNode variationsArray = demogs.path("VARIATIONS");
        if (variationsArray.isArray() && variationsArray.size() > 0) {
            for (JsonNode variationTypeNode : variationsArray) {
                String variationType = getTextValue(variationTypeNode, "TYPE");

                JsonNode variationItemsArray = variationTypeNode.path("VARIATION");
                if (variationItemsArray.isArray() && variationItemsArray.size() > 0) {
                    for (JsonNode variationItemNode : variationItemsArray) {
                        CreditBureauDemographicVariation demographicVariation = CreditBureauDemographicVariation.builder()
                                .enquiryId(enquiryId)
                                .variationType(variationType)
                                .variationValue(getTextValue(variationItemNode, "VALUE"))
                                .reportedDate(parseDate(getTextValue(variationItemNode, "REPORTED-DT")))
                                .firstReportedDate(parseDate(getTextValue(variationItemNode, "FIRST-REPORTED-DT")))
                                .loanTypeAssociated(getTextValue(variationItemNode, "LOAN-TYPE-ASSOC"))
                                .sourceIndicator(getTextValue(variationItemNode, "SOURCE-INDICATOR"))
                                .build();

                        creditBureauDemographicVariationRepositoryWrapper.saveWithException(demographicVariation);
                    }
                }
            }
        }
    }

    private void parseEmploymentDetails(Long enquiryId, JsonNode standardData) {
        JsonNode employmentDetailsArray = standardData.path("EMPLOYMENT-DETAILS");
        if (employmentDetailsArray.isArray() && employmentDetailsArray.size() > 0) {
            for (JsonNode employmentDetailNode : employmentDetailsArray) {
                JsonNode employmentDetail = employmentDetailNode.path("EMPLOYMENT-DETAIL");
                if (!employmentDetail.isMissingNode()) {
                    CreditBureauDemographicVariation demographicVariation = CreditBureauDemographicVariation.builder()
                            .enquiryId(enquiryId)
                            .variationType("EMPLOYMENT-DETAILS")
                            .variationValue(getTextValue(employmentDetail, "OCCUPATION"))
                            .reportedDate(parseDate(getTextValue(employmentDetail, "LAST-REPORTED-DT")))
                            .firstReportedDate(parseDate(getTextValue(employmentDetail, "FIRST-REPORTED-DT")))
                            .loanTypeAssociated(getTextValue(employmentDetail, "ACCT-TYPE"))
                            .sourceIndicator(getTextValue(employmentDetail, "SOURCE-INDICATOR"))
                            .build();

                    creditBureauDemographicVariationRepositoryWrapper.saveWithException(demographicVariation);
                }
            }
            log.info("Successfully parsed and stored employment details for enquiry ID: {}", enquiryId);
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
}
