package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivasafinance.features.creditbureau.entity.CreditBureauTrends;
import com.nivasafinance.features.creditbureau.repository.CreditBureauTrendsRepository;
import com.nivasafinance.features.creditbureau.repository.CreditBureauTrendsRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrifTrendsService {

    private final CreditBureauTrendsRepositoryWrapper creditBureauTrendsRepositoryWrapper;
    private final CreditBureauTrendsRepository creditBureauTrendsRepository;

    public void parseAndStore(Long enquiryId, JsonNode reportData) {
        try {
            log.info("Parsing credit bureau score trends for enquiry ID: {}", enquiryId);

            // reportData is already REPORT-DATA node, so access TRENDS directly
            JsonNode trendsNode = reportData.path("TRENDS");
            if (trendsNode.isMissingNode()) {
                log.info("TRENDS not found in REPORT-DATA for enquiry ID: {}", enquiryId);
                return;
            }

            // Delete existing trends for this enquiry
            List<CreditBureauTrends> existingTrends = creditBureauTrendsRepositoryWrapper.findByEnquiryIdEntity(enquiryId);
            if (!existingTrends.isEmpty()) {
                creditBureauTrendsRepository.deleteAll(existingTrends);
                log.info("Deleted {} existing score trends for enquiry ID: {}", existingTrends.size(), enquiryId);
            }

            String trendName = getTextValue(trendsNode, "NAME");
            String datesString = getTextValue(trendsNode, "DATES");
            String valuesString = getTextValue(trendsNode, "VALUES");
            String descriptionString = getTextValue(trendsNode, "DESCRIPTION");

            // Check if DATES or VALUES are null or empty
            if (datesString == null || datesString.trim().isEmpty() || 
                valuesString == null || valuesString.trim().isEmpty()) {
                log.info("DATES or VALUES are empty in TRENDS for enquiry ID: {}. Skipping trends parsing.", enquiryId);
                return;
            }

            // Parse pipe-separated values
            List<String> dates = parsePipeSeparatedString(datesString);
            List<String> values = parsePipeSeparatedString(valuesString);
            List<String> descriptions = descriptionString != null
                    ? parsePipeSeparatedString(descriptionString)
                    : new ArrayList<>();

            // Validate that dates and values have the same count
            if (dates.size() != values.size()) {
                log.warn("Mismatch in dates and values count for enquiry ID: {}. Dates: {}, Values: {}",
                        enquiryId, dates.size(), values.size());
                // Use the minimum size to avoid index out of bounds
                int minSize = Math.min(dates.size(), values.size());
                dates = dates.subList(0, minSize);
                values = values.subList(0, minSize);
            }

            // Create one entity per date/score pair
            int savedCount = 0;
            for (int i = 0; i < dates.size(); i++) {
                String dateStr = dates.get(i);
                String valueStr = values.get(i);
                String description = (i < descriptions.size()) ? descriptions.get(i) : null;

                LocalDate date = parseDate(dateStr);
                Integer scoreValue = parseInteger(valueStr);

                if (date != null && scoreValue != null) {
                    CreditBureauTrends scoreTrend = CreditBureauTrends.builder()
                            .enquiryId(enquiryId)
                            .trendName(trendName)
                            .date(date)
                            .scoreValue(scoreValue)
                            .description(description)
                            .build();

                    creditBureauTrendsRepositoryWrapper.saveWithException(scoreTrend);
                    savedCount++;
                } else {
                    log.warn("Skipping invalid date or score value at index {} for enquiry ID: {}. Date: {}, Value: {}",
                            i, enquiryId, dateStr, valueStr);
                }
            }

            log.info("Successfully parsed and stored {} score trends for enquiry ID: {}", savedCount, enquiryId);

        } catch (Exception e) {
            log.error("Failed to parse credit bureau score trends for enquiry ID: {}", enquiryId, e);
            throw new RuntimeException("Failed to parse credit bureau score trends for enquiry ID: " + enquiryId, e);
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText(null);
    }

    private List<String> parsePipeSeparatedString(String pipeSeparatedString) {
        if (pipeSeparatedString == null || pipeSeparatedString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(pipeSeparatedString.split("\\|"));
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            String trimmed = dateString.trim();
            // Try dd-MM-yyyy format first (e.g., "30-09-2025")
            if (trimmed.length() == 10 && trimmed.contains("-")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                return LocalDate.parse(trimmed, formatter);
            }
            // Try yyyy-MM-dd format
            if (trimmed.length() == 10) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(trimmed, formatter);
            }
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateString);
        }
        return null;
    }

    private Integer parseInteger(String valueString) {
        if (valueString == null || valueString.trim().isEmpty()) {
            return null;
        }
        try {
            // Remove commas and parse (e.g., "1,749" -> 1749)
            String cleaned = valueString.trim().replace(",", "");
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer value: {}", valueString);
            return null;
        }
    }
}